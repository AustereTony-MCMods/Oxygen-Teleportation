package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.contextmenu.GUIContextMenu;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.panel.GUIButtonPanel;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.alternateui.util.EnumGUIOrientation;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen.util.MathUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.CampsBackgroundGUIFiller;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback.CampCreationGUICallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback.CampRemoveGUICallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback.EditCampGUICallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback.InvitationsGUICallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback.InviteGUICallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback.LeaveCampGUICallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.context.EditContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.context.InvitationsContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.context.InviteContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.context.LockContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.context.MakeFavoriteContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.context.RemoveContextAction;
import austeretony.oxygen_teleportation.client.input.TeleportationKeyHandler;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.WorldPoint;
import austeretony.oxygen_teleportation.common.main.WorldPoint.EnumWorldPoint;

public class CampsGUISection extends AbstractGUISection {

    private final TeleportationMenuGUIScreen screen;

    private GUITextLabel pointsAmountTextLabel, cooldownTextLabel;

    private GUIButton locationsPageButton, playersPageButton, searchButton, refreshButton, createButton, moveButton,
    sortUpButton, sortDownButton;

    private GUIButtonPanel pointsListPanel;

    private WorldPointGUIButton prevFavButton, currentButton;   

    private WorldPointDataGUIElement previewImageLabel;

    private WorldPoint currentPoint;

    private GUITextField searchTextField;

    private AbstractGUICallback creationCallback, inviteCallback, invitationsCallback, pointEditingCallback, 
    removePointCallback, leavePointCallback;

    private final int teleportationCooldown = PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN.toString(), 
            TeleportationConfig.CAMPS_TELEPORT_COOLDOWN.getIntValue()) * 1000;

    private boolean cooldown;

    public CampsGUISection(TeleportationMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {	
        int maxCamps = PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue());

        if (TeleportationManagerClient.instance().getPlayerData().getCampsAmount() > maxCamps)//owned + invitations
            maxCamps = TeleportationManagerClient.instance().getPlayerData().getCampsAmount();

        this.addElement(new CampsBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        String title = ClientReference.localize("teleportation.gui.menu.campsTitle");
        this.addElement(new GUITextLabel(2, 4).setDisplayText(title, false, GUISettings.instance().getTitleScale()));	

        this.addElement(new GUIButton(this.getWidth() - 44, 0, 12, 12).setTexture(TeleportationGUITextures.CAMP_ICONS, 12, 12).initSimpleTooltip(ClientReference.localize("teleportation.gui.menu.tooltip.camps"), GUISettings.instance().getTooltipScale()).toggle());	
        this.addElement(this.locationsPageButton = new GUIButton(this.getWidth() - 30, 0, 12, 12).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(TeleportationGUITextures.LOCATION_ICONS, 14, 14).initSimpleTooltip(ClientReference.localize("teleportation.gui.menu.tooltip.locations"), GUISettings.instance().getTooltipScale()));	
        this.addElement(this.playersPageButton = new GUIButton(this.getWidth() - 15, 0, 12, 12).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(TeleportationGUITextures.PLAYERS_ICONS, 14, 14).initSimpleTooltip(ClientReference.localize("teleportation.gui.menu.tooltip.players"), GUISettings.instance().getTooltipScale()));        
        if (!TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue())
            this.locationsPageButton.disable();
        if (!TeleportationConfig.ENABLE_PLAYERS.getBooleanValue())
            this.playersPageButton.disable();

        this.addElement(this.searchButton = new GUIButton(7, 15, 7, 7).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SEARCH_ICONS, 7, 7).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.search"), GUISettings.instance().getTooltipScale()));	
        this.addElement(this.sortDownButton = new GUIButton(2, 19, 3, 3).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SORT_DOWN_ICONS, 3, 3).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 
        this.addElement(this.sortUpButton = new GUIButton(2, 15, 3, 3).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SORT_UP_ICONS, 3, 3).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 
        this.addElement(this.refreshButton = new GUIButton(0, 14, 10, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.REFRESH_ICONS, 9, 9).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.refresh"), GUISettings.instance().getTooltipScale()));
        this.addElement(this.pointsAmountTextLabel = new GUITextLabel(0, 15).setTextScale(GUISettings.instance().getSubTextScale()));	

        this.pointsListPanel = new GUIButtonPanel(EnumGUIOrientation.VERTICAL, 0, 24, 82, 10).setButtonsOffset(1).setTextScale(GUISettings.instance().getTextScale());
        this.addElement(this.pointsListPanel);
        this.addElement(this.searchTextField = new GUITextField(0, 14, 85, 9, WorldPoint.MAX_POINT_NAME_LENGTH).setTextScale(GUISettings.instance().getSubTextScale())
                .enableDynamicBackground(GUISettings.instance().getEnabledTextFieldColor(), GUISettings.instance().getDisabledTextFieldColor(), GUISettings.instance().getHoveredTextFieldColor())
                .setLineOffset(3).setDisplayText("...").cancelDraggedElementLogic().disableFull());
        this.pointsListPanel.initSearchField(this.searchTextField);
        GUIScroller scroller = new GUIScroller(MathUtils.clamp(maxCamps, 10, 100), 10);
        this.pointsListPanel.initScroller(scroller);
        GUISlider slider = new GUISlider(83, 24, 2, 109);
        slider.setDynamicBackgroundColor(GUISettings.instance().getEnabledSliderColor(), GUISettings.instance().getDisabledSliderColor(), GUISettings.instance().getHoveredSliderColor());
        scroller.initSlider(slider);

        this.addElement(this.createButton = new GUIButton(22, 137, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor())
                .setDisplayText(ClientReference.localize("teleportation.gui.menu.createButton"), true, GUISettings.instance().getButtonTextScale()));     
        this.lockCreateButton();     

        GUIContextMenu menu = new GUIContextMenu(GUISettings.instance().getContextMenuWidth(), 10).setScale(GUISettings.instance().getContextMenuScale()).setTextScale(GUISettings.instance().getTextScale()).setTextAlignment(EnumGUIAlignment.LEFT, 2);
        menu.setOpenSound(OxygenSoundEffects.CONTEXT_OPEN.soundEvent);
        menu.setCloseSound(OxygenSoundEffects.CONTEXT_CLOSE.soundEvent);
        this.pointsListPanel.initContextMenu(menu);
        menu.enableDynamicBackground(GUISettings.instance().getEnabledContextActionColor(), GUISettings.instance().getDisabledContextActionColor(), GUISettings.instance().getHoveredContextActionColor());
        menu.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
        menu.addElement(new MakeFavoriteContextAction(this));
        menu.addElement(new LockContextAction(this));
        menu.addElement(new InviteContextAction(this));
        menu.addElement(new InvitationsContextAction(this));
        menu.addElement(new EditContextAction(this));
        menu.addElement(new RemoveContextAction(this));

        this.creationCallback = new CampCreationGUICallback(this.screen, this, 140, 71).enableDefaultBackground();
        this.inviteCallback = new InviteGUICallback(this.screen, this, 140, 68).enableDefaultBackground();
        this.invitationsCallback = new InvitationsGUICallback(this.screen, this, 140, 81).enableDefaultBackground();

        this.pointEditingCallback = new EditCampGUICallback(this.screen, this, 140, 92).enableDefaultBackground();
        this.removePointCallback = new CampRemoveGUICallback(this.screen, this, 140, 42).enableDefaultBackground();
        this.leavePointCallback = new LeaveCampGUICallback(this.screen, this, 140, 42).enableDefaultBackground();

        this.addElement(this.previewImageLabel = new WorldPointDataGUIElement(86, 14));
        this.addElement(this.moveButton = new GUIButton(92, 137,  40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor())
                .setDisplayText(ClientReference.localize("teleportation.gui.menu.moveButton"), true, GUISettings.instance().getButtonTextScale()).disableFull());

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.addElement(this.cooldownTextLabel = new GUITextLabel(134, 138).setTextScale(GUISettings.instance().getTextScale()).disableFull());  
            this.cooldown = true;
        }
    }

    public void sortPoints(int mode) {
        List<WorldPoint> points = new ArrayList<WorldPoint>(TeleportationManagerClient.instance().getPlayerData().getCamps());

        if (mode == 0)
            Collections.sort(points, (p1, p2)-> (int) ((p1.getId() - p2.getId()) / 5000L));
        else if (mode == 1)
            Collections.sort(points, (p1, p2)-> (int) ((p2.getId() - p1.getId()) / 5000L));

        this.pointsListPanel.reset();
        WorldPointGUIButton button;
        for (WorldPoint worldPoint : points) {
            button = new WorldPointGUIButton(worldPoint);
            button.enableDynamicBackground(GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getHoveredElementColor());
            button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            if (worldPoint.getId() == TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId()) {
                this.prevFavButton = button;
                button.setFavorite();
            }
            if (worldPoint.isLocked())
                button.setTextDynamicColor(GUISettings.instance().getEnabledTextColorDark(), GUISettings.instance().getDisabledTextColorDark(), GUISettings.instance().getHoveredTextColorDark());
            if (!worldPoint.isOwner(OxygenHelperClient.getPlayerUUID()))
                button.setDownloaded();
            if (TeleportationManagerClient.instance().getSharedCampsManager().invitedPlayersExist(worldPoint.getId()))
                button.setShared();
            this.pointsListPanel.addButton(button);
        }

        this.pointsAmountTextLabel.setDisplayText(String.valueOf(TeleportationManagerClient.instance().getPlayerData().getCampsAmount()) + 
                " / " + String.valueOf(PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue())));     
        this.pointsAmountTextLabel.setX(83 - this.textWidth(this.pointsAmountTextLabel.getDisplayText(), GUISettings.instance().getSubTextScale()));
        this.refreshButton.setX(this.pointsAmountTextLabel.getX() - 11);

        this.searchTextField.reset();

        this.pointsListPanel.getScroller().resetPosition();
        this.pointsListPanel.getScroller().getSlider().reset();

        this.sortUpButton.toggle();
        this.sortDownButton.setToggled(false);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.searchTextField.isEnabled() && !this.searchTextField.isHovered()) {
            this.sortUpButton.enableFull();
            this.sortDownButton.enableFull();
            this.searchButton.enableFull();
            this.refreshButton.enableFull();
            this.searchTextField.disableFull();
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);  	    	
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (element == this.locationsPageButton)                
            this.screen.getLocationsSection().open();
        else if (element == this.playersPageButton)
            this.screen.getPlayersSection().open();
        else if (element == this.createButton)
            this.creationCallback.open();
        else if (element == this.searchButton) {
            this.searchTextField.enableFull();
            this.sortUpButton.disableFull();
            this.sortDownButton.disableFull();
            this.searchButton.disableFull();
            this.refreshButton.disableFull();
        } else if (element == this.sortDownButton) {
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
                this.currentPoint = TeleportationManagerClient.instance().getPlayerData().getCamp(button.index);
                button.toggle();                    
                this.showPointInfo(false);
            }
        }
    }

    public void showPointInfo(boolean forceLoad) {
        this.previewImageLabel.show(this.currentPoint, EnumWorldPoint.CAMP, forceLoad);
        this.moveButton.enableFull();

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.cooldownTextLabel.enableFull();
            this.moveButton.disable();
        }

        if (this.currentPoint.isLocked() && !this.currentPoint.isOwner(OxygenHelperClient.getPlayerUUID()))
            this.moveButton.disable();
    }

    public void showFavoriteMark() {
        this.previewImageLabel.show(this.currentPoint, EnumWorldPoint.CAMP, false);
    }

    public void resetPointInfo() {
        this.previewImageLabel.hide();
        this.moveButton.disableFull();

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown)
            this.cooldownTextLabel.disableFull();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (keyCode == TeleportationKeyHandler.OPEN_MENU.getKeyCode() && !this.hasCurrentCallback() && !this.searchTextField.isDragged())
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
        return MathUtils.clamp((int) (this.teleportationCooldown - (System.currentTimeMillis() - TeleportationManagerClient.instance().getPlayerData().getCooldownInfo().getLastCampTime())), 
                0, this.teleportationCooldown);
    }

    public void lockCreateButton() {
        if (TeleportationManagerClient.instance().getPlayerData().getOwnedCampsAmount() >= PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue()))
            this.createButton.disable();
    }

    public void unlockCreateButton() {
        if (TeleportationManagerClient.instance().getPlayerData().getOwnedCampsAmount() < PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue()))
            this.createButton.enable();
    }

    public void openInviteCallback() {
        this.inviteCallback.open();
    }

    public void openInvitationsCallback() {
        this.invitationsCallback.open();
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
