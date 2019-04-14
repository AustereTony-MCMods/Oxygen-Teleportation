package austeretony.teleportation.client.gui.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.contextmenu.GUIContextAction;
import austeretony.alternateui.screen.contextmenu.GUIContextMenu;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.panel.GUIButtonPanel;
import austeretony.alternateui.screen.panel.GUIButtonPanel.GUIEnumOrientation;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen.client.gui.friends.FriendsListGUIScreen;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.camps.CampCreationGUICallback;
import austeretony.teleportation.client.gui.menu.camps.CampRemoveGUICallback;
import austeretony.teleportation.client.gui.menu.camps.CampsDownloadGUICallback;
import austeretony.teleportation.client.gui.menu.camps.EditCampGUICallback;
import austeretony.teleportation.client.gui.menu.camps.EditContextAction;
import austeretony.teleportation.client.gui.menu.camps.InvitationGUICallback;
import austeretony.teleportation.client.gui.menu.camps.InviteContextAction;
import austeretony.teleportation.client.gui.menu.camps.LeaveCampGUICallback;
import austeretony.teleportation.client.gui.menu.camps.LockContextAction;
import austeretony.teleportation.client.gui.menu.camps.MakeFavoriteContextAction;
import austeretony.teleportation.client.gui.menu.camps.RemoveContextAction;
import austeretony.teleportation.client.handler.TeleportationKeyHandler;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

public class CampsGUISection extends AbstractGUISection {

    private final TeleportationMenuGUIScreen screen;

    private GUITextLabel pointsAmountTextLabel, cooldownTextLabel;

    private GUIButton locationsPageButton, playersPageButton, downloadButton, searchButton, refreshButton, createButton, moveButton,
    sortUpButton, sortDownButton;

    private GUIButtonPanel pointsListPanel;

    private WorldPointGUIButton prevFavButton, currentButton;

    private PreviewGUIImageLabel previewImageLabel;

    private WorldPoint currentPoint;

    private GUITextField searchTextField;

    private AbstractGUICallback downloadCallback, creationCallback, invitationsManagementCallback, pointEditingCallback, removePointCallback, leavePointCallback;

    private final int teleportationCooldown = PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMP_TELEPORTATION_COOLDOWN.toString(), 
            TeleportationConfig.CAMPS_TELEPORT_COOLDOWN.getIntValue()) * 1000;

    private boolean cooldown;

    public CampsGUISection(TeleportationMenuGUIScreen screen) {
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
        String title = I18n.format("teleportation.gui.menu.campsTitle");
        this.addElement(new GUITextLabel(2, 4).setDisplayText(title, false, GUISettings.instance().getTitleScale()));	
        this.addElement(this.downloadButton = new GUIButton(this.width(title, GUISettings.instance().getTitleScale()) + 4, 4,  8, 8).setTexture(FriendsListGUIScreen.DOWNLOAD_ICONS, 8, 8).initSimpleTooltip(I18n.format("oxygen.tooltip.download"), GUISettings.instance().getTooltipScale()));

        this.addElement(new GUIButton(this.getWidth() - 44, 1,  14, 14).setTexture(TeleportationMenuGUIScreen.CAMP_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.gui.menu.tooltip.camps"), GUISettings.instance().getTooltipScale()).toggle());	
        this.addElement(this.locationsPageButton = new GUIButton(this.getWidth() - 30, 1, 14, 14).setTexture(TeleportationMenuGUIScreen.LOCATION_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.gui.menu.tooltip.locations"), GUISettings.instance().getTooltipScale()));	
        this.addElement(this.playersPageButton = new GUIButton(this.getWidth() - 15, 1, 14, 14).setTexture(TeleportationMenuGUIScreen.PLAYERS_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.gui.menu.tooltip.players"), GUISettings.instance().getTooltipScale()));        
        if (!TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue())
            this.locationsPageButton.disable();
        if (!TeleportationConfig.ENABLE_PLAYERS.getBooleanValue())
            this.playersPageButton.disable();

        this.addElement(this.searchButton = new GUIButton(7, 18, 7, 7).setTexture(FriendsListGUIScreen.SEARCH_ICONS, 7, 7).initSimpleTooltip(I18n.format("oxygen.tooltip.search"), GUISettings.instance().getTooltipScale()));	
        this.addElement(this.sortDownButton = new GUIButton(2, 22, 3, 3).setTexture(FriendsListGUIScreen.SORT_DOWN_ICONS, 3, 3).initSimpleTooltip(I18n.format("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 
        this.addElement(this.sortUpButton = new GUIButton(2, 18, 3, 3).setTexture(FriendsListGUIScreen.SORT_UP_ICONS, 3, 3).initSimpleTooltip(I18n.format("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 
        this.addElement(this.refreshButton = new GUIButton(0, 17, 10, 10).setTexture(FriendsListGUIScreen.REFRESH_ICONS, 9, 9).initSimpleTooltip(I18n.format("oxygen.tooltip.refresh"), GUISettings.instance().getTooltipScale()));
        this.addElement(this.pointsAmountTextLabel = new GUITextLabel(0, 18).setTextScale(GUISettings.instance().getSubTextScale()));	

        this.pointsListPanel = new GUIButtonPanel(GUIEnumOrientation.VERTICAL, 0, 27, 82, 10).setButtonsOffset(1).setTextScale(GUISettings.instance().getTextScale());
        this.addElement(this.pointsListPanel);
        this.addElement(this.searchTextField = new GUITextField(0, 18, 113, WorldPoint.MAX_POINT_NAME_LENGTH).setScale(0.7F).enableDynamicBackground().setDisplayText("...", false, GUISettings.instance().getTextScale()).cancelDraggedElementLogic().disableFull());
        this.pointsListPanel.initSearchField(this.searchTextField);
        GUIScroller panelScroller = new GUIScroller(maxCamps, 10);
        this.pointsListPanel.initScroller(panelScroller);
        GUISlider panelSlider = new GUISlider(83, 27, 2, 109);
        panelScroller.initSlider(panelSlider);

        this.addElement(this.createButton = new GUIButton(22, 140,  40, 10)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor())
                .setDisplayText(I18n.format("teleportation.gui.menu.createButton"), true, GUISettings.instance().getButtonTextScale()));     
        this.lockCreateButton();     

        GUIContextMenu menu = new GUIContextMenu(GUISettings.instance().getContextMenuWidth(), 10).setScale(GUISettings.instance().getContextMenuScale()).setTextScale(GUISettings.instance().getTextScale()).setTextAlignment(EnumGUIAlignment.LEFT, 2);
        this.pointsListPanel.initContextMenu(menu);
        menu.enableDynamicBackground(GUISettings.instance().getEnabledContextActionColor(), GUISettings.instance().getDisabledContextActionColor(), GUISettings.instance().getHoveredContextActionColor());
        menu.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
        menu.addElement(new GUIContextAction(new MakeFavoriteContextAction(this)));
        menu.addElement(new GUIContextAction(new LockContextAction(this)));
        menu.addElement(new GUIContextAction(new InviteContextAction(this)));
        menu.addElement(new GUIContextAction(new EditContextAction(this)));
        menu.addElement(new GUIContextAction(new RemoveContextAction(this)));

        this.downloadCallback = new CampsDownloadGUICallback(this.screen, this, 140, 40).enableDefaultBackground();
        this.creationCallback = new CampCreationGUICallback(this.screen, this, 140, 71).enableDefaultBackground();
        this.invitationsManagementCallback = new InvitationGUICallback(this.screen, this, 140, 132).enableDefaultBackground();
        this.pointEditingCallback = new EditCampGUICallback(this.screen, this, 140, 100).enableDefaultBackground();
        this.removePointCallback = new CampRemoveGUICallback(this.screen, this, 140, 42).enableDefaultBackground();
        this.leavePointCallback = new LeaveCampGUICallback(this.screen, this, 140, 42).enableDefaultBackground();

        this.addElement(this.previewImageLabel = new PreviewGUIImageLabel(87, 17));
        this.addElement(this.moveButton = new GUIButton(92, 140,  40, 10)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor())
                .setDisplayText(I18n.format("teleportation.gui.menu.moveButton"), true, GUISettings.instance().getButtonTextScale()).disableFull());

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.addElement(this.cooldownTextLabel = new GUITextLabel(134, 141).setTextScale(GUISettings.instance().getTextScale()).disableFull());  
            this.cooldown = true;
        }

        this.sortPoints(0);
    }

    public void sortPoints(int mode) {
        List<WorldPoint> points = new ArrayList<WorldPoint>(TeleportationManagerClient.instance().getPlayerData().getCamps());
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
            if (worldPoint.getId() == TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId()) {
                this.prevFavButton = button;
                button.setFavorite();
            }
            if (worldPoint.isLocked() || !worldPoint.isOwner(OxygenHelperClient.getPlayerUUID()))
                button.setTextDynamicColor(GUISettings.instance().getEnabledTextColorDark(), GUISettings.instance().getDisabledTextColorDark(), GUISettings.instance().getHoveredTextColorDark());
            if (TeleportationManagerClient.instance().getPlayerData().haveInvitedPlayers(worldPoint.getId()))
                button.setShared();
            this.pointsListPanel.addButton(button);
        }

        this.pointsAmountTextLabel.setDisplayText(String.valueOf(TeleportationManagerClient.instance().getPlayerData().getCampsAmount()) + 
                " / " + String.valueOf(PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue())));     
        this.pointsAmountTextLabel.setX(83 - this.width(this.pointsAmountTextLabel.getDisplayText(), GUISettings.instance().getSubTextScale()));
        this.refreshButton.setX(this.pointsAmountTextLabel.getX() - 11);

        this.pointsListPanel.getScroller().resetPosition();
        this.pointsListPanel.getScroller().getSlider().reset();

        this.sortUpButton.toggle();
        this.sortDownButton.setToggled(false);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.searchTextField.isEnabled() && !this.searchTextField.isHovered())
            this.searchTextField.disableFull();
        return super.mouseClicked(mouseX, mouseY, mouseButton);  	    	
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {
        if (element == this.locationsPageButton)                
            this.screen.getLocationsSection().open();
        else if (element == this.playersPageButton)
            this.screen.getPlayersSection().open();
        else if (element == this.downloadButton)
            this.downloadCallback.open();
        else if (element == this.createButton)
            this.creationCallback.open();
        else if (element == this.searchButton)
            this.searchTextField.enableFull();
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
            this.searchTextField.reset();
            this.sortPoints(0);
            this.resetPointInfo();
        } else if (element == this.moveButton) {
            TeleportationManagerClient.instance().getCampsManager().moveToCampSynced(this.currentPoint.getId());
            this.screen.close();
        } else if (element instanceof WorldPointGUIButton) {
            WorldPointGUIButton button = (WorldPointGUIButton) element;
            if (this.currentButton != button) {
                if (this.currentButton != null)
                    this.currentButton.setToggled(false);
                this.currentButton = button;
                this.currentPoint = button.worldPoint;
                button.toggle();                    
                this.showPointInfo(false);
            }
        }
    }

    public void showPointInfo(boolean forceLoad) {
        this.previewImageLabel.show(this.currentPoint, forceLoad);
        this.moveButton.enableFull();

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.cooldownTextLabel.enableFull();
            this.moveButton.disable();
        }
    }

    public void showFavoriteMark() {
        this.previewImageLabel.show(this.currentPoint, false);
    }

    public void resetPointInfo() {
        this.previewImageLabel.hide();
        this.moveButton.disableFull();

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown)
            this.cooldownTextLabel.disableFull();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (keyCode == TeleportationKeyHandler.OPEN_MENU.getKeyBinding().getKeyCode() && !this.hasCurrentCallback() && !this.searchTextField.isDragged())
            this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    @Override
    public void update() {
        if (this.cooldown) {
            if (this.getCooldownElapsedTime() > 0)
                this.cooldownTextLabel.setDisplayText("[" + (this.getCooldownElapsedTime() / 1000) + "]");
            else if (this.cooldown) {
                this.cooldown = false;
                this.cooldownTextLabel.disableFull();
                this.moveButton.enable();
            }
        }
    }

    private int getCooldownElapsedTime() {
        return MathHelper.clamp((int) (this.teleportationCooldown - (System.currentTimeMillis() - TeleportationManagerClient.instance().getPlayerData().getCooldownInfo().getLastCampTime())), 
                0, this.teleportationCooldown);
    }

    public void lockCreateButton() {
        if (TeleportationManagerClient.instance().getPlayerData().getCampsAmount() >= PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue()))
            this.createButton.disable();
    }

    public void unlockCreateButton() {
        if (TeleportationManagerClient.instance().getPlayerData().getCampsAmount() < PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue()))
            this.createButton.enable();
    }

    public void openInvitationCallback() {
        this.invitationsManagementCallback.open();
    }

    public void openPointEditingCallback() {
        this.pointEditingCallback.open();
    }

    public void openRemovePointCallback() {
        this.removePointCallback.open();
    }

    public void openLeavePointCallback() {
        this.leavePointCallback.open();
    }

    public WorldPoint getCurrentPoint() {
        return this.currentPoint;
    }

    public WorldPointGUIButton getCurrentButton() {
        return this.currentButton;
    }

    public WorldPointGUIButton getPreviousFavoriteButton() {
        return this.prevFavButton;
    }
}
