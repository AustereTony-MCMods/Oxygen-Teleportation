package austeretony.oxygen_teleportation.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.main.TeleportationWorldData;
import net.minecraft.entity.player.EntityPlayer;

public class TeleportationManagerServer {

    private static TeleportationManagerServer instance;

    //Data
    private final Map<UUID, TeleportationPlayerData> playersData = new ConcurrentHashMap<UUID, TeleportationPlayerData>();

    private final TeleportationWorldData worldData;

    //Camps
    private final CampsManagerServer campsManager;

    private final SharedCampsManagerServer sharedCampsManager;

    //Locations
    private final LocationsManagerServer locationsManager;

    //Players
    private final PlayersManagerServer playersManager;

    //Images
    private final ImagesManagerServer imagesManager;

    private final ImagesLoaderServer imagesLoader;

    private final Set<UUID> teleportations = new HashSet<UUID>();

    private TeleportationManagerServer() {
        this.worldData = new TeleportationWorldData();
        this.campsManager = new CampsManagerServer(this);
        this.sharedCampsManager = new SharedCampsManagerServer(this);
        this.locationsManager = new LocationsManagerServer(this);
        this.playersManager = new PlayersManagerServer(this);
        this.imagesManager = new ImagesManagerServer(this);
        this.imagesLoader = new ImagesLoaderServer(this);
    }

    public static void create() {
        if (instance == null) 
            instance = new TeleportationManagerServer();
    }

    public static TeleportationManagerServer instance() {
        return instance;
    }

    public Collection<TeleportationPlayerData> getPlayersData() {
        return this.playersData.values();
    }

    public boolean dataExist(UUID playerUUID) {
        return this.playersData.containsKey(playerUUID);
    }

    public void createPlayerData(UUID playerUUID) {
        this.playersData.put(playerUUID, new TeleportationPlayerData(playerUUID));
    }    

    public TeleportationPlayerData getPlayerData(UUID playerUUID) {
        return this.playersData.get(playerUUID);
    }

    public TeleportationWorldData getWorldData() {
        return this.worldData;
    }  

    public CampsManagerServer getCampsManager() {
        return this.campsManager;
    }

    public SharedCampsManagerServer getSharedCampsManager() {
        return this.sharedCampsManager;
    }  

    public LocationsManagerServer getLocationsManager() {
        return this.locationsManager;
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

    //TODO onPlayerLoaded()
    public void onPlayerLoaded(EntityPlayer player) {
        UUID playerUUID = CommonReference.getPersistentUUID(player);
        if (!this.dataExist(playerUUID)) {
            this.createPlayerData(playerUUID);
            OxygenHelperServer.loadPersistentDataDelegated(this.getPlayerData(playerUUID));
        }
        this.updateSharedPlayerData(playerUUID);
    }

    public void updateSharedPlayerData(UUID playerUUID) {
        OxygenHelperServer.getSharedPlayerData(playerUUID).setByte(TeleportationMain.JUMP_PROFILE_SHARED_DATA_ID, this.getPlayerData(playerUUID).getJumpProfile().ordinal());
    }

    public void reset() {
        this.playersData.clear();
        this.worldData.reset();
        this.sharedCampsManager.reset();
    }
}
