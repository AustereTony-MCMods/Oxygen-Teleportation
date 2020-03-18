package austeretony.oxygen_teleportation.common.main;

import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.settings.SettingsScreen;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.privilege.PrivilegeUtils;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.command.CommandOxygenOperator;
import austeretony.oxygen_core.server.network.NetworkRequestsRegistryServer;
import austeretony.oxygen_teleportation.client.CampsSyncHandlerClient;
import austeretony.oxygen_teleportation.client.LocationsSyncHandlerClient;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.TeleportationStatusMessagesHandler;
import austeretony.oxygen_teleportation.client.command.TeleportationArgumentClient;
import austeretony.oxygen_teleportation.client.event.TeleportationEventsClient;
import austeretony.oxygen_teleportation.client.gui.context.TeleportToPlayerContextAction;
import austeretony.oxygen_teleportation.client.gui.settings.TeleportationSettingsContainer;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuScreen;
import austeretony.oxygen_teleportation.client.settings.EnumTeleportationClientSetting;
import austeretony.oxygen_teleportation.client.settings.gui.EnumTeleportationGUISetting;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.network.client.CPDownloadPreviewImage;
import austeretony.oxygen_teleportation.common.network.client.CPFavoriteCampUpdated;
import austeretony.oxygen_teleportation.common.network.client.CPPlayerUninvited;
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
import austeretony.oxygen_teleportation.common.network.server.SPRequestInvitationsSync;
import austeretony.oxygen_teleportation.common.network.server.SPSetFavoriteCamp;
import austeretony.oxygen_teleportation.common.network.server.SPStartImageUpload;
import austeretony.oxygen_teleportation.common.network.server.SPUploadImagePart;
import austeretony.oxygen_teleportation.server.CampsSyncHandlerServer;
import austeretony.oxygen_teleportation.server.LocationsSyncHandlerServer;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import austeretony.oxygen_teleportation.server.command.TeleportationArgumentOperator;
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
        dependencies = "required-after:oxygen_core@[0.11.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = TeleportationMain.VERSIONS_FORGE_URL)
public class TeleportationMain {

    public static final String 
    MODID = "oxygen_teleportation",    
    NAME = "Oxygen: Teleportation",
    VERSION = "0.11.0",
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Teleportation/info/mod_versions_forge.json";

    public static final int 
    TELEPORTATION_MOD_INDEX = 1,

    JUMP_PROFILE_SHARED_DATA_ID = 5,

    TELEPORTATION_REQUEST_ID = 10,
    INVITATION_TO_CAMP_ID = 11,

    CAMPS_DATA_ID = 10,
    LOCATIONS_DATA_ID = 11,

    TELEPORTATION_MENU_SCREEN_ID = 10,

    MANAGE_POINT_REQUEST_ID = 15,
    IMAGE_UPLOAD_REQUEST_ID = 16,
    TELEPORT_REQUEST_ID = 17;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperCommon.registerConfig(new TeleportationConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgument(new TeleportationArgumentClient());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        TeleportationManagerServer.create();
        CommonReference.registerEvent(new TeleportationEventsServer());
        NetworkRequestsRegistryServer.registerRequest(MANAGE_POINT_REQUEST_ID, 1000);
        NetworkRequestsRegistryServer.registerRequest(IMAGE_UPLOAD_REQUEST_ID, 1000);
        NetworkRequestsRegistryServer.registerRequest(TELEPORT_REQUEST_ID, 1000);
        OxygenHelperServer.registerSharedDataValue(JUMP_PROFILE_SHARED_DATA_ID, Byte.BYTES);
        CommandOxygenOperator.registerArgument(new TeleportationArgumentOperator());
        OxygenHelperServer.registerDataSyncHandler(new CampsSyncHandlerServer());
        OxygenHelperServer.registerDataSyncHandler(new LocationsSyncHandlerServer());
        EnumTeleportationPrivilege.register();
        if (event.getSide() == Side.CLIENT) {
            TeleportationManagerClient.create();
            CommonReference.registerEvent(new TeleportationEventsClient());
            OxygenGUIHelper.registerScreenId(TELEPORTATION_MENU_SCREEN_ID);
            OxygenGUIHelper.registerContextAction(20, new TeleportToPlayerContextAction());//20 - group menu id
            OxygenGUIHelper.registerContextAction(50, new TeleportToPlayerContextAction());
            OxygenGUIHelper.registerContextAction(60, new TeleportToPlayerContextAction());
            OxygenGUIHelper.registerContextAction(110, new TeleportToPlayerContextAction());//110 - guild menu id
            OxygenGUIHelper.registerOxygenMenuEntry(TeleportationMenuScreen.TELEPORTATIOIN_MENU_ENTRY);
            OxygenHelperClient.registerStatusMessagesHandler(new TeleportationStatusMessagesHandler());
            OxygenHelperClient.registerSharedDataSyncListener(TELEPORTATION_MENU_SCREEN_ID, TeleportationManagerClient.instance().getTeleportationMenuManager()::sharedDataSynchronized);
            OxygenHelperClient.registerDataSyncHandler(new CampsSyncHandlerClient());
            OxygenHelperClient.registerDataSyncHandler(new LocationsSyncHandlerClient());
            EnumTeleportationClientSetting.register();
            EnumTeleportationGUISetting.register();
            SettingsScreen.registerSettingsContainer(new TeleportationSettingsContainer());
        }
    }

    public static void addDefaultPrivileges() {
        if (PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).getPrivilege(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.id()) == null) {
            PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).addPrivileges(  
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.PROCESS_TELEPORTATION_ON_MOVE.id(), true),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.id(), true),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_LOCKED_LOCATIONS.id(), true),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.id(), true),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), true),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.ALLOW_LOCATIONS_USAGE.id(), true),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.ALLOW_PLAYER_TELEPORTATION_USAGE.id(), true),

                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.CAMP_TELEPORTATION_DELAY_SECONDS.id(), 0),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN_SECONDS.id(), 0),

                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.id(), true),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_DELAY_SECONDS.id(), 0),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.id(), 0),

                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_DELAY_SECONDS.id(), 0),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.id(), 0),

                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.CAMP_TELEPORTATION_FEE.id(), 0L),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_FEE.id(), 0L),
                    PrivilegeUtils.getPrivilege(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_FEE.id(), 0L));
            OxygenManagerServer.instance().getPrivilegesContainer().markChanged();
            OxygenMain.LOGGER.info("[Teleportation] Default Operator role privileges added.");
        }
    }

    private void initNetwork() {
        OxygenMain.network().registerPacket(CPDownloadPreviewImage.class);
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
