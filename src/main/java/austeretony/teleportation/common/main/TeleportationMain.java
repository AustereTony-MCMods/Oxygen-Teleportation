package austeretony.teleportation.common.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.network.OxygenNetworkHandler;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.client.gui.overlay.NotificationRenderer;
import austeretony.teleportation.client.handler.KeyHandler;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.menu.locations.LocationsManagerServer;
import austeretony.teleportation.common.menu.players.PlayersManagerServer;
import austeretony.teleportation.common.network.client.CPCommand;
import austeretony.teleportation.common.network.client.CPDownloadImagePart;
import austeretony.teleportation.common.network.client.CPSendJumpRequest;
import austeretony.teleportation.common.network.client.CPStartImageDownload;
import austeretony.teleportation.common.network.client.CPSyncCooldown;
import austeretony.teleportation.common.network.client.CPSyncPoints;
import austeretony.teleportation.common.network.client.CPSyncValidPointIds;
import austeretony.teleportation.common.network.server.SPAbsentPoints;
import austeretony.teleportation.common.network.server.SPChangeJumpProfile;
import austeretony.teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.teleportation.common.network.server.SPEditWorldPoint;
import austeretony.teleportation.common.network.server.SPJumpRequestReply;
import austeretony.teleportation.common.network.server.SPLockPoint;
import austeretony.teleportation.common.network.server.SPMoveToPlayer;
import austeretony.teleportation.common.network.server.SPMoveToPoint;
import austeretony.teleportation.common.network.server.SPRemoveWorldPoint;
import austeretony.teleportation.common.network.server.SPRequest;
import austeretony.teleportation.common.network.server.SPSetFavoriteCamp;
import austeretony.teleportation.common.network.server.SPStartImageUpload;
import austeretony.teleportation.common.network.server.SPUploadImagePart;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = TeleportationMain.MODID, 
        name = TeleportationMain.NAME, 
        version = TeleportationMain.VERSION,
        dependencies = "required-after:oxygen",//TODO specify version
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = TeleportationMain.VERSIONS_FORGE_URL)
public class TeleportationMain {

    public static final String 
    MODID = "teleportation",
    NAME = "Teleportation",
    VERSION = "0.1.0",
    VERSION_CUSTOM = VERSION + ":alpha:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Teleportation/info/mod_versions_forge.json";

    public static final int 
    TELEPORTATION_MOD_INDEX = 1,
    JUMP_PROFILE_DATA_ID = 1;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static OxygenNetworkHandler network;

    static {
        OxygenHelperServer.loadConfig(CommonReference.getGameFolder() + "/config/oxygen/teleportation/config.json", 
                "assets/teleportation/config.json", new TeleportationConfig());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) { 
        this.initNetwork();
        if (event.getSide() == Side.CLIENT) {
            CommonReference.registerEvent(new KeyHandler());
            CommonReference.registerEvent(new NotificationRenderer());
            OxygenHelperClient.registerChatMessageInfoListener(new TeleportationChatMessagesListener());
        }
        CommonReference.registerEvent(new TeleportationEvents());
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) { 
        CampsManagerServer.create();
        LocationsManagerServer.create();
        PlayersManagerServer.create();
    }

    private void initNetwork() {
        network = OxygenHelperServer.createNetworkHandler("oxygen:" + MODID);

        network.register(CPCommand.class);
        network.register(CPSyncValidPointIds.class);
        network.register(CPSyncPoints.class);
        network.register(CPStartImageDownload.class);
        network.register(CPDownloadImagePart.class);
        network.register(CPSyncCooldown.class);
        network.register(CPSendJumpRequest.class);

        network.register(SPRequest.class);
        network.register(SPAbsentPoints.class);
        network.register(SPCreateWorldPoint.class);
        network.register(SPRemoveWorldPoint.class);
        network.register(SPEditWorldPoint.class);
        network.register(SPStartImageUpload.class);
        network.register(SPUploadImagePart.class);
        network.register(SPMoveToPoint.class);
        network.register(SPSetFavoriteCamp.class);
        network.register(SPLockPoint.class);
        network.register(SPChangeJumpProfile.class);
        network.register(SPMoveToPlayer.class);
        network.register(SPJumpRequestReply.class);
    }

    public static OxygenNetworkHandler network() {
        return network;
    }
}
