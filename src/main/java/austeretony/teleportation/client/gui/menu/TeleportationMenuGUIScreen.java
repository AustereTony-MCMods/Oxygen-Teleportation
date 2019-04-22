package austeretony.teleportation.client.gui.menu;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.oxygen.common.api.OxygenGUIHelper;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.TeleportationMain;
import net.minecraft.util.ResourceLocation;

public class TeleportationMenuGUIScreen extends AbstractGUIScreen {

    public static final ResourceLocation 
    POINTS_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/points_background.png"),
    PLAYERS_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/players_background.png"),
    CAMP_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/camp_icons.png"),
    LOCATION_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/location_icons.png"),
    PLAYERS_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/players_icons.png"),
    FAVORITE_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/favorite_icons.png"),
    SHARED_ICON = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/shared_icon.png");

    private CampsGUISection campsSection;

    private LocationsGUISection locationsSection;

    private PlayersGUISection playersSection;

    private boolean initialized;

    @Override
    protected GUIWorkspace initWorkspace() {
        return new GUIWorkspace(this, 327, 149);
    }

    @Override
    protected void initSections() {
        this.campsSection = new CampsGUISection(this);
        this.getWorkspace().initSection(this.campsSection);        
        this.locationsSection = new LocationsGUISection(this);
        this.getWorkspace().initSection(this.locationsSection);   
        this.playersSection = new PlayersGUISection(this);
        this.getWorkspace().initSection(this.playersSection);
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

    @Override
    public void updateScreen() {    
        super.updateScreen();
        if (!this.initialized//reduce map calls
                && OxygenGUIHelper.isNeedSync(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID)
                && OxygenGUIHelper.isScreenInitialized(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID)
                && OxygenGUIHelper.isDataRecieved(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID)) {
            this.initialized = true;
            OxygenGUIHelper.resetNeedSync(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
            this.campsSection.sortPoints(0);
            this.locationsSection.sortPoints(0);
            this.playersSection.sortPlayers(0);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        OxygenGUIHelper.resetNeedSync(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
        OxygenGUIHelper.resetScreenInitialized(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
        OxygenGUIHelper.resetDataRecieved(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
        TeleportationManagerClient.instance().getCampsLoader().savePlayerDataDelegated();
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
