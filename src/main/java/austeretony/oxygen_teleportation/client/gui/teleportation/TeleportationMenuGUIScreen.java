package austeretony.oxygen_teleportation.client.gui.teleportation;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.oxygen.client.gui.SynchronizedGUIScreen;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraft.util.ResourceLocation;

public class TeleportationMenuGUIScreen extends SynchronizedGUIScreen {

    public static final ResourceLocation 
    POINTS_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/teleportation/points_menu.png"),
    PLAYERS_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/teleportation/players_menu.png"),

    CREATE_POINT_CALLBACK_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/teleportation/create_point_callback.png"),
    REMOVE_POINT_CALLBACK_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/teleportation/remove_point_callback.png"),
    EDIT_POINT_CALLBACK_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/teleportation/edit_point_callback.png"),
    INVITE_CALLBACK_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/teleportation/invite_callback.png"),
    LEAVE_CAMP_CALLBACK_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/teleportation/leave_camp_callback.png"),
    INVITATIONS_CALLBACK_BACKGROUND = new ResourceLocation(TeleportationMain.MODID, "textures/gui/teleportation/invitations_callback.png");

    private CampsGUISection campsSection;

    private LocationsGUISection locationsSection;

    private PlayersGUISection playersSection;

    public TeleportationMenuGUIScreen() {
        super(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        return new GUIWorkspace(this, 327, 149);
    }

    @Override
    protected void initSections() {
        this.getWorkspace().initSection(this.campsSection = new CampsGUISection(this));        
        this.getWorkspace().initSection(this.locationsSection = new LocationsGUISection(this));   
        this.getWorkspace().initSection(this.playersSection = new PlayersGUISection(this));
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
    public void loadData() {
        this.campsSection.sortPoints(0);
        this.locationsSection.sortPoints(0);
        this.playersSection.sortPlayers(0);
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
