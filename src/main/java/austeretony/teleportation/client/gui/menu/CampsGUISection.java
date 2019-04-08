package austeretony.teleportation.client.gui.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.panel.SearchableGUIButtonPanel;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.camps.CampCreationGUICallback;
import austeretony.teleportation.client.gui.menu.camps.CampRemoveGUICallback;
import austeretony.teleportation.client.gui.menu.camps.CampsDownloadGUICallback;
import austeretony.teleportation.client.gui.menu.camps.EditCampGUICallback;
import austeretony.teleportation.client.gui.menu.camps.InvitationGUICallback;
import austeretony.teleportation.client.gui.menu.camps.LeaveCampGUICallback;
import austeretony.teleportation.client.handler.KeyHandler;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CampsGUISection extends AbstractGUISection {

    private final MenuGUIScreen screen;

    private GUITextLabel pointsAmountLabel, cooldownLabel;

    private GUIButton campsPageButton, locationsPageButton, playersPageButton, downloadButton, searchButton, refreshButton, createButton, moveButton, setFavoriteButton,
    lockPointButton, invitationsManagementButton, editPointButton, removePointButton, sortUpButton, sortDownButton;

    private SearchableGUIButtonPanel pointsListPanel;

    private WorldPointGUIButton prevFavButton, currentButton;

    private PreviewGUIImageLabel previewLabel;

    public WorldPoint currentPoint;

    private GUITextField searchField;

    private AbstractGUICallback downloadCallback, creationCallback, invitationsManagementCallback, editingPointCallback, removePointCallback, leavePointCallback;

    private final int teleportationCooldown = PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMP_TELEPORTATION_COOLDOWN.toString(), 
            TeleportationConfig.CAMPS_TELEPORT_COOLDOWN.getIntValue()) * 1000;

    private boolean cooldown;

    public CampsGUISection(MenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    protected void init() {	
        int maxCamps = PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue());

        this.addElement(new GUIImageLabel(- 1, - 1, this.getWidth() + 2, this.getHeight() + 2).enableStaticBackground(GUISettings.instance().getBaseGUIBackgroundColor()));//main background
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), 15).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//title background
        this.addElement(new GUIImageLabel(0, 17, 85, 9).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//search background
        this.addElement(new GUIImageLabel(0, 27, 82, 109).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//list background
        this.addElement(new GUIImageLabel(83, 27, 2, 109).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor())); //slider background
        this.addElement(new GUIImageLabel(0, 138, 85, 14).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//create background
        this.addElement(new GUITextLabel(2, 4).setDisplayText(I18n.format("teleportation.menu.campsTitle")));	

        this.addElement(this.campsPageButton = new GUIButton(this.getWidth() - 44, 1,  14, 14).setTexture(MenuGUIScreen.CAMP_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.camps"), 0.8F).toggle());	
        this.addElement(this.locationsPageButton = new GUIButton(this.getWidth() - 30, 1, 14, 14).setTexture(MenuGUIScreen.LOCATION_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.locations"), 0.8F));	
        this.addElement(this.playersPageButton = new GUIButton(this.getWidth() - 15, 1, 14, 14).setTexture(MenuGUIScreen.PLAYERS_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.players"), 0.8F));        
        if (!TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue())
            this.locationsPageButton.disable();
        if (!TeleportationConfig.ENABLE_PLAYERS.getBooleanValue())
            this.playersPageButton.disable();

        this.addElement(this.searchButton = new GUIButton(7, 18, 7, 7).setTexture(MenuGUIScreen.SEARCH_ICONS, 7, 7).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.search"), 0.8F));	
        this.addElement(this.sortDownButton = new GUIButton(2, 22, 3, 3).setTexture(MenuGUIScreen.SORT_DOWN_ICONS, 3, 3).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.sort"), 0.8F)); 
        this.addElement(this.sortUpButton = new GUIButton(2, 18, 3, 3).setTexture(MenuGUIScreen.SORT_UP_ICONS, 3, 3).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.sort"), 0.8F)); 
        this.addElement(this.refreshButton = new GUIButton(0, 17, 10, 10).setTexture(MenuGUIScreen.REFRESH_ICONS, 10, 10).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.refresh"), 0.8F));
        this.addElement(this.downloadButton = new GUIButton(this.width(I18n.format("teleportation.menu.campsTitle")) + 3, 4,  8, 8).setTexture(MenuGUIScreen.DOWNLOAD_ICONS, 8, 8).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.download"), 0.8F));
        this.addElement(this.pointsAmountLabel = new GUITextLabel(0, 18).setTextScale(0.7F));	

        this.downloadCallback = new CampsDownloadGUICallback(this.screen, this, 140, 40).enableDefaultBackground();

        this.pointsListPanel = new SearchableGUIButtonPanel(0, 27, 82, 10, 10);
        this.pointsListPanel.setButtonsOffset(1);
        this.pointsListPanel.setTextScale(0.8F);
        this.addElement(this.pointsListPanel);
        this.addElement(this.searchField = new GUITextField(0, 18, 98, 16).setScale(0.8F).enableDynamicBackground().setDisplayText("...", false, 0.8F).disableFull());
        this.pointsListPanel.initSearchField(this.searchField);
        GUIScroller panelScroller = new GUIScroller(maxCamps, 10);
        this.pointsListPanel.initScroller(panelScroller);
        GUISlider panelSlider = new GUISlider(83, 27, 2, 109);
        panelScroller.initSlider(panelSlider);

        this.addElement(this.createButton = new GUIButton(22, 140,  40, 10).enableDynamicBackground(0xFF404040, 0xFF202020, 0xFF606060).setDisplayText(I18n.format("teleportation.menu.createButton"), true, 0.8F));     
        this.lockCreateButton();     
        this.creationCallback = new CampCreationGUICallback(this.screen, this, 140, 74).enableDefaultBackground();

        this.invitationsManagementCallback = new InvitationGUICallback(this.screen, this, 140, 132).enableDefaultBackground();
        this.editingPointCallback = new EditCampGUICallback(this.screen, this, 140, 100).enableDefaultBackground();
        this.removePointCallback = new CampRemoveGUICallback(this.screen, this, 140, 42).enableDefaultBackground();
        this.leavePointCallback = new LeaveCampGUICallback(this.screen, this, 140, 42).enableDefaultBackground();

        this.initSpecials(); 
        this.updatePoints();
        this.sortDownButton.toggle(); 
    }

    private void initSpecials() {
        this.addElement(this.previewLabel = new PreviewGUIImageLabel(87, 17));
        this.addElement(this.moveButton = new GUIButton(92, 140,  40, 10).enableDynamicBackground(0xFF404040, 0xFF101010, 0xFF606060).setDisplayText(I18n.format("teleportation.menu.moveButton"), true, 0.8F).disableFull());
        this.addElement(this.setFavoriteButton = new GUIButton(this.getWidth() - 67, 20,  10, 10).enableStaticBackground(0x96101010).setTexture(MenuGUIScreen.FAVORITE_ICONS, 10, 10).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.favorite"), 0.8F).disableFull());
        this.addElement(this.lockPointButton = new GUIButton(this.getWidth() - 54, 20,  10, 10).enableStaticBackground(0x96101010).setTexture(MenuGUIScreen.LOCK_ICONS, 10, 10).disableFull());
        this.addElement(this.invitationsManagementButton = new GUIButton(this.getWidth() - 41, 20,  10, 10).enableStaticBackground(0x96101010).setTexture(MenuGUIScreen.INVITE_ICONS, 10, 10).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.invite"), 0.8F).disableFull());
        this.addElement(this.editPointButton = new GUIButton(this.getWidth() - 28, 20,  10, 10).enableStaticBackground(0x96101010).setTexture(MenuGUIScreen.EDIT_ICONS, 10, 10).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.edit"), 0.8F).disableFull());
        this.addElement(this.removePointButton = new GUIButton(this.getWidth() - 15, 20,  10, 10).enableStaticBackground(0x96101010).setTexture(MenuGUIScreen.REMOVE_ICONS, 10, 10).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.remove"), 0.8F).disableFull());

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.addElement(this.cooldownLabel = new GUITextLabel(134, 141).setTextScale(0.7F).disableFull());  
            this.cooldown = true;
        }
    }

    public void updatePoints() {
        this.pointsAmountLabel.setDisplayText(String.valueOf(TeleportationManagerClient.instance().getPlayerProfile().getCampsAmount()) + 
                " / " + String.valueOf(PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue())));	
        this.pointsAmountLabel.setX(83 - (int) ((float) this.width(this.pointsAmountLabel.getDisplayText()) * 0.7F));
        this.refreshButton.setX(this.pointsAmountLabel.getX() - 11);
        this.sortPoints(1);
    }

    private void sortPoints(int mode) {
        List<WorldPoint> points = new ArrayList<WorldPoint>(TeleportationManagerClient.instance().getPlayerProfile().getCamps().values());
        Collections.sort(points, new Comparator<WorldPoint>() {

            @Override
            public int compare(WorldPoint point1, WorldPoint point2) {
                if (mode == 0)
                    return (int) (point1.getId() - point2.getId());
                else
                    return (int) (point2.getId() - point1.getId());
            }
        });
        this.pointsListPanel.reset();
        WorldPointGUIButton button;
        for (WorldPoint worldPoint : points) {
            button = new WorldPointGUIButton(worldPoint);
            button.enableDynamicBackground(GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getHoveredElementColor());
            button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            button.setDisplayText(worldPoint.getName());
            button.setTextAlignment(EnumGUIAlignment.LEFT, 2);
            if (worldPoint.getId() == TeleportationManagerClient.instance().getPlayerProfile().getFavoriteCampId()) {
                this.prevFavButton = button;
                button.setFavorite();
            }
            if (worldPoint.isLocked() || !worldPoint.isOwner(OxygenHelperClient.getPlayerUUID()))
                button.setTextDynamicColor(GUISettings.instance().getEnabledTextColorDark(), GUISettings.instance().getDisabledTextColorDark(), GUISettings.instance().getHoveredTextColorDark());
            if (TeleportationManagerClient.instance().getPlayerProfile().haveInvitedPlayers(worldPoint.getId()))
                button.setShared();
            this.pointsListPanel.addButton(button);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY) {
        if (this.searchField.isEnabled() && !this.searchField.isHovered())
            this.searchField.disableFull();
        return super.mouseClicked(mouseX, mouseY);  	    	
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {
        if (element == this.locationsPageButton)                
            this.screen.locationsSection.open();
        else if (element == this.playersPageButton)
            this.screen.playersSection.open();
        else if (element == this.downloadButton)
            this.downloadCallback.open();
        else if (element == this.createButton)
            this.creationCallback.open();
        else if (element == this.searchButton)
            this.searchField.enableFull();
        else if (element == this.sortDownButton) {
            if (!this.sortDownButton.isToggled()) {
                this.sortPoints(1);
                this.sortUpButton.setToggled(false);
                this.sortDownButton.toggle(); 
            }
        } else if (element == this.sortUpButton) {
            if (!this.sortUpButton.isToggled()) {
                this.sortPoints(0);
                this.sortDownButton.setToggled(false);
                this.sortUpButton.toggle();
            }
        } else if (element == this.refreshButton) {
            this.searchField.reset();
            this.updatePoints();
            this.resetPointInfo();
        } else if (element == this.moveButton) {
            TeleportationManagerClient.instance().getCampsManager().moveToCampSynced(this.currentPoint.getId());
            this.screen.close();
        } else if (element == this.setFavoriteButton) {
            TeleportationManagerClient.instance().getCampsManager().setFavoriteCampSynced(this.currentPoint.getId());
            if (this.prevFavButton != null)
                this.prevFavButton.resetFavorite();
            this.currentButton.setFavorite();
            this.showFavoriteMark();
        } else if (element == this.lockPointButton) {
            if (!this.lockPointButton.isToggled()) {
                TeleportationManagerClient.instance().getCampsManager().lockCampSynced(this.currentPoint, true);
                this.lockPointButton.initSimpleTooltip(I18n.format("teleportation.menu.tooltip.unlock"), 0.8F);
                this.lockPointButton.toggle();
            } else {
                TeleportationManagerClient.instance().getCampsManager().lockCampSynced(this.currentPoint, false);
                this.lockPointButton.initSimpleTooltip(I18n.format("teleportation.menu.tooltip.lock"), 0.8F);  
                this.lockPointButton.setToggled(false);
            }
        } else if (element == this.invitationsManagementButton)
            this.invitationsManagementCallback.open();
        else if (element == this.editPointButton)
            this.editingPointCallback.open();
        else if (element == this.removePointButton) {
            if (this.currentPoint.isOwner(OxygenHelperClient.getPlayerUUID()))
                this.removePointCallback.open();
            else
                this.leavePointCallback.open();
        } else {
            for (GUIButton button : this.pointsListPanel.buttons.values()) {
                if (element == button && this.currentButton != button) {
                    if (this.currentButton != null)
                        this.currentButton.setToggled(false);
                    this.currentButton = (WorldPointGUIButton) button;
                    this.currentPoint = ((WorldPointGUIButton) button).worldPoint;
                    button.toggle();                    
                    this.showPointInfo(false);
                }
            }
        }
    }

    public void showPointInfo(boolean forceLoad) {
        this.previewLabel.show(this.currentPoint, forceLoad);
        this.moveButton.enableFull();
        if (TeleportationConfig.ENABLE_FAVORITE_CAMP.getBooleanValue()) {
            this.setFavoriteButton.enableFull();
            if (this.currentPoint.getId() == TeleportationManagerClient.instance().getPlayerProfile().getFavoriteCampId())
                this.setFavoriteButton.disable();
        }
        this.invitationsManagementButton.enableFull();
        this.invitationsManagementButton.disable();//TODO Invitations, WIP
        this.editPointButton.enableFull();
        this.removePointButton.enableFull();
        this.lockPointButton.enableFull();
        if (this.currentPoint.isLocked())
            this.lockPointButton.toggle();
        else
            this.lockPointButton.setToggled(false);
        if (!this.currentPoint.isOwner(OxygenHelperClient.getPlayerUUID())) {
            this.invitationsManagementButton.disable();
            this.editPointButton.disable();
            this.lockPointButton.disable();
            this.removePointButton.initSimpleTooltip(I18n.format("teleportation.menu.tooltip.leave"), 0.8F);
        }

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.cooldownLabel.enableFull();
            this.moveButton.disable();
        }

        if (this.currentPoint.isLocked())
            this.lockPointButton.initSimpleTooltip(I18n.format("teleportation.menu.tooltip.unlock"), 0.8F);
        else
            this.lockPointButton.initSimpleTooltip(I18n.format("teleportation.menu.tooltip.lock"), 0.8F);  
    }

    private void showFavoriteMark() {
        this.previewLabel.show(this.currentPoint, false);
        this.setFavoriteButton.disable();
    }

    public void resetPointInfo() {
        this.previewLabel.hide();
        this.moveButton.disableFull();
        this.setFavoriteButton.disableFull();
        this.invitationsManagementButton.disableFull();
        this.editPointButton.disableFull();
        this.removePointButton.disableFull();
        this.lockPointButton.disableFull();
        this.lockPointButton.setToggled(false);

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown)
            this.cooldownLabel.disableFull();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (keyCode == KeyHandler.OPEN_MENU.getKeyCode() && !this.hasCurrentCallback() && !this.searchField.isDragged())
            this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    @Override
    public void update() {
        if (this.cooldown) {
            if (this.getCooldownElapsedTime() > 0)
                this.cooldownLabel.setDisplayText("[" + (this.getCooldownElapsedTime() / 1000) + "]");
            else if (this.cooldown) {
                this.cooldown = false;
                this.cooldownLabel.disableFull();
                this.moveButton.enable();
            }
        }
    }

    private int getCooldownElapsedTime() {
        return MathHelper.clamp((int) (this.teleportationCooldown - (System.currentTimeMillis() - TeleportationManagerClient.instance().getPlayerProfile().getCooldownInfo().getLastCampTime()))
                , 0, this.teleportationCooldown);
    }

    public void lockCreateButton() {
        if (TeleportationManagerClient.instance().getPlayerProfile().getCampsAmount() >= PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue()))
            this.createButton.disable();
    }

    public void unlockCreateButton() {
        if (TeleportationManagerClient.instance().getPlayerProfile().getCampsAmount() < PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue()))
            this.createButton.enable();
    }
}
