package austeretony.teleportation.client;

import austeretony.oxygen.client.reference.ClientReference;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.client.gui.menu.MenuGUIScreen;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.WorldProfile;
import austeretony.teleportation.common.network.server.SPRequest;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TeleportationManagerClient {

    private static TeleportationManagerClient instance;

    //Data
    private final PlayerProfile playerProfile;

    private final WorldProfile worldProfile;

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
        this.playerProfile = new PlayerProfile(OxygenHelperClient.getPlayerUUID());
        this.worldProfile = new WorldProfile();
        this.campsManager = new CampsManagerClient(this);
        this.campsLoader = new CampsLoaderClient(this);
        this.locationsManager = new LocationsManagerClient(this);
        this.locationsLoader = new LocationsLoaderClient(this);
        this.playersManager = new PlayersManagerClient(this);
        this.imagesManager = new ImagesManagerClient(this);
        this.imagesLoader = new ImagesLoaderClient(this);
    }

    public static void create() {
        instance = new TeleportationManagerClient();
    }

    public static TeleportationManagerClient instance() {
        return instance;
    }

    public PlayerProfile getPlayerProfile() {
        return this.playerProfile;
    }

    public WorldProfile getWorldProfile() {
        return this.worldProfile;
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
        ClientReference.displayGuiScreen(new MenuGUIScreen());
        this.campsLoader.savePlayerDataDelegated();
        this.imagesLoader.removeUnusedCampPreviewImagesDelegated();
        this.imagesLoader.removeUnusedLocationPreviewImagesDelegated();
    }
}
