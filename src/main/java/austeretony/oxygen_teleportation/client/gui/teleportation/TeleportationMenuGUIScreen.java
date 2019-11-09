package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.UUID;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.common.inventory.InventoryHelper;
import austeretony.oxygen_core.server.OxygenPlayerData;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class TeleportationMenuGUIScreen extends AbstractGUIScreen {

    public static final OxygenMenuEntry TELEPORTATIOIN_MENU_ENTRY = new TeleportationMenuEntry();

    private CampsGUISection campsSection;

    private LocationsGUISection locationsSection;

    private PlayersGUISection playersSection;

    public final long balance;

    public TeleportationMenuGUIScreen() {
        OxygenHelperClient.syncSharedData(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);

        OxygenHelperClient.syncData(TeleportationMain.CAMPS_DATA_ID);
        OxygenHelperClient.syncData(TeleportationMain.LOCATIONS_DATA_ID);

        if (TeleportationConfig.FEE_MODE.getIntValue() == 1)
            this.balance = InventoryHelper.getEqualStackAmount(ClientReference.getClientPlayer(), TeleportationManagerClient.instance().getFeeStackWrapper());
        else
            this.balance = WatcherHelperClient.getLong(OxygenPlayerData.CURRENCY_COINS_WATCHER_ID);
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        return new GUIWorkspace(this, 333, 151);
    }

    @Override
    protected void initSections() {
        this.getWorkspace().initSection(this.campsSection = (CampsGUISection) new CampsGUISection(this)
                .setDisplayText(ClientReference.localize("oxygen_teleportation.gui.menu.camps")).setEnabled(TeleportationConfig.ENABLE_CAMPS.getBooleanValue()));        
        this.getWorkspace().initSection(this.locationsSection = (LocationsGUISection) new LocationsGUISection(this)
                .setDisplayText(ClientReference.localize("oxygen_teleportation.gui.menu.locations")).setEnabled(TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue()));   
        this.getWorkspace().initSection(this.playersSection = (PlayersGUISection) new PlayersGUISection(this)
                .setDisplayText(ClientReference.localize("oxygen_teleportation.gui.menu.players")).setEnabled(TeleportationConfig.ENABLE_PLAYERS.getBooleanValue()));
    }

    @Override
    protected AbstractGUISection getDefaultSection() {	        
        if (TeleportationConfig.ENABLE_CAMPS.getBooleanValue())
            return this.campsSection;
        if (TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue())
            return this.locationsSection;
        if (TeleportationConfig.ENABLE_PLAYERS.getBooleanValue())
            return this.playersSection;
        return null;
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {}

    @Override
    protected boolean doesGUIPauseGame() {
        return false;
    }

    public void sharedDataSynchronized() {
        this.playersSection.sharedDataSynchronized();
    }

    public void campsSynchronized() {
        this.campsSection.campsSynchronized();
    }

    public void locationsSynchronized() {
        this.locationsSection.locationsSynchronized();
    }

    public void cooldownSynchronized() {
        this.campsSection.cooldownSynchronized();
        this.locationsSection.cooldownSynchronized();
        this.playersSection.cooldownSynchronized();
    }

    public void campCreated(WorldPoint worldPoint) {
        this.campsSection.campCreated(worldPoint);
    }

    public void campEdited(long oldPoiintId, WorldPoint worldPoint, boolean updateImage) {
        this.campsSection.campEdited(oldPoiintId, worldPoint, updateImage);
    }

    public void campRemoved(long pointId) {
        this.campsSection.campRemoved(pointId);
    }

    public void favoriteCampSet(long pointId) {
        this.campsSection.favoriteCampSet(pointId);
    }

    public void playerUninvited(long pointId, UUID playerUUID) {
        this.campsSection.playerUninvited(pointId, playerUUID);
    }

    public void locationCreated(WorldPoint worldPoint) {
        this.locationsSection.locationCreated(worldPoint);
    }

    public void locationEdited(long oldPoiintId, WorldPoint worldPoint, boolean updateImage) {
        this.locationsSection.locationEdited(oldPoiintId, worldPoint, updateImage);
    }

    public void locationRemoved(long pointId) {
        this.locationsSection.locationRemoved(pointId);
    }

    public CampsGUISection getCampsSection() {
        return this.campsSection;
    }

    public LocationsGUISection getLocationsSection() {
        return this.locationsSection;
    }

    public PlayersGUISection getPlayersSection() {
        return this.playersSection;
    }
}
