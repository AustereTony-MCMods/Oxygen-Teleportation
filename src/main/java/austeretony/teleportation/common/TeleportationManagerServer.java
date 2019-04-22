package austeretony.teleportation.common;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.IOxygenTask;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationPlayerData;
import austeretony.teleportation.common.main.TeleportationWorldData;
import net.minecraft.entity.player.EntityPlayer;

public class TeleportationManagerServer {

    private static TeleportationManagerServer instance;

    //Data
    private final Map<UUID, TeleportationPlayerData> playersData = new ConcurrentHashMap<UUID, TeleportationPlayerData>();

    private final TeleportationWorldData worldData;

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
        this.worldData = new TeleportationWorldData();
        this.campsManager = new CampsManagerServer(this);
        this.campsLoader = new CampsLoaderServer(this);
        this.locationsManager = new LocationsManagerServer(this);
        this.locationsLoader = new LocationsLoaderServer(this);
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

    public boolean profileExist(UUID playerUUID) {
        return this.playersData.containsKey(playerUUID);
    }

    public void createPlayerProfile(UUID playerUUID) {
        this.playersData.put(playerUUID, new TeleportationPlayerData(playerUUID));
    }    

    public TeleportationPlayerData getPlayerProfile(UUID playerUUID) {
        return this.playersData.get(playerUUID);
    }

    public TeleportationWorldData getWorldData() {
        return this.worldData;
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
        } else
            this.appendSharedPlayerDataDelegated(playerUUID);
    }

    //TODO Need better solution. May be implement some queue for shared data attaching?
    private void appendSharedPlayerDataDelegated(UUID playerUUID) {
        OxygenHelperServer.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                appendSharedPlayerData(playerUUID);
            }
        });
    }

    public void appendSharedPlayerData(UUID playerUUID) {
        ByteBuffer byteBuff = ByteBuffer.allocate(Byte.BYTES);
        byteBuff.put((byte) this.getPlayerProfile(playerUUID).getJumpProfile().ordinal());
        OxygenHelperServer.getSharedPlayerData(playerUUID).addData(TeleportationMain.JUMP_PROFILE_DATA_ID, byteBuff);
    }

    public void updateSharedPlayerData(UUID playerUUID) {
        OxygenHelperServer.getSharedPlayerData(playerUUID).getData(TeleportationMain.JUMP_PROFILE_DATA_ID).put(0, (byte) this.getPlayerProfile(playerUUID).getJumpProfile().ordinal());
    }

    public void onPlayerLoggedOut(EntityPlayer player) {
        UUID playerUUID = CommonReference.uuid(player);
        OxygenHelperServer.setRequesting(playerUUID, false);
        OxygenHelperServer.setRequested(playerUUID, false); 
    }

    public void reset() {
        this.playersData.clear();
        this.worldData.resetData();
    }
}
