package austeretony.teleportation.client.gui.menu;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.TeleportationMain;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MenuGUIScreen extends AbstractGUIScreen {

    public static final ResourceLocation 
    CAMP_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/camp_icons.png"),
    LOCATION_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/location_icons.png"),
    PLAYERS_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/players_icons.png"),
    SETTINGS_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/settings_icons.png"),
    DOWNLOAD_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/download_icons.png"),
    REFRESH_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/refresh_icons.png"),
    SEARCH_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/search_icons.png"),
    FAVORITE_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/favorite_icons.png"),
    EDIT_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/edit_icons.png"),
    REMOVE_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/remove_icons.png"),
    LOCK_ICONS = new ResourceLocation(TeleportationMain.MODID, "textures/gui/menu/lock_icons.png");

    protected AbstractGUISection campsSection, locationsSection, playersSection;

    @Override
    protected GUIWorkspace initWorkspace() {
        return new GUIWorkspace(this, 327, 152);
    }

    @Override
    protected void initSections() {
        this.campsSection = this.getWorkspace().initSection(new CampsGUISection(this));        
        this.locationsSection = this.getWorkspace().initSection(new LocationsGUISection(this));                   
        this.playersSection = this.getWorkspace().initSection(new PlayersGUISection(this));
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
    public boolean doesGuiPauseGame() {
        return false;
    }
}
