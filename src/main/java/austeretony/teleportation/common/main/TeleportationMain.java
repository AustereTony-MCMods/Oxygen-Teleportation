package austeretony.teleportation.common.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import austeretony.oxygen.common.api.IOxygenTask;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.network.OxygenNetwork;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen.common.privilege.api.Privilege;
import austeretony.oxygen.common.privilege.api.PrivilegedGroup;
import austeretony.teleportation.client.handler.TeleportationKeyHandler;
import austeretony.teleportation.client.listener.TeleportationListenerClient;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.listener.TeleportationListenerServer;
import austeretony.teleportation.common.network.client.CPCommand;
import austeretony.teleportation.common.network.client.CPDownloadImagePart;
import austeretony.teleportation.common.network.client.CPStartImageDownload;
import austeretony.teleportation.common.network.client.CPSyncCooldown;
import austeretony.teleportation.common.network.client.CPSyncInvitedPlayers;
import austeretony.teleportation.common.network.client.CPSyncValidWorldPointsIds;
import austeretony.teleportation.common.network.client.CPSyncWorldPoints;
import austeretony.teleportation.common.network.server.SPSendAbsentPointsIds;
import austeretony.teleportation.common.network.server.SPChangeJumpProfile;
import austeretony.teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.teleportation.common.network.server.SPEditWorldPoint;
import austeretony.teleportation.common.network.server.SPLeaveCampPoint;
import austeretony.teleportation.common.network.server.SPLockPoint;
import austeretony.teleportation.common.network.server.SPManageInvitation;
import austeretony.teleportation.common.network.server.SPMoveToPlayer;
import austeretony.teleportation.common.network.server.SPMoveToPoint;
import austeretony.teleportation.common.network.server.SPRemoveWorldPoint;
import austeretony.teleportation.common.network.server.SPRequest;
import austeretony.teleportation.common.network.server.SPSetFavoriteCamp;
import austeretony.teleportation.common.network.server.SPStartImageUpload;
import austeretony.teleportation.common.network.server.SPUploadImagePart;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = TeleportationMain.MODID, 
        name = TeleportationMain.NAME, 
        version = TeleportationMain.VERSION,
        dependencies = "required-after:oxygen@[0.3.0,);",//TODO always check required core version before build
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = TeleportationMain.VERSIONS_FORGE_URL)
public class TeleportationMain {

    public static final String 
    MODID = "teleportation",
    NAME = "Teleportation",
    VERSION = "0.3.0",
    VERSION_CUSTOM = VERSION + ":alpha:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Teleportation/info/mod_versions_forge.json";

    public static final int 
    TELEPORTATION_MOD_INDEX = 1,
    JUMP_PROFILE_DATA_ID = 10,
    TELEPORTATION_REQUEST_ID = 10,
    INVITATION_TO_CAMP_ID = 11;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static OxygenNetwork network;

    static {
        OxygenHelperServer.registerConfig(new TeleportationConfig());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();

        TeleportationListenerServer listenerServer = new TeleportationListenerServer();
        OxygenHelperServer.registerPlayerLogInListener(listenerServer);
        OxygenHelperServer.registerPlayerLogOutListener(listenerServer);

        if (event.getSide() == Side.CLIENT) {
            CommonReference.registerEvent(new TeleportationKeyHandler());

            TeleportationListenerClient listenerClient = new TeleportationListenerClient();
            OxygenHelperClient.registerClientInitListener(listenerClient);
            OxygenHelperClient.registerChatMessageInfoListener(listenerClient);

            OxygenHelperClient.registerNotificationIcon(TELEPORTATION_REQUEST_ID, new ResourceLocation(MODID, "textures/gui/notifications/teleportation_request_icon.png"));
            OxygenHelperClient.registerNotificationIcon(INVITATION_TO_CAMP_ID, new ResourceLocation(OxygenMain.MODID, "textures/gui/invitation_request_icon.png"));
        }
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) { 
        TeleportationManagerServer.create();
        TeleportationManagerServer.instance().getLocationsLoader().loadLocationsDataDelegated();
        this.addPrivilegesDelegated();
    }

    //TODO Need better solution (queue or something).
    private void addPrivilegesDelegated() {
        OxygenHelperServer.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                if (!PrivilegedGroup.OPERATORS_GROUP.hasPrivilege(EnumPrivileges.LOCATIONS_MANAGEMENT.toString()))
                    PrivilegedGroup.OPERATORS_GROUP.addPrivileges(true, 
                            new Privilege(EnumPrivileges.PROCESS_TELEPORTATION_ON_MOVE.toString()),
                            new Privilege(EnumPrivileges.ENABLE_MOVE_TO_LOCKED_LOCATIONS.toString()),
                            new Privilege(EnumPrivileges.ENABLE_CROSS_DIM_TELEPORTATION.toString()),
                            new Privilege(EnumPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString()),

                            new Privilege(EnumPrivileges.CAMP_TELEPORTATION_DELAY.toString(), 0),
                            new Privilege(EnumPrivileges.CAMP_TELEPORTATION_COOLDOWN.toString(), 0),

                            new Privilege(EnumPrivileges.LOCATIONS_MANAGEMENT.toString()),
                            new Privilege(EnumPrivileges.LOCATION_TELEPORTATION_DELAY.toString(), 0),
                            new Privilege(EnumPrivileges.LOCATION_TELEPORTATION_COOLDOWN.toString(), 0),

                            new Privilege(EnumPrivileges.PLAYER_TELEPORTATION_DELAY.toString(), 0),
                            new Privilege(EnumPrivileges.PLAYER_TELEPORTATION_COOLDOWN.toString(), 0));
            }
        });
    }

    private void initNetwork() {
        network = OxygenHelperServer.createNetworkHandler("oxygen:" + MODID);

        network.registerPacket(CPCommand.class);
        network.registerPacket(CPSyncValidWorldPointsIds.class);
        network.registerPacket(CPSyncWorldPoints.class);
        network.registerPacket(CPStartImageDownload.class);
        network.registerPacket(CPDownloadImagePart.class);
        network.registerPacket(CPSyncCooldown.class);
        network.registerPacket(CPSyncInvitedPlayers.class);

        network.registerPacket(SPRequest.class);
        network.registerPacket(SPSendAbsentPointsIds.class);
        network.registerPacket(SPCreateWorldPoint.class);
        network.registerPacket(SPRemoveWorldPoint.class);
        network.registerPacket(SPEditWorldPoint.class);
        network.registerPacket(SPStartImageUpload.class);
        network.registerPacket(SPUploadImagePart.class);
        network.registerPacket(SPMoveToPoint.class);
        network.registerPacket(SPSetFavoriteCamp.class);
        network.registerPacket(SPLockPoint.class);
        network.registerPacket(SPChangeJumpProfile.class);
        network.registerPacket(SPMoveToPlayer.class);
        network.registerPacket(SPManageInvitation.class);
        network.registerPacket(SPLeaveCampPoint.class);
    }

    public static OxygenNetwork network() {
        return network;
    }
}
