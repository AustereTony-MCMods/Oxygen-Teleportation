package austeretony.oxygen_teleportation.client;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen.common.itemstack.ItemStackWrapper;
import austeretony.oxygen.common.main.EnumOxygenPrivilege;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.main.SharedPlayerData;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.main.TeleportationWorldData;

public class TeleportationManagerClient {

    private static TeleportationManagerClient instance;

    //Data      
    private TeleportationPlayerData playerData;

    private final TeleportationWorldData worldData;

    //Camps
    private final CampsManagerClient campsManager;

    private final SharedCampsManagerClient sharedCampsManager;

    //Locations
    private final LocationsManagerClient locationsManager;

    //Players
    private final PlayersManagerClient playersManager;

    //Preview Images
    private final ImagesManagerClient imagesManager;

    private final ImagesLoaderClient imagesLoader;

    private long time, delay;

    private ItemStackWrapper feeStackWrapper;

    private TeleportationManagerClient() {
        this.worldData = new TeleportationWorldData();
        this.campsManager = new CampsManagerClient(this);
        this.sharedCampsManager = new SharedCampsManagerClient(this);
        this.locationsManager = new LocationsManagerClient(this);
        this.playersManager = new PlayersManagerClient(this);
        this.imagesManager = new ImagesManagerClient(this);
        this.imagesLoader = new ImagesLoaderClient(this);
    }

    public static void create() {
        if (instance == null) 
            instance = new TeleportationManagerClient();
    }

    public static TeleportationManagerClient instance() {
        return instance;
    }

    public void init() {
        this.reset();
        this.playerData = new TeleportationPlayerData(OxygenHelperClient.getPlayerUUID());

        OxygenHelperClient.loadPersistentDataDelegated(TeleportationManagerClient.instance().getPlayerData());
        this.getImagesLoader().loadCampPreviewImagesDelegated();

        OxygenHelperClient.loadPersistentDataDelegated(TeleportationManagerClient.instance().getSharedCampsManager());

        OxygenHelperClient.loadPersistentDataDelegated(TeleportationManagerClient.instance().getWorldData());
        this.getImagesLoader().loadLocationPreviewImagesDelegated();   

        this.getImagesLoader().removeUnusedCampPreviewImagesDelegated();
        this.getImagesLoader().removeUnusedLocationPreviewImagesDelegated();
    }

    public TeleportationPlayerData getPlayerData() {
        return this.playerData;
    }

    public TeleportationWorldData getWorldData() {
        return this.worldData;
    }

    public CampsManagerClient getCampsManager() {
        return this.campsManager;
    }

    public SharedCampsManagerClient getSharedCampsManager() {
        return this.sharedCampsManager;
    }

    public LocationsManagerClient getLocationsManager() {
        return this.locationsManager;
    }

    public PlayersManagerClient getPlayersManager() {
        return this.playersManager;
    }

    public ImagesManagerClient getImagesManager() {
        return this.imagesManager;
    }

    public ImagesLoaderClient getImagesLoader() {
        return this.imagesLoader;
    }

    public boolean teleporting() {
        return System.currentTimeMillis() < this.time + this.delay;
    }

    public void setTeleportationDelay(long delay) {
        this.delay = delay * 1000;
        this.time = System.currentTimeMillis();
    }

    public void setFeeStack(ItemStackWrapper stackWrapper) {
        this.feeStackWrapper = stackWrapper;
    }

    public ItemStackWrapper getFeeStackWrapper() {
        return this.feeStackWrapper;
    }

    public static boolean isPlayerAvailable(String username) {
        if (username.equals(OxygenHelperClient.getSharedClientPlayerData().getUsername()))
            return false;
        SharedPlayerData sharedData = OxygenHelperClient.getSharedPlayerData(username);
        if (sharedData != null) {
            if ((OxygenHelperClient.getPlayerStatus(sharedData) != OxygenPlayerData.EnumActivityStatus.OFFLINE || PrivilegeProviderClient.getPrivilegeValue(EnumOxygenPrivilege.EXPOSE_PLAYERS_OFFLINE.toString(), false)))
                return true;
        }
        return false;
    }

    public void reset() {
        if (this.playerData != null)
            this.playerData.reset();
        this.worldData.reset();
        this.sharedCampsManager.reset();
    }
}
