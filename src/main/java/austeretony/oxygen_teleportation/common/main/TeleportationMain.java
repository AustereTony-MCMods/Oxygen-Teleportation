package austeretony.oxygen_teleportation.common.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.privilege.PrivilegeImpl;
import austeretony.oxygen_core.common.privilege.PrivilegedGroupImpl;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_core.server.api.RequestsFilterHelper;
import austeretony.oxygen_core.server.command.CommandOxygenServer;
import austeretony.oxygen_teleportation.client.CampsSyncHandlerClient;
import austeretony.oxygen_teleportation.client.LocationsSyncHandlerClient;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.TeleportationStatusMessagesHandler;
import austeretony.oxygen_teleportation.client.command.TeleportationArgumentExecutorClient;
import austeretony.oxygen_teleportation.client.event.TeleportationEventsClient;
import austeretony.oxygen_teleportation.client.gui.context.TeleportToPlayerContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;
import austeretony.oxygen_teleportation.client.input.TeleportationKeyHandler;
import austeretony.oxygen_teleportation.client.input.TeleportationMenuKeyHandler;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.network.client.CPDownloadImagePart;
import austeretony.oxygen_teleportation.common.network.client.CPFavoriteCampUpdated;
import austeretony.oxygen_teleportation.common.network.client.CPPlayerUninvited;
import austeretony.oxygen_teleportation.common.network.client.CPStartImageDownload;
import austeretony.oxygen_teleportation.common.network.client.CPSyncAdditionalData;
import austeretony.oxygen_teleportation.common.network.client.CPSyncCooldown;
import austeretony.oxygen_teleportation.common.network.client.CPSyncFeeItemStack;
import austeretony.oxygen_teleportation.common.network.client.CPSyncInvitedPlayers;
import austeretony.oxygen_teleportation.common.network.client.CPWorldPointCreated;
import austeretony.oxygen_teleportation.common.network.client.CPWorldPointEdited;
import austeretony.oxygen_teleportation.common.network.client.CPWorldPointRemoved;
import austeretony.oxygen_teleportation.common.network.server.SPChangeJumpProfile;
import austeretony.oxygen_teleportation.common.network.server.SPChangePointLockState;
import austeretony.oxygen_teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPEditWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPLeaveCampPoint;
import austeretony.oxygen_teleportation.common.network.server.SPManageInvitation;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToFavoriteCamp;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToPlayer;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToPoint;
import austeretony.oxygen_teleportation.common.network.server.SPRemoveWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPSetFavoriteCamp;
import austeretony.oxygen_teleportation.common.network.server.SPStartImageUpload;
import austeretony.oxygen_teleportation.common.network.server.SPRequestInvitationsSync;
import austeretony.oxygen_teleportation.common.network.server.SPUploadImagePart;
import austeretony.oxygen_teleportation.server.CampsSyncHandlerServer;
import austeretony.oxygen_teleportation.server.LocationsSyncHandlerServer;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import austeretony.oxygen_teleportation.server.command.TeleportationArgumentExecutorServer;
import austeretony.oxygen_teleportation.server.event.TeleportationEventsServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = TeleportationMain.MODID, 
        name = TeleportationMain.NAME, 
        version = TeleportationMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.9.5,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = TeleportationMain.VERSIONS_FORGE_URL)
public class TeleportationMain {

    public static final String 
    MODID = "oxygen_teleportation",    
    NAME = "Oxygen: Teleportation",
    VERSION = "0.9.1",
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Teleportation/info/mod_versions_forge.json";

    public static final int 
    TELEPORTATION_MOD_INDEX = 1,

    JUMP_PROFILE_SHARED_DATA_ID = 10,

    TELEPORTATION_REQUEST_ID = 10,
    INVITATION_TO_CAMP_ID = 11,

    CAMPS_DATA_ID = 10,
    LOCATIONS_DATA_ID = 11,

    TELEPORTATION_MENU_SCREEN_ID = 10,
    MANAGE_POINT_REQUEST_ID = 15,
    IMAGE_UPLOAD_REQUEST_ID = 16,
    TELEPORT_REQUEST_ID = 17;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperCommon.registerConfig(new TeleportationConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgumentExecutor(new TeleportationArgumentExecutorClient("teleportation", true));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        TeleportationManagerServer.create();
        CommonReference.registerEvent(new TeleportationEventsServer());
        RequestsFilterHelper.registerNetworkRequest(MANAGE_POINT_REQUEST_ID, 1);
        RequestsFilterHelper.registerNetworkRequest(IMAGE_UPLOAD_REQUEST_ID, 1);
        RequestsFilterHelper.registerNetworkRequest(TELEPORT_REQUEST_ID, 1);
        OxygenHelperServer.registerSharedDataValue(JUMP_PROFILE_SHARED_DATA_ID, Byte.BYTES);
        CommandOxygenServer.registerArgumentExecutor(new TeleportationArgumentExecutorServer("teleportation", true));
        OxygenHelperServer.registerDataSyncHandler(new CampsSyncHandlerServer());
        OxygenHelperServer.registerDataSyncHandler(new LocationsSyncHandlerServer());
        if (event.getSide() == Side.CLIENT) {
            TeleportationManagerClient.create();
            CommonReference.registerEvent(new TeleportationEventsClient());
            CommonReference.registerEvent(new TeleportationKeyHandler());
            if (!OxygenGUIHelper.isOxygenMenuEnabled())
                CommonReference.registerEvent(new TeleportationMenuKeyHandler());
            OxygenGUIHelper.registerScreenId(TELEPORTATION_MENU_SCREEN_ID);
            OxygenGUIHelper.registerContextAction(50, new TeleportToPlayerContextAction());
            OxygenGUIHelper.registerContextAction(60, new TeleportToPlayerContextAction());
            OxygenGUIHelper.registerContextAction(20, new TeleportToPlayerContextAction());//20 - group menu id
            OxygenGUIHelper.registerOxygenMenuEntry(TeleportationMenuGUIScreen.TELEPORTATIOIN_MENU_ENTRY);
            OxygenHelperClient.registerStatusMessagesHandler(new TeleportationStatusMessagesHandler());
            OxygenHelperClient.registerSharedDataSyncListener(TELEPORTATION_MENU_SCREEN_ID, 
                    ()->TeleportationManagerClient.instance().getTeleportationMenuManager().sharedDataSynchronized());
            OxygenHelperClient.registerDataSyncHandler(new CampsSyncHandlerClient());
            OxygenHelperClient.registerDataSyncHandler(new LocationsSyncHandlerClient());
        }
        EnumTeleportationPrivilege.register();
    }

    public static void addDefaultPrivileges() {
        if (!PrivilegeProviderServer.getGroup(PrivilegedGroupImpl.OPERATORS_GROUP.groupName).hasPrivilege(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString())) {
            PrivilegeProviderServer.addPrivileges(PrivilegedGroupImpl.OPERATORS_GROUP.groupName, true,  
                    new PrivilegeImpl(EnumTeleportationPrivilege.PROCESS_TELEPORTATION_ON_MOVE.toString(), true),
                    new PrivilegeImpl(EnumTeleportationPrivilege.ENABLE_MOVE_TO_LOCKED_LOCATIONS.toString(), true),
                    new PrivilegeImpl(EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.toString(), true),
                    new PrivilegeImpl(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), true),

                    new PrivilegeImpl(EnumTeleportationPrivilege.CAMP_TELEPORTATION_DELAY_SECONDS.toString(), 0),
                    new PrivilegeImpl(EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN_SECONDS.toString(), 0),

                    new PrivilegeImpl(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), true),
                    new PrivilegeImpl(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_DELAY_SECONDS.toString(), 0),
                    new PrivilegeImpl(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.toString(), 0),

                    new PrivilegeImpl(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_DELAY_SECONDS.toString(), 0),
                    new PrivilegeImpl(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.toString(), 0));
            LOGGER.info("Default <{}> group privileges added.", PrivilegedGroupImpl.OPERATORS_GROUP.groupName);
        }
    }

    private void initNetwork() {
        OxygenMain.network().registerPacket(CPStartImageDownload.class);
        OxygenMain.network().registerPacket(CPDownloadImagePart.class);
        OxygenMain.network().registerPacket(CPSyncCooldown.class);
        OxygenMain.network().registerPacket(CPSyncInvitedPlayers.class);
        OxygenMain.network().registerPacket(CPSyncAdditionalData.class);
        OxygenMain.network().registerPacket(CPSyncFeeItemStack.class);
        OxygenMain.network().registerPacket(CPWorldPointCreated.class);
        OxygenMain.network().registerPacket(CPWorldPointRemoved.class);
        OxygenMain.network().registerPacket(CPWorldPointEdited.class);
        OxygenMain.network().registerPacket(CPFavoriteCampUpdated.class);
        OxygenMain.network().registerPacket(CPPlayerUninvited.class);

        OxygenMain.network().registerPacket(SPStartImageUpload.class);
        OxygenMain.network().registerPacket(SPUploadImagePart.class);
        OxygenMain.network().registerPacket(SPRequestInvitationsSync.class);
        OxygenMain.network().registerPacket(SPCreateWorldPoint.class);
        OxygenMain.network().registerPacket(SPRemoveWorldPoint.class);
        OxygenMain.network().registerPacket(SPEditWorldPoint.class);
        OxygenMain.network().registerPacket(SPMoveToPoint.class);    
        OxygenMain.network().registerPacket(SPMoveToFavoriteCamp.class);
        OxygenMain.network().registerPacket(SPSetFavoriteCamp.class);
        OxygenMain.network().registerPacket(SPChangePointLockState.class);
        OxygenMain.network().registerPacket(SPChangeJumpProfile.class);
        OxygenMain.network().registerPacket(SPMoveToPlayer.class);
        OxygenMain.network().registerPacket(SPManageInvitation.class);
        OxygenMain.network().registerPacket(SPLeaveCampPoint.class);
    }
}
