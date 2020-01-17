package austeretony.oxygen_teleportation.server;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportationManagerServer {

    private static TeleportationManagerServer instance;

    //Players Data
    private final PlayersDataContainerServer playersDataContainer = new PlayersDataContainerServer();

    private final PlayersDataManagerServer playersDataManager;

    //Shared Camps
    private final SharedCampsContainerServer sharedCampsContainer = new SharedCampsContainerServer();

    //Locations
    private final LocationsContainerServer locationsContainer = new LocationsContainerServer();

    private final LocationsManagerServer locationsManager;

    //Preview Images
    private final ImagesManagerServer imagesManager;

    private final ImagesLoaderServer imagesLoader;

    //Fee
    private ItemStackWrapper feeStackWrapper;

    private TeleportationManagerServer() {
        this.playersDataManager = new PlayersDataManagerServer(this);
        this.locationsManager = new LocationsManagerServer(this);
        this.imagesManager = new ImagesManagerServer(this);
        this.imagesLoader = new ImagesLoaderServer(this);
    }

    private void registerPersistentData() {
        OxygenHelperServer.registerPersistentData(()->this.playersDataContainer.save());
        OxygenHelperServer.registerPersistentData(this.sharedCampsContainer);
        OxygenHelperServer.registerPersistentData(this.locationsContainer);
    }

    public static void create() {
        if (instance == null) {
            instance = new TeleportationManagerServer();
            instance.registerPersistentData();
        }
    }

    public static TeleportationManagerServer instance() {
        return instance;
    }

    public PlayersDataContainerServer getPlayersDataContainer() {
        return this.playersDataContainer;
    }  

    public PlayersDataManagerServer getPlayersDataManager() {
        return this.playersDataManager;
    }  

    public SharedCampsContainerServer getSharedCampsContainer() {
        return this.sharedCampsContainer;
    }   

    public LocationsContainerServer getLocationsContainer() {
        return this.locationsContainer;
    }  

    public LocationsManagerServer getLocationsManager() {
        return this.locationsManager;
    }  

    public ImagesManagerServer getImagesManager() {
        return this.imagesManager;
    }

    public ImagesLoaderServer getImagesLoader() {
        return this.imagesLoader;
    }

    public void worldLoaded() {
        OxygenHelperServer.loadPersistentDataAsync(this.sharedCampsContainer);
        OxygenHelperServer.loadPersistentDataAsync(this.locationsContainer);
        if (TeleportationConfig.FEE_MODE.asInt() == 1)
            TeleportationLoaderServer.loadFeeItemStackAsync();
        this.imagesLoader.loadLocationPreviewImagesAsync();
    }

    public void onPlayerLoaded(EntityPlayerMP playerMP) {
        this.playersDataManager.onPlayerLoaded(playerMP);  
    }

    public void onPlayerUnloaded(EntityPlayerMP playerMP) {
        this.playersDataManager.onPlayerUnloaded(playerMP);
    }

    public void setFeeStack(ItemStackWrapper stackWrapper) {
        this.feeStackWrapper = stackWrapper;
    }

    public ItemStackWrapper getFeeStackWrapper() {
        return this.feeStackWrapper;
    }
}
