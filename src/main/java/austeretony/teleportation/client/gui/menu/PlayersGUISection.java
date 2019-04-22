package austeretony.teleportation.client.gui.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.list.GUIDropDownList;
import austeretony.alternateui.screen.panel.GUIButtonPanel;
import austeretony.alternateui.screen.panel.GUIButtonPanel.GUIEnumOrientation;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen.client.OxygenManagerClient;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.gui.StatusGUIDropDownElement;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.EnumDimensions;
import austeretony.oxygen.common.api.OxygenGUIHelper;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.main.EnumOxygenPrivileges;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.main.SharedPlayerData;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.players.JumpProfileGUIDropDownElement;
import austeretony.teleportation.client.gui.menu.players.PlayerGUIButton;
import austeretony.teleportation.client.gui.menu.players.PlayersBackgroundGUIFiller;
import austeretony.teleportation.client.handler.TeleportationKeyHandler;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumTeleportationPrivileges;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationPlayerData;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

public class PlayersGUISection extends AbstractGUISection {

    private final TeleportationMenuGUIScreen screen;

    private GUITextLabel playersAmountTextLabel, targetUsernameTextLabel, targetDimensionTextLabel, cooldownTextLabel;

    private GUIButton campsPageButton, locationsPageButton, searchButton, refreshButton, moveButton, sortDownStatusButton, sortUpStatusButton, 
    sortDownUsernameButton, sortUpUsernameButton;

    private PlayerGUIButton currentButton;

    private GUIButtonPanel playersListPanel;

    private GUITextField searchTextField;

    private GUIDropDownList statusDropDownList, jumpProfileDropDownList;

    private GUIImageLabel statusImageLabel;

    private SharedPlayerData currentPlayer;

    private OxygenPlayerData.EnumStatus currentStatus;

    private TeleportationPlayerData.EnumJumpProfile currentJumpProfile;

    private final Set<SharedPlayerData> playerList = new HashSet<SharedPlayerData>();

    private String 
    moveToStr = I18n.format("teleportation.gui.menu.moveButton"), 
    requestStr = I18n.format("teleportation.gui.menu.requestButton");

    private final int teleportationCooldown = PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.PLAYER_TELEPORTATION_COOLDOWN.toString(), 
            TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) * 1000;

    private boolean cooldown;

    public PlayersGUISection(TeleportationMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {              
        this.addElement(new PlayersBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 4).setDisplayText(I18n.format("teleportation.gui.menu.playersTitle"), false, GUISettings.instance().getTitleScale()));     
        this.addElement(new GUITextLabel(111, 27).setDisplayText(I18n.format("oxygen.gui.friends.username"), false, GUISettings.instance().getTextScale())); 
        this.addElement(new GUITextLabel(197, 27).setDisplayText(I18n.format("oxygen.gui.friends.dimension"), false, GUISettings.instance().getTextScale())); 
        this.addElement(new GUITextLabel(282, 27).setDisplayText(I18n.format("teleportation.gui.menu.players.profile"), false, GUISettings.instance().getTextScale())); 

        this.addElement(this.campsPageButton = new GUIButton(this.getWidth() - 44, 0,  12, 12).setTexture(TeleportationMenuGUIScreen.CAMP_ICONS, 12, 12).initSimpleTooltip(I18n.format("teleportation.gui.menu.tooltip.camps"), GUISettings.instance().getTooltipScale()));   
        this.addElement(this.locationsPageButton = new GUIButton(this.getWidth() - 30, 0, 12, 12).setTexture(TeleportationMenuGUIScreen.LOCATION_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.gui.menu.tooltip.locations"), GUISettings.instance().getTooltipScale()));   
        this.addElement(new GUIButton(this.getWidth() - 15, 0, 12, 12).setTexture(TeleportationMenuGUIScreen.PLAYERS_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.gui.menu.tooltip.players"), GUISettings.instance().getTooltipScale()).toggle());
        if (!TeleportationConfig.ENABLE_CAMPS.getBooleanValue())
            this.campsPageButton.disable();
        if (!TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue())
            this.locationsPageButton.disable();

        this.addElement(this.searchButton = new GUIButton(91, 15, 7, 7).setTexture(OxygenGUITextures.SEARCH_ICONS, 7, 7).initSimpleTooltip(I18n.format("oxygen.tooltip.search"), GUISettings.instance().getTooltipScale()));  

        this.addElement(this.sortDownStatusButton = new GUIButton(94, 31, 3, 3).setTexture(OxygenGUITextures.SORT_DOWN_ICONS, 3, 3).initSimpleTooltip(I18n.format("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale()));   
        this.addElement(this.sortUpStatusButton = new GUIButton(94, 27, 3, 3).setTexture(OxygenGUITextures.SORT_UP_ICONS, 3, 3).initSimpleTooltip(I18n.format("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 
        this.addElement(this.sortDownUsernameButton = new GUIButton(106, 31, 3, 3).setTexture(OxygenGUITextures.SORT_DOWN_ICONS, 3, 3).initSimpleTooltip(I18n.format("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale()));   
        this.addElement(this.sortUpUsernameButton = new GUIButton(106, 27, 3, 3).setTexture(OxygenGUITextures.SORT_UP_ICONS, 3, 3).initSimpleTooltip(I18n.format("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 

        this.addElement(this.refreshButton = new GUIButton(175, 14, 10, 10).setTexture(OxygenGUITextures.REFRESH_ICONS, 9, 9).initSimpleTooltip(I18n.format("oxygen.tooltip.refresh"), GUISettings.instance().getTooltipScale()));
        this.addElement(this.playersAmountTextLabel = new GUITextLabel(0, 15).setTextScale(GUISettings.instance().getSubTextScale()));   

        this.playersListPanel = new GUIButtonPanel(GUIEnumOrientation.VERTICAL, 87, 39, 237, 10).setButtonsOffset(1).setTextScale(GUISettings.instance().getPanelTextScale());
        this.addElement(this.playersListPanel);
        this.addElement(this.searchTextField = new GUITextField(87, 15, 113, WorldPoint.MAX_POINT_NAME_LENGTH).setScale(0.7F).enableDynamicBackground().setDisplayText("...", false, GUISettings.instance().getTextScale()).cancelDraggedElementLogic().disableFull());
        this.playersListPanel.initSearchField(this.searchTextField);
        GUIScroller scroller = new GUIScroller(this.playerList.size() > 10 ? this.playerList.size() : 10, 10);
        this.playersListPanel.initScroller(scroller);
        GUISlider slider = new GUISlider(325, 39, 2, 110);
        slider.setDynamicBackgroundColor(GUISettings.instance().getEnabledSliderColor(), GUISettings.instance().getDisabledSliderColor(), GUISettings.instance().getHoveredSliderColor());
        scroller.initSlider(slider);

        this.addElement(this.targetUsernameTextLabel = new GUITextLabel(3, 101).setTextScale(GUISettings.instance().getTextScale()));  
        this.addElement(this.targetDimensionTextLabel = new GUITextLabel(3, 109).setTextScale(GUISettings.instance().getSubTextScale()));    
        this.addElement(this.moveButton = new GUIButton(22, 137,  40, 10)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor())
                .setTextScale(GUISettings.instance().getButtonTextScale()).enableTextShadow().disableFull());

        SharedPlayerData clientSharedData = OxygenHelperClient.getSharedClientPlayerData();
        this.currentStatus = getPlayerStatus(clientSharedData);

        this.addElement(new GUITextLabel(3, 33).setDisplayText(clientSharedData.getUsername(), false, GUISettings.instance().getTextScale()));  
        this.addElement(new GUITextLabel(3, 50).setDisplayText(EnumDimensions.getLocalizedNameFromId(OxygenHelperClient.getPlayerDimension(clientSharedData)), false, GUISettings.instance().getSubTextScale()));   

        TeleportationPlayerData.EnumJumpProfile clientJumpProfile = getJumpProfile(clientSharedData);
        this.currentJumpProfile = clientJumpProfile;
        this.jumpProfileDropDownList = new GUIDropDownList(2, 61, 70, 10).setDisplayText(clientJumpProfile.getLocalizedName())
                .setScale(GUISettings.instance().getDropDownListScale()).setTextScale(GUISettings.instance().getTextScale()).setTextAlignment(EnumGUIAlignment.LEFT, 1);
        JumpProfileGUIDropDownElement jumpProfileElement;
        for (TeleportationPlayerData.EnumJumpProfile jumpProfile : TeleportationPlayerData.EnumJumpProfile.values()) {
            jumpProfileElement = new JumpProfileGUIDropDownElement(jumpProfile);
            jumpProfileElement.setDisplayText(jumpProfile.getLocalizedName());
            jumpProfileElement.enableDynamicBackground(GUISettings.instance().getEnabledContextActionColor(), GUISettings.instance().getDisabledContextActionColor(), GUISettings.instance().getHoveredContextActionColor());
            jumpProfileElement.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            this.jumpProfileDropDownList.addElement(jumpProfileElement);
        }
        this.addElement(this.jumpProfileDropDownList);   

        this.addElement(this.statusImageLabel = new GUIImageLabel(3, 44).setTexture(OxygenGUITextures.STATUS_ICONS, 3, 3, this.currentStatus.ordinal() * 3, 0, 12, 3));   
        this.statusDropDownList = new GUIDropDownList(9, 42, GUISettings.instance().getContextMenuWidth(), 10).setScale(GUISettings.instance().getDropDownListScale()).setDisplayText(this.currentStatus.getLocalizedName()).setTextScale(GUISettings.instance().getTextScale()).setTextAlignment(EnumGUIAlignment.LEFT, 1);
        StatusGUIDropDownElement profileElement;
        for (OxygenPlayerData.EnumStatus status : OxygenPlayerData.EnumStatus.values()) {
            profileElement = new StatusGUIDropDownElement(status);
            profileElement.setDisplayText(status.getLocalizedName());
            profileElement.enableDynamicBackground(GUISettings.instance().getEnabledContextActionColor(), GUISettings.instance().getDisabledContextActionColor(), GUISettings.instance().getHoveredContextActionColor());
            profileElement.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            this.statusDropDownList.addElement(profileElement);
        }
        this.addElement(this.statusDropDownList); 

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.addElement(this.cooldownTextLabel = new GUITextLabel(64, 138).setTextScale(GUISettings.instance().getTextScale()).disableFull());  
            this.cooldown = true;
        }      

        if (!OxygenGUIHelper.isNeedSync(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID) || OxygenGUIHelper.isDataRecieved(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID))
            this.sortPlayers(0);

        OxygenGUIHelper.screenInitialized(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
    }

    public static OxygenPlayerData.EnumStatus getPlayerStatus(SharedPlayerData sharedData) {
        return OxygenPlayerData.EnumStatus.values()[sharedData.getData(OxygenMain.STATUS_DATA_ID).get(0)];
    }

    public void sortPlayers(int mode) {
        this.playerList.clear();

        SharedPlayerData clientSharedData = OxygenHelperClient.getSharedClientPlayerData();
        int jProfile;
        for (SharedPlayerData sharedData : OxygenHelperClient.getSharedPlayersData()) {
            jProfile = sharedData.getData(TeleportationMain.JUMP_PROFILE_DATA_ID).get(0);
            if (OxygenHelperClient.isOnline(sharedData.getPlayerUUID()) 
                    && (this.getPlayerStatus(sharedData) != OxygenPlayerData.EnumStatus.OFFLINE || PrivilegeProviderClient.getPrivilegeValue(EnumOxygenPrivileges.EXPOSE_PLAYERS_OFFLINE.toString(), false))
                    && (jProfile != TeleportationPlayerData.EnumJumpProfile.DISABLED.ordinal() || PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false)) 
                    && sharedData != clientSharedData)
                this.playerList.add(sharedData);
        }

        List<SharedPlayerData> players = new ArrayList<SharedPlayerData>(this.playerList);
        if (mode == 0 || mode == 1) {//by status, starting from /online/
            Collections.sort(players, new Comparator<SharedPlayerData>() {

                @Override
                public int compare(SharedPlayerData player1, SharedPlayerData player2) {
                    if (mode == 0)
                        return getPlayerStatus(player1).ordinal() - getPlayerStatus(player2).ordinal();
                    else
                        return getPlayerStatus(player2).ordinal() - getPlayerStatus(player1).ordinal();
                }
            });
        } else if (mode == 2 || mode == 3) {//by username, A-z
            Collections.sort(players, new Comparator<SharedPlayerData>() {

                @Override
                public int compare(SharedPlayerData player1, SharedPlayerData player2) {
                    if (mode == 0)
                        return player1.getUsername().compareTo(player2.getUsername());
                    else
                        return player2.getUsername().compareTo(player1.getUsername());
                }
            });
        }

        this.playersListPanel.reset();
        PlayerGUIButton button;
        for (SharedPlayerData sharedData : players) {
            button = new PlayerGUIButton(
                    sharedData,
                    sharedData.getUsername(), 
                    EnumDimensions.getLocalizedNameFromId(OxygenHelperClient.getPlayerDimension(sharedData)), 
                    getJumpProfile(sharedData).getLocalizedName(),
                    OxygenHelperClient.getPlayerStatus(sharedData.getPlayerUUID()));
            button.enableDynamicBackground(GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getHoveredElementColor());
            button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            this.playersListPanel.addButton(button);
        }

        this.playersAmountTextLabel.setDisplayText(String.valueOf(this.playerList.size()) + " / " + String.valueOf(OxygenHelperClient.getMaxPlayers()));   
        this.playersAmountTextLabel.setX(325 - this.textWidth(this.playersAmountTextLabel.getDisplayText(), GUISettings.instance().getSubTextScale()));

        this.playersListPanel.getScroller().resetPosition();
        this.playersListPanel.getScroller().getSlider().reset();

        this.sortUpStatusButton.toggle();
        this.sortDownStatusButton.setToggled(false);
        this.sortDownUsernameButton.setToggled(false);
        this.sortUpUsernameButton.setToggled(false);
    }

    public static TeleportationPlayerData.EnumJumpProfile getJumpProfile(SharedPlayerData sharedData) {
        return TeleportationPlayerData.EnumJumpProfile.values()[sharedData.getData(TeleportationMain.JUMP_PROFILE_DATA_ID).get(0)];
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.searchTextField.isEnabled() && !this.searchTextField.isHovered())
            this.searchTextField.disableFull();
        return super.mouseClicked(mouseX, mouseY, mouseButton);              
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (element == this.campsPageButton)                  
            this.screen.getCampsSection().open();
        else if (element == this.locationsPageButton)
            this.screen.getLocationsSection().open();
        else if (element == this.searchButton)
            this.searchTextField.enableFull();
        else if (element == this.sortDownStatusButton) {
            if (!this.sortDownStatusButton.isToggled()) {
                this.sortPlayers(1);
                this.sortUpStatusButton.setToggled(false);
                this.sortDownStatusButton.toggle(); 

                this.sortDownUsernameButton.setToggled(false);
                this.sortUpUsernameButton.setToggled(false);
            }
        } else if (element == this.sortUpStatusButton) {
            if (!this.sortUpStatusButton.isToggled()) {
                this.sortPlayers(0);
                this.sortDownStatusButton.setToggled(false);
                this.sortUpStatusButton.toggle();

                this.sortDownUsernameButton.setToggled(false);
                this.sortUpUsernameButton.setToggled(false);
            }
        } else if (element == this.sortDownUsernameButton) {
            if (!this.sortDownUsernameButton.isToggled()) {
                this.sortPlayers(3);
                this.sortUpUsernameButton.setToggled(false);
                this.sortDownUsernameButton.toggle(); 

                this.sortDownStatusButton.setToggled(false);
                this.sortUpStatusButton.setToggled(false);
            }
        } else if (element == this.sortUpUsernameButton) {
            if (!this.sortUpUsernameButton.isToggled()) {
                this.sortPlayers(2);
                this.sortDownUsernameButton.setToggled(false);
                this.sortUpUsernameButton.toggle();

                this.sortDownStatusButton.setToggled(false);
                this.sortUpStatusButton.setToggled(false);
            }
        } else if (element == this.refreshButton) {
            this.searchTextField.reset();
            this.sortPlayers(0);
            this.resetPlayerInfo();
        } else if (element == this.moveButton) {
            TeleportationManagerClient.instance().getPlayersManager().moveToPlayerSynced(this.currentPlayer.getPlayerUUID());
            this.screen.close();        
        } else if (element instanceof JumpProfileGUIDropDownElement) {
            JumpProfileGUIDropDownElement profileButton = (JumpProfileGUIDropDownElement) element;
            if (profileButton.profile != this.currentJumpProfile) {
                TeleportationManagerClient.instance().getPlayersManager().changeJumpProfileSynced(profileButton.profile);
                this.currentJumpProfile = profileButton.profile;
                return;
            }
        } else if (element instanceof PlayerGUIButton) {
            PlayerGUIButton button = (PlayerGUIButton) element;
            if (button != this.currentButton) {
                if (this.currentButton != null)
                    this.currentButton.setToggled(false);
                this.currentButton = button;
                this.currentPlayer = button.playerData;
                button.toggle();                    
                this.showPlayerInfo();
            }
        } else if (element instanceof StatusGUIDropDownElement) {
            StatusGUIDropDownElement profileButton = (StatusGUIDropDownElement) element;
            if (profileButton.status != this.currentStatus) {
                OxygenManagerClient.instance().getFriendListManager().changeStatusSynced(profileButton.status);
                this.currentStatus = profileButton.status;
                this.statusImageLabel.setTextureUV(this.currentStatus.ordinal() * 3, 0);
            }
        }
    }

    private void showPlayerInfo() {
        this.targetUsernameTextLabel.setDisplayText(this.currentPlayer.getUsername());
        this.targetDimensionTextLabel.setDisplayText(EnumDimensions.getLocalizedNameFromId(OxygenHelperClient.getPlayerDimension(this.currentPlayer)));
        TeleportationPlayerData.EnumJumpProfile jumpProfile = this.getJumpProfile(this.currentPlayer);
        switch (jumpProfile) {
        case DISABLED:
            if (PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
                this.moveButton.setDisplayText(this.moveToStr, true);
            break;
        case FREE:
            this.moveButton.setDisplayText(this.moveToStr, true);
            break;
        case REQUEST:
            if (PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
                this.moveButton.setDisplayText(this.moveToStr, true);
            else
                this.moveButton.setDisplayText(this.requestStr, true);
            break;
        }
        this.targetUsernameTextLabel.enableFull();
        this.targetDimensionTextLabel.enableFull();
        this.moveButton.enableFull();

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.cooldownTextLabel.enableFull();
            this.moveButton.disable();
        }
    }

    private void resetPlayerInfo() {
        this.targetUsernameTextLabel.disableFull();
        this.targetDimensionTextLabel.disableFull();
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
        return MathHelper.clamp((int) (this.teleportationCooldown - (System.currentTimeMillis() - TeleportationManagerClient.instance().getPlayerData().getCooldownInfo().getLastJumpTime())), 
                0, this.teleportationCooldown);
    }
}
