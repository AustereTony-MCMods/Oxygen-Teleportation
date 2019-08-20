package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.list.GUIDropDownList;
import austeretony.alternateui.screen.panel.GUIButtonPanel;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.alternateui.util.EnumGUIOrientation;
import austeretony.oxygen.client.OxygenManagerClient;
import austeretony.oxygen.client.api.OxygenGUIHelper;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.api.WatcherHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.IndexedGUIDropDownElement;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen.common.api.EnumDimension;
import austeretony.oxygen.common.main.EnumOxygenPrivilege;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.main.OxygenPlayerData.EnumActivityStatus;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen.common.main.SharedPlayerData;
import austeretony.oxygen.util.MathUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.players.PlayerGUIButton;
import austeretony.oxygen_teleportation.client.gui.teleportation.players.PlayersBackgroundGUIFiller;
import austeretony.oxygen_teleportation.client.input.TeleportationMenuKeyHandler;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData.EnumJumpProfile;
import austeretony.oxygen_teleportation.common.main.WorldPoint;

public class PlayersGUISection extends AbstractGUISection {

    private final TeleportationMenuGUIScreen screen;

    private GUITextLabel playersAmountTextLabel, targetUsernameTextLabel, targetDimensionTextLabel, cooldownTextLabel;

    private GUIButton campsPageButton, locationsPageButton, searchButton, refreshButton, moveButton, sortDownStatusButton, sortUpStatusButton, 
    sortDownUsernameButton, sortUpUsernameButton;

    private PlayerGUIButton currentButton;

    private GUIButtonPanel playersListPanel;

    private GUITextField searchField;

    private GUIDropDownList statusDropDownList, jumpProfileDropDownList;

    private GUIImageLabel statusImageLabel;

    private SharedPlayerData currentPlayer;

    private EnumActivityStatus clientStatus;

    private EnumJumpProfile currentJumpProfile;

    private AdvancedBalanceGUIElement feeElement, balanceElement;

    private final String 
    moveToStr = ClientReference.localize("teleportation.gui.menu.moveButton"), 
    requestStr = ClientReference.localize("teleportation.gui.menu.requestButton");

    private final int teleportationCooldown = PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN.toString(), 
            TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) * 1000;

    private boolean cooldown;

    public PlayersGUISection(TeleportationMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {              
        this.addElement(new PlayersBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 4).setDisplayText(ClientReference.localize("teleportation.gui.menu.playersTitle"), false, GUISettings.instance().getTitleScale()));     
        this.addElement(new GUITextLabel(111, 27).setDisplayText(ClientReference.localize("oxygen.gui.username"), false, GUISettings.instance().getSubTextScale())); 
        this.addElement(new GUITextLabel(197, 27).setDisplayText(ClientReference.localize("oxygen.gui.dimension"), false, GUISettings.instance().getSubTextScale())); 
        this.addElement(new GUITextLabel(282, 27).setDisplayText(ClientReference.localize("teleportation.gui.menu.players.profile"), false, GUISettings.instance().getSubTextScale())); 

        this.addElement(this.campsPageButton = new GUIButton(this.getWidth() - 44, 0,  12, 12).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(TeleportationGUITextures.CAMP_ICONS, 12, 12).initSimpleTooltip(ClientReference.localize("teleportation.gui.menu.tooltip.camps"), GUISettings.instance().getTooltipScale()));   
        this.addElement(this.locationsPageButton = new GUIButton(this.getWidth() - 30, 0, 12, 12).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(TeleportationGUITextures.LOCATION_ICONS, 14, 14).initSimpleTooltip(ClientReference.localize("teleportation.gui.menu.tooltip.locations"), GUISettings.instance().getTooltipScale()));   
        this.addElement(new GUIButton(this.getWidth() - 15, 0, 12, 12).setTexture(TeleportationGUITextures.PLAYERS_ICONS, 14, 14).initSimpleTooltip(ClientReference.localize("teleportation.gui.menu.tooltip.players"), GUISettings.instance().getTooltipScale()).toggle());
        if (!TeleportationConfig.ENABLE_CAMPS.getBooleanValue())
            this.campsPageButton.disable();
        if (!TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue())
            this.locationsPageButton.disable();

        this.addElement(this.searchButton = new GUIButton(91, 15, 7, 7).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SEARCH_ICONS, 7, 7).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.search"), GUISettings.instance().getTooltipScale()));  

        this.addElement(this.sortDownStatusButton = new GUIButton(94, 31, 3, 3).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SORT_DOWN_ICONS, 3, 3).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale()));   
        this.addElement(this.sortUpStatusButton = new GUIButton(94, 27, 3, 3).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SORT_UP_ICONS, 3, 3).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 
        this.addElement(this.sortDownUsernameButton = new GUIButton(106, 31, 3, 3).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SORT_DOWN_ICONS, 3, 3).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale()));   
        this.addElement(this.sortUpUsernameButton = new GUIButton(106, 27, 3, 3).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SORT_UP_ICONS, 3, 3).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 

        this.addElement(this.refreshButton = new GUIButton(175, 14, 10, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.REFRESH_ICONS, 9, 9).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.refresh"), GUISettings.instance().getTooltipScale()));
        this.addElement(this.playersAmountTextLabel = new GUITextLabel(0, 15).setTextScale(GUISettings.instance().getSubTextScale()));   

        this.playersListPanel = new GUIButtonPanel(EnumGUIOrientation.VERTICAL, 87, 39, 237, 10).setButtonsOffset(1).setTextScale(GUISettings.instance().getPanelTextScale());
        this.addElement(this.playersListPanel);
        this.addElement(this.searchField = new GUITextField(87, 14, 85, 9, WorldPoint.MAX_NAME_LENGTH).setTextScale(GUISettings.instance().getSubTextScale())
                .enableDynamicBackground(GUISettings.instance().getEnabledTextFieldColor(), GUISettings.instance().getDisabledTextFieldColor(), GUISettings.instance().getHoveredTextFieldColor())
                .setLineOffset(3).setDisplayText("...").cancelDraggedElementLogic().disableFull());
        this.playersListPanel.initSearchField(this.searchField);
        GUIScroller scroller = new GUIScroller(MathUtils.clamp(OxygenHelperClient.getMaxPlayers(), 10, 1000), 10);
        this.playersListPanel.initScroller(scroller);
        GUISlider slider = new GUISlider(325, 39, 2, 110);
        slider.setDynamicBackgroundColor(GUISettings.instance().getEnabledSliderColor(), GUISettings.instance().getDisabledSliderColor(), GUISettings.instance().getHoveredSliderColor());
        scroller.initSlider(slider);

        this.addElement(this.targetUsernameTextLabel = new GUITextLabel(3, 83).setTextScale(GUISettings.instance().getTextScale()));  
        this.addElement(this.targetDimensionTextLabel = new GUITextLabel(3, 91).setTextScale(GUISettings.instance().getSubTextScale()));    
        this.addElement(this.moveButton = new GUIButton(3, 116, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor())
                .setTextScale(GUISettings.instance().getButtonTextScale()).enableTextShadow().disableFull());

        SharedPlayerData clientSharedData = OxygenHelperClient.getSharedClientPlayerData();
        this.clientStatus = OxygenHelperClient.getPlayerStatus(clientSharedData);

        this.addElement(new GUITextLabel(3, 33).setDisplayText(clientSharedData.getUsername(), false, GUISettings.instance().getTextScale()));  
        this.addElement(new GUITextLabel(3, 50).setDisplayText(EnumDimension.getLocalizedNameFromId(OxygenHelperClient.getPlayerDimension(clientSharedData)), false, GUISettings.instance().getSubTextScale()));   

        EnumJumpProfile clientJumpProfile = getJumpProfile(clientSharedData);
        this.currentJumpProfile = clientJumpProfile;
        this.jumpProfileDropDownList = new GUIDropDownList(2, 61, 70, 10).setDisplayText(clientJumpProfile.localizedName())
                .setScale(GUISettings.instance().getDropDownListScale()).setTextScale(GUISettings.instance().getTextScale()).setTextAlignment(EnumGUIAlignment.LEFT, 1);
        this.jumpProfileDropDownList.setOpenSound(OxygenSoundEffects.DROP_DOWN_LIST_OPEN.soundEvent);
        this.jumpProfileDropDownList.setCloseSound(OxygenSoundEffects.CONTEXT_CLOSE.soundEvent);
        IndexedGUIDropDownElement<EnumJumpProfile> jumpProfileElement;
        for (EnumJumpProfile jumpProfile : EnumJumpProfile.values()) {
            jumpProfileElement = new IndexedGUIDropDownElement(jumpProfile);
            jumpProfileElement.setDisplayText(jumpProfile.localizedName());
            jumpProfileElement.enableDynamicBackground(GUISettings.instance().getEnabledContextActionColor(), GUISettings.instance().getDisabledContextActionColor(), GUISettings.instance().getHoveredContextActionColor());
            jumpProfileElement.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            this.jumpProfileDropDownList.addElement(jumpProfileElement);
        }
        this.addElement(this.jumpProfileDropDownList);   

        this.addElement(this.statusImageLabel = new GUIImageLabel(3, 44).setTexture(OxygenGUITextures.STATUS_ICONS, 3, 3, this.clientStatus.ordinal() * 3, 0, 12, 3));   
        this.statusDropDownList = new GUIDropDownList(9, 42, GUISettings.instance().getContextMenuWidth(), 10).setScale(GUISettings.instance().getDropDownListScale()).setDisplayText(this.clientStatus.localizedName()).setTextScale(GUISettings.instance().getTextScale()).setTextAlignment(EnumGUIAlignment.LEFT, 1);
        this.statusDropDownList.setOpenSound(OxygenSoundEffects.DROP_DOWN_LIST_OPEN.soundEvent);
        this.statusDropDownList.setCloseSound(OxygenSoundEffects.CONTEXT_CLOSE.soundEvent);
        IndexedGUIDropDownElement<EnumActivityStatus> statusElement;
        for (EnumActivityStatus status : EnumActivityStatus.values()) {
            statusElement = new IndexedGUIDropDownElement(status);
            statusElement.setDisplayText(status.localizedName());
            statusElement.enableDynamicBackground(GUISettings.instance().getEnabledContextActionColor(), GUISettings.instance().getDisabledContextActionColor(), GUISettings.instance().getHoveredContextActionColor());
            statusElement.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            this.statusDropDownList.addElement(statusElement);
        }
        this.addElement(this.statusDropDownList); 

        this.addElement(this.feeElement = new AdvancedBalanceGUIElement(76, 118).disableFull());   
        this.addElement(this.balanceElement = new AdvancedBalanceGUIElement(76, 139).disableFull()); 
        this.feeElement.setItemStack(this.screen.feeStack);
        this.balanceElement.setItemStack(this.screen.feeStack);

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.addElement(this.cooldownTextLabel = new GUITextLabel(3, 129).setTextScale(GUISettings.instance().getTextScale()).disableFull());  
            this.cooldown = true;
        }      
    }

    public void sortPlayers(int mode) {    
        List<SharedPlayerData> players = OxygenHelperClient.getSharedPlayersData()
                .stream()
                .filter(s->OxygenHelperClient.isOnline(s.getPlayerUUID()) 
                        && (OxygenHelperClient.getPlayerStatus(s) != EnumActivityStatus.OFFLINE || PrivilegeProviderClient.getPrivilegeValue(EnumOxygenPrivilege.EXPOSE_PLAYERS_OFFLINE.toString(), false))
                        && (s.getByte(TeleportationMain.JUMP_PROFILE_SHARED_DATA_ID) != TeleportationPlayerData.EnumJumpProfile.DISABLED.ordinal() || PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false)))
                .collect(Collectors.toList());

        players.remove(OxygenHelperClient.getSharedClientPlayerData());

        if (mode == 0)
            Collections.sort(players, (s1, s2)->OxygenHelperClient.getPlayerStatus(s1).ordinal() - OxygenHelperClient.getPlayerStatus(s2).ordinal());
        else if (mode == 1)
            Collections.sort(players, (s1, s2)->OxygenHelperClient.getPlayerStatus(s2).ordinal() - OxygenHelperClient.getPlayerStatus(s1).ordinal());
        else if (mode == 2)
            Collections.sort(players, (s1, s2)->s1.getUsername().compareTo(s2.getUsername()));
        else if (mode == 3)
            Collections.sort(players, (s1, s2)->s2.getUsername().compareTo(s1.getUsername()));

        this.playersListPanel.reset();
        PlayerGUIButton button;
        for (SharedPlayerData sharedData : players) {
            button = new PlayerGUIButton(
                    sharedData.getPlayerUUID(),
                    sharedData.getUsername(), 
                    EnumDimension.getLocalizedNameFromId(OxygenHelperClient.getPlayerDimension(sharedData)), 
                    getJumpProfile(sharedData).localizedName(),
                    OxygenHelperClient.getPlayerStatus(sharedData.getPlayerUUID()));
            button.enableDynamicBackground(GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getHoveredElementColor());
            button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            this.playersListPanel.addButton(button);
        }

        this.playersAmountTextLabel.setDisplayText(String.valueOf(players.size()) + " / " + String.valueOf(OxygenHelperClient.getMaxPlayers()));   
        this.playersAmountTextLabel.setX(325 - this.textWidth(this.playersAmountTextLabel.getDisplayText(), GUISettings.instance().getSubTextScale()));

        this.searchField.reset();

        this.playersListPanel.getScroller().resetPosition();
        this.playersListPanel.getScroller().getSlider().reset();

        this.sortUpStatusButton.toggle();
        this.sortDownStatusButton.setToggled(false);
        this.sortDownUsernameButton.setToggled(false);
        this.sortUpUsernameButton.setToggled(false);
    }

    public static EnumJumpProfile getJumpProfile(SharedPlayerData sharedData) {
        return EnumJumpProfile.values()[sharedData.getByte(TeleportationMain.JUMP_PROFILE_SHARED_DATA_ID)];
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.searchField.isEnabled() && !this.searchField.isHovered()) {
            this.searchButton.enableFull();
            this.searchField.disableFull();
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);              
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.campsPageButton)                  
                this.screen.getCampsSection().open();
            else if (element == this.locationsPageButton)
                this.screen.getLocationsSection().open();
            else if (element == this.searchButton) {
                this.searchField.enableFull();
                this.searchButton.disableFull();
            } else if (element == this.sortDownStatusButton) {
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
                this.searchField.reset();
                this.sortPlayers(0);
                this.resetPlayerInfo();
            } else if (element == this.moveButton) {
                TeleportationManagerClient.instance().getPlayersManager().moveToPlayerSynced(this.currentPlayer.getIndex());
                this.screen.close();        
            }
        }
        if (element instanceof IndexedGUIDropDownElement) {
            if (((IndexedGUIDropDownElement) element).index instanceof EnumActivityStatus) {
                IndexedGUIDropDownElement<EnumActivityStatus> profileButton = (IndexedGUIDropDownElement) element;
                if (profileButton.index != this.clientStatus) {
                    OxygenManagerClient.instance().changeActivityStatusSynced(profileButton.index);
                    this.clientStatus = profileButton.index;
                    this.statusImageLabel.setTextureUV(this.clientStatus.ordinal() * 3, 0);
                }
            } else {
                IndexedGUIDropDownElement<EnumJumpProfile> profileButton = (IndexedGUIDropDownElement) element;
                if (profileButton.index != this.currentJumpProfile) {
                    TeleportationManagerClient.instance().getPlayersManager().changeJumpProfileSynced(profileButton.index);
                    this.currentJumpProfile = profileButton.index;
                    return;
                }
            }
        } else if (element instanceof PlayerGUIButton) {
            PlayerGUIButton button = (PlayerGUIButton) element;
            if (button != this.currentButton) {
                if (this.currentButton != null)
                    this.currentButton.setToggled(false);
                this.currentButton = button;
                this.currentPlayer = OxygenHelperClient.getSharedPlayerData(button.index);
                button.toggle();                    
                this.showPlayerInfo();
            }
        }
    }

    private void showPlayerInfo() {
        this.targetUsernameTextLabel.setDisplayText(this.currentPlayer.getUsername());
        this.targetDimensionTextLabel.setDisplayText(EnumDimension.getLocalizedNameFromId(OxygenHelperClient.getPlayerDimension(this.currentPlayer)));
        TeleportationPlayerData.EnumJumpProfile jumpProfile = this.getJumpProfile(this.currentPlayer);
        switch (jumpProfile) {
        case DISABLED:
            if (PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
                this.moveButton.setDisplayText(this.moveToStr, true);
            break;
        case FREE:
            this.moveButton.setDisplayText(this.moveToStr, true);
            break;
        case REQUEST:
            if (PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
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

        int fee = PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.JUMP_TO_PLAYER_FEE.toString(), TeleportationConfig.JUMP_TO_PLAYER_FEE.getIntValue());
        if (fee > 0) {
            this.feeElement.setBalance(fee);
            if (this.screen.feeStack == null)
                this.balanceElement.setBalance(WatcherHelperClient.getInt(OxygenPlayerData.CURRENCY_COINS_WATCHER_ID));
            else
                this.balanceElement.setBalance(this.screen.feeStackBalance);
            this.feeElement.enableFull();
            this.balanceElement.enableFull();

            if (fee > this.balanceElement.getBalance()) {
                this.moveButton.disable();
                this.feeElement.setRed(true);
            }
        }
    }

    private void resetPlayerInfo() {
        this.targetUsernameTextLabel.disableFull();
        this.targetDimensionTextLabel.disableFull();
        this.moveButton.disableFull();

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown)
            this.cooldownTextLabel.disableFull();

        this.feeElement.disableFull();
        this.balanceElement.disableFull();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.searchField.isDragged() && !this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == TeleportationMenuGUIScreen.TELEPORTATIOIN_MENU_ENTRY.index + 2)
                    this.screen.close();
            } else if (keyCode == TeleportationMenuKeyHandler.TELEPORTATION_MENU.getKeyCode())
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
        return MathUtils.clamp((int) (this.teleportationCooldown - (System.currentTimeMillis() - TeleportationManagerClient.instance().getPlayerData().getCooldownInfo().getLastJumpTime())), 
                0, this.teleportationCooldown);
    }
}
