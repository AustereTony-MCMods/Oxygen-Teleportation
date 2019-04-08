package austeretony.teleportation.common;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.WorldProfile;
import net.minecraft.entity.player.EntityPlayer;

public class TeleportationManagerServer {

    private static TeleportationManagerServer instance;

    //Data
    private final Map<UUID, PlayerProfile> playersProfiles = new ConcurrentHashMap<UUID, PlayerProfile>();

    private final WorldProfile worldProfile;

    //Camps
    private final CampsManagerServer campsManager;

    private final CampsLoaderServer campsLoader;

    //Locations
    private final LocationsManagerServer locationsManager;

    private final LocationsLoaderServer locationsLoader;

    //Players
    private final PlayersManagerServer playersManager;

    //Images
    private final ImagesManagerServer imagesManager;

    private final ImagesLoaderServer imagesLoader;

    private final Set<UUID> teleportations = new HashSet<UUID>();

    private TeleportationManagerServer() {
        this.worldProfile = new WorldProfile();
        this.campsManager = new CampsManagerServer(this);
        this.campsLoader = new CampsLoaderServer(this);
        this.locationsManager = new LocationsManagerServer(this);
        this.locationsLoader = new LocationsLoaderServer(this);
        this.playersManager = new PlayersManagerServer(this);
        this.imagesManager = new ImagesManagerServer(this);
        this.imagesLoader = new ImagesLoaderServer(this);
    }

    public static void create() {
        instance = new TeleportationManagerServer();
    }

    public static TeleportationManagerServer instance() {
        return instance;
    }

    public Map<UUID, PlayerProfile> getPlayersProfiles() {
        return this.playersProfiles;
    }

    public boolean profileExist(UUID playerUUID) {
        return this.playersProfiles.containsKey(playerUUID);
    }

    public void createPlayerProfile(UUID playerUUID) {
        this.getPlayersProfiles().put(playerUUID, new PlayerProfile(playerUUID));
    }    

    public PlayerProfile getPlayerProfile(UUID playerUUID) {
        return this.playersProfiles.get(playerUUID);
    }

    public WorldProfile getWorldProfile() {
        return this.worldProfile;
    }  

    public CampsManagerServer getCampsManager() {
        return this.campsManager;
    }

    public CampsLoaderServer getCampsLoader() {
        return this.campsLoader;
    }

    public LocationsManagerServer getLocationsManager() {
        return this.locationsManager;
    }

    public LocationsLoaderServer getLocationsLoader() {
        return this.locationsLoader;
    }

    public PlayersManagerServer getPlayersManager() {
        return this.playersManager;
    }

    public ImagesManagerServer getImagesManager() {
        return this.imagesManager;
    }

    public ImagesLoaderServer getImagesLoader() {
        return this.imagesLoader;
    }

    public Set<UUID> getTeleportations() {
        return this.teleportations;
    }

    public void onPlayerLoggedIn(EntityPlayer player) {
        UUID playerUUID = CommonReference.uuid(player);
        if (!this.profileExist(playerUUID)) {
            this.createPlayerProfile(playerUUID);
            this.campsLoader.loadPlayerDataDelegated(playerUUID);
        }
        if (this.profileExist(playerUUID))
            this.appendAdditionalPlayerData(playerUUID);
    }

    public void appendAdditionalPlayerData(UUID playerUUID) {
        ByteBuffer byteBuff = ByteBuffer.allocate(1);
        byteBuff.put((byte) this.getPlayerProfile(playerUUID).getJumpProfile().ordinal());
        OxygenHelperServer.getPlayerData(playerUUID).addData(TeleportationMain.JUMP_PROFILE_DATA_ID, byteBuff);
    }

    public void updateAdditionalPlayerData(UUID playerUUID) {
        OxygenHelperServer.getPlayerData(playerUUID).getData(TeleportationMain.JUMP_PROFILE_DATA_ID).put(0, (byte) this.getPlayerProfile(playerUUID).getJumpProfile().ordinal());
    }

    public void onPlayerLoggedOut(EntityPlayer player) {
        UUID playerUUID = CommonReference.uuid(player);
        this.campsManager.resetInviting(playerUUID);
        this.playersManager.resetRequest(playerUUID);
        this.playersManager.resetRequesting(playerUUID);
    }
}
