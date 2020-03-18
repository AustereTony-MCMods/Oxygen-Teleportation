package austeretony.oxygen_teleportation.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_teleportation.client.input.TeleportationKeyHandler;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData;

public class TeleportationManagerClient {

    private static TeleportationManagerClient instance;

    //Players Data
    private TeleportationPlayerData playerData = new TeleportationPlayerData();

    private final PlayerDataManagerClient playerDataManager;

    //Shared Camps
    private final SharedCampsManagerContainer sharedCampsContainer;

    //Locations
    private final LocationsContainerClient locationsContainer = new LocationsContainerClient();

    private final LocationsManagerClient locationsManager;

    //Preview Images
    private final ImagesManagerClient imagesManager;

    private final ImagesLoaderClient imagesLoader;

    //Menu
    private final TeleportationMenuManager menuManager;

    private final TeleportationKeyHandler keyHandler = new TeleportationKeyHandler();

    //Fee
    private ItemStackWrapper feeStackWrapper;

    private TeleportationManagerClient() {
        this.playerDataManager = new PlayerDataManagerClient(this);
        this.sharedCampsContainer = new SharedCampsManagerContainer(this);
        this.locationsManager = new LocationsManagerClient(this);
        this.imagesManager = new ImagesManagerClient(this);
        this.imagesLoader = new ImagesLoaderClient(this);
        this.menuManager = new TeleportationMenuManager(this);
        CommonReference.registerEvent(this.keyHandler);
    }

    private void registerPersistentData() {
        OxygenHelperClient.registerPersistentData(this.playerData);
        OxygenHelperClient.registerPersistentData(this.sharedCampsContainer);
        OxygenHelperClient.registerPersistentData(this.locationsContainer);
    }

    public static void create() {
        if (instance == null) {
            instance = new TeleportationManagerClient();
            instance.registerPersistentData();
        }
    }

    public static TeleportationManagerClient instance() {
        return instance;
    }

    public TeleportationPlayerData getPlayerData() {
        return this.playerData;
    }

    public PlayerDataManagerClient getPlayerDataManager() {
        return this.playerDataManager;
    }

    public SharedCampsManagerContainer getSharedCampsContainer() {
        return this.sharedCampsContainer;
    }

    public LocationsContainerClient getLocationsContainer() {
        return this.locationsContainer;
    }

    public LocationsManagerClient getLocationsManager() {
        return this.locationsManager;
    }

    public ImagesManagerClient getImagesManager() {
        return this.imagesManager;
    }

    public ImagesLoaderClient getImagesLoader() {
        return this.imagesLoader;
    }

    public TeleportationMenuManager getTeleportationMenuManager() {
        return this.menuManager;
    }

    public TeleportationKeyHandler getKeyHandler() {
        return this.keyHandler;
    }

    public void worldLoaded() {
        this.playerData.setPlayerUUID(OxygenHelperClient.getPlayerUUID());
        this.playerData.setPath(OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/player_data.dat");
        OxygenHelperClient.loadPersistentDataAsync(this.playerData);

        OxygenHelperClient.loadPersistentDataAsync(this.sharedCampsContainer);
        OxygenHelperClient.loadPersistentDataAsync(this.locationsContainer);

        this.getImagesLoader().loadCampPreviewImagesAsync();
        this.getImagesLoader().loadLocationPreviewImagesAsync();   

        this.getImagesLoader().removeUnusedCampPreviewImagesAsync();
        this.getImagesLoader().removeUnusedLocationPreviewImagesAsync();
    }

    public void setFeeStack(ItemStackWrapper stackWrapper) {
        this.feeStackWrapper = stackWrapper;
    }

    public ItemStackWrapper getFeeStackWrapper() {
        return this.feeStackWrapper;
    }
}
