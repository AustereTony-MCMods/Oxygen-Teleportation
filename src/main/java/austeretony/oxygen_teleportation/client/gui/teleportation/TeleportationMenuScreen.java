package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.UUID;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.common.inventory.InventoryHelper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.menu.TeleportationMenuEntry;
import austeretony.oxygen_teleportation.client.settings.gui.EnumTeleportationGUISetting;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class TeleportationMenuScreen extends AbstractGUIScreen {

    public static final OxygenMenuEntry TELEPORTATIOIN_MENU_ENTRY = new TeleportationMenuEntry();

    private CampsSection campsSection;

    private LocationsSection locationsSection;

    private PlayersSection playersSection;

    public final long balance;

    public final boolean campsEnabled, locationsEnabled, jumpsEnabled;

    public TeleportationMenuScreen() {
        OxygenHelperClient.syncSharedData(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);

        OxygenHelperClient.syncData(TeleportationMain.CAMPS_DATA_ID);
        OxygenHelperClient.syncData(TeleportationMain.LOCATIONS_DATA_ID);

        if (TeleportationConfig.FEE_MODE.asInt() == 1)
            this.balance = InventoryHelper.getEqualStackAmount(ClientReference.getClientPlayer(), TeleportationManagerClient.instance().getFeeStackWrapper());
        else
            this.balance = WatcherHelperClient.getLong(OxygenMain.COMMON_CURRENCY_INDEX);

        this.campsEnabled = PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean());
        this.locationsEnabled = PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.ALLOW_LOCATIONS_USAGE.id(), TeleportationConfig.ENABLE_LOCATIONS.asBoolean());
        this.jumpsEnabled = PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.ALLOW_PLAYER_TELEPORTATION_USAGE.id(), TeleportationConfig.ENABLE_PLAYER_TELEPORTATION.asBoolean());
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        EnumGUIAlignment alignment = EnumGUIAlignment.CENTER;
        switch (EnumTeleportationGUISetting.TELEPORTATION_MENU_ALIGNMENT.get().asInt()) {
        case - 1: 
            alignment = EnumGUIAlignment.LEFT;
            break;
        case 0:
            alignment = EnumGUIAlignment.CENTER;
            break;
        case 1:
            alignment = EnumGUIAlignment.RIGHT;
            break;    
        default:
            alignment = EnumGUIAlignment.CENTER;
            break;
        }
        return new GUIWorkspace(this, 333, 149).setAlignment(alignment, 0, 0);
    }

    @Override
    protected void initSections() {
        this.getWorkspace().initSection(this.campsSection = (CampsSection) new CampsSection(this).setEnabled(this.campsEnabled));        
        this.getWorkspace().initSection(this.locationsSection = (LocationsSection) new LocationsSection(this).setEnabled(this.locationsEnabled));   
        this.getWorkspace().initSection(this.playersSection = (PlayersSection) new PlayersSection(this).setEnabled(this.jumpsEnabled));
    }

    @Override
    protected AbstractGUISection getDefaultSection() {	        
        if (this.campsEnabled)
            return this.campsSection;
        if (this.locationsEnabled)
            return this.locationsSection;
        if (this.jumpsEnabled)
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

    public CampsSection getCampsSection() {
        return this.campsSection;
    }

    public LocationsSection getLocationsSection() {
        return this.locationsSection;
    }

    public PlayersSection getPlayersSection() {
        return this.playersSection;
    }
}
