package austeretony.teleportation.client;

import austeretony.oxygen.common.core.api.ClientReference;
import austeretony.teleportation.client.gui.menu.TeleportationMenuGUIScreen;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationPlayerData;
import austeretony.teleportation.common.main.TeleportationWorldData;
import austeretony.teleportation.common.network.server.SPRequest;

public class TeleportationManagerClient {

    private static TeleportationManagerClient instance;

    //Data
    private final TeleportationPlayerData playerData;

    private final TeleportationWorldData worldData;

    //Camps
    private final CampsManagerClient campsManager;

    private final CampsLoaderClient campsLoader;

    //Locations
    private final LocationsManagerClient locationsManager;

    private final LocationsLoaderClient locationsLoader;

    //Players
    private final PlayersManagerClient playersManager;

    //Preview Images
    private final ImagesManagerClient imagesManager;

    private final ImagesLoaderClient imagesLoader;

    private long time, delay;

    private TeleportationManagerClient() {
        this.playerData = new TeleportationPlayerData();
        this.worldData = new TeleportationWorldData();
        this.campsManager = new CampsManagerClient(this);
        this.campsLoader = new CampsLoaderClient(this);
        this.locationsManager = new LocationsManagerClient(this);
        this.locationsLoader = new LocationsLoaderClient(this);
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

    public TeleportationPlayerData getPlayerData() {
        return this.playerData;
    }

    public TeleportationWorldData getWorldProfile() {
        return this.worldData;
    }

    public CampsManagerClient getCampsManager() {
        return this.campsManager;
    }

    public CampsLoaderClient getCampsLoader() {
        return this.campsLoader;
    }

    public LocationsManagerClient getLocationsManager() {
        return this.locationsManager;
    }

    public LocationsLoaderClient getLocationsLoader() {
        return this.locationsLoader;
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

    public void openMenuSynced() {
        if (!this.teleporting()) {
            ClientReference.getMinecraft().gameSettings.hideGUI = true;
            TeleportationMain.network().sendToServer(new SPRequest(SPRequest.EnumRequest.OPEN_MENU));
        }
    }

    public boolean teleporting() {
        return System.currentTimeMillis() < this.time + this.delay;
    }

    public void setTeleportationDelay(long delay) {
        this.delay = delay * 1000;
        this.time = System.currentTimeMillis();
    }

    public void openMenuDelegated() {
        ClientReference.getMinecraft().addScheduledTask(new Runnable() {

            @Override
            public void run() {
                openMenu();
            }
        });
    }

    public void openMenu() {
        this.getImagesManager().preparePreviewImage();
        ClientReference.getMinecraft().gameSettings.hideGUI = false;
        ClientReference.displayGuiScreen(new TeleportationMenuGUIScreen());
        this.imagesLoader.removeUnusedCampPreviewImagesDelegated();
        this.imagesLoader.removeUnusedLocationPreviewImagesDelegated();
    }

    public void reset() {
        this.playerData.resetData();
        this.worldData.resetData();
    }
}
