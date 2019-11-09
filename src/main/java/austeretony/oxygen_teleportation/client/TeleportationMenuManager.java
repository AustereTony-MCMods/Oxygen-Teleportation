package austeretony.oxygen_teleportation.client;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;
import austeretony.oxygen_teleportation.common.WorldPoint;

public class TeleportationMenuManager {

    private final TeleportationManagerClient manager;

    protected TeleportationMenuManager(TeleportationManagerClient manager) { 
        this.manager = manager;
    }

    public void openMenu() {
        ClientReference.getGameSettings().hideGUI = true;
        OxygenManagerClient.instance().getExecutionManager().scheduleTask(()->this.openMenuDelegated(), 100L, TimeUnit.MILLISECONDS);
    }

    private void openMenuDelegated() {
        ClientReference.delegateToClientThread(()->{
            this.manager.getImagesManager().preparePreviewImage();
            ClientReference.getGameSettings().hideGUI = false;
            ClientReference.displayGuiScreen(new TeleportationMenuGUIScreen());
        });
    }

    public void sharedDataSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).sharedDataSynchronized();
        }); 
    }

    public void campsSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).campsSynchronized();
        }); 
    }

    public void locationsSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).locationsSynchronized();
        }); 
    }

    public void cooldownSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).cooldownSynchronized();
        }); 
    }

    public void campCreated(WorldPoint worldPoint) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).campCreated(worldPoint);
        }); 
    }

    public void campEdited(long oldPointId, WorldPoint worldPoint, boolean updateImage) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).campEdited(oldPointId, worldPoint, updateImage);
        }); 
    }

    public void campRemoved(long pointId) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).campRemoved(pointId);
        }); 
    }

    public void favoriteCampSet(long pointId) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).favoriteCampSet(pointId);
        }); 
    }

    public void playerUninvited(long pointId, UUID playerUUID) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).playerUninvited(pointId, playerUUID);
        }); 
    }

    public void locationCreated(WorldPoint worldPoint) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).locationCreated(worldPoint);
        }); 
    }

    public void locationEdited(long oldPointId, WorldPoint worldPoint, boolean updateImage) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).locationEdited(oldPointId, worldPoint, updateImage);
        }); 
    }

    public void locationRemoved(long pointId) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((TeleportationMenuGUIScreen) ClientReference.getCurrentScreen()).locationRemoved(pointId);
        }); 
    }

    public static boolean isMenuOpened() {
        return ClientReference.hasActiveGUI() && ClientReference.getCurrentScreen() instanceof TeleportationMenuGUIScreen;
    }
}
