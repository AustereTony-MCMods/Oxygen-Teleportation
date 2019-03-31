package austeretony.teleportation.client.gui.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.list.GUIDropDownElement;
import austeretony.alternateui.screen.list.GUIDropDownList;
import austeretony.alternateui.screen.panel.SearchableGUIButtonPanel;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.client.gui.menu.players.PlayerGUIButton;
import austeretony.teleportation.client.handler.KeyHandler;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.PlayerProfile.EnumJumpProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.menu.players.PlayersManagerClient;
import austeretony.teleportation.common.world.EnumDimensions;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayersGUISection extends AbstractGUISection {

    private final MenuGUIScreen screen;

    private GUITextLabel playersAmountLabel, targetUsernameLabel, targetDimensionLabel, cooldownLabel;

    private GUIButton campsPageButton, locationsPageButton, playersPageButton, searchButton, refreshButton, moveButton, sortUpButton, sortDownButton;

    private PlayerGUIButton currentButton;

    private SearchableGUIButtonPanel playersListPanel;

    private GUITextField searchField;

    private GUIDropDownList profileSettingsList;

    private OxygenPlayerData currentPlayer;

    private final Map<PlayerGUIButton, OxygenPlayerData> mappedPlayers = new HashMap<PlayerGUIButton, OxygenPlayerData>();

    private final Map<GUIDropDownElement, EnumJumpProfile> mappedProfiles = new HashMap<GUIDropDownElement, EnumJumpProfile>();

    private GUIDropDownElement currentProfile;

    private final Set<OxygenPlayerData> playerList = new TreeSet<OxygenPlayerData>();

    private String 
    moveToStr = I18n.format("teleportation.menu.moveButton"), 
    requestStr = I18n.format("teleportation.menu.requestButton");

    private final int teleportationCooldown = PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.PLAYER_TELEPORTATION_COOLDOWN.toString(), 
            TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) * 1000;

    private boolean cooldown;

    public PlayersGUISection(MenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    protected void init() {               
        OxygenPlayerData clientData = OxygenHelperClient.getPlayerData(OxygenHelperClient.getPlayerUUID());
        int jumpProfile;
        for (OxygenPlayerData playerData : OxygenHelperClient.getPlayersData().values()) {
            jumpProfile = playerData.getData(TeleportationMain.JUMP_PROFILE_DATA_ID).get(0);
            if (OxygenHelperClient.isPlayerOnline(playerData.getUUID()) && (CampsManagerClient.instance().isOpped() || jumpProfile != PlayerProfile.EnumJumpProfile.DISABLED.ordinal()) 
                    && !playerData.getUsername().equals(clientData.getUsername()))
                this.playerList.add(playerData);
        }

        this.addElement(new GUIImageLabel(- 1, - 1, this.getWidth() + 2, this.getHeight() + 2).enableStaticBackground(0xC8202020));//main background
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), 15).enableStaticBackground(0xFF101010));//title background
        this.addElement(new GUIImageLabel(0, 17, 85, 65).enableStaticBackground(0xFF101010));//client profile background
        this.addElement(new GUIImageLabel(87, 17, 85, 9).enableStaticBackground(0xFF101010));//players list search background
        this.addElement(new GUIImageLabel(173, 17, 156, 9).enableStaticBackground(0xFF101010));//players list amount background
        this.addElement(new GUIImageLabel(87, 27, 240, 15).enableStaticBackground(0xFF101010));//players list sorters background
        this.addElement(new GUIImageLabel(87, 43, 236, 109).enableStaticBackground(0xFF101010));//players list background
        this.addElement(new GUIImageLabel(324, 43, 3, 109).enableStaticBackground(0xFF101010)); //slider background
        this.addElement(new GUIImageLabel(0, 84, 85, 68).enableStaticBackground(0xFF101010));//point player background
        this.addElement(new GUITextLabel(2, 4).setDisplayText(I18n.format("teleportation.menu.playersTitle"))); 
        this.addElement(new GUITextLabel(2, 20).setDisplayText(I18n.format("teleportation.menu.players.client")).setTextScale(0.8F)); 
        this.addElement(new GUITextLabel(2, 88).setDisplayText(I18n.format("teleportation.menu.players.target")).setTextScale(0.8F));        
        this.addElement(new GUITextLabel(89, 30).setDisplayText(I18n.format("teleportation.menu.players.username")).setTextScale(0.8F)); 
        this.addElement(new GUITextLabel(180, 30).setDisplayText(I18n.format("teleportation.menu.players.dimension")).setTextScale(0.8F)); 
        this.addElement(new GUITextLabel(282, 30).setDisplayText(I18n.format("teleportation.menu.players.profile")).setTextScale(0.8F)); 

        this.addElement(this.campsPageButton = new GUIButton(this.getWidth() - 44, 1,  14, 14).setTexture(MenuGUIScreen.CAMP_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.camps"), 0.8F));   
        this.addElement(this.locationsPageButton = new GUIButton(this.getWidth() - 30, 1, 14, 14).setTexture(MenuGUIScreen.LOCATION_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.locations"), 0.8F));   
        this.addElement(this.playersPageButton = new GUIButton(this.getWidth() - 15, 1, 14, 14).setTexture(MenuGUIScreen.PLAYERS_ICONS, 14, 14).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.players"), 0.8F).toggle());
        if (!TeleportationConfig.ENABLE_CAMPS.getBooleanValue())
            this.campsPageButton.disable();
        if (!TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue())
            this.locationsPageButton.disable();

        this.addElement(this.searchButton = new GUIButton(94, 18, 7, 7).setTexture(MenuGUIScreen.SEARCH_ICONS, 7, 7).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.search"), 0.8F));  
        this.addElement(this.sortDownButton = new GUIButton(89, 22, 3, 3).setTexture(MenuGUIScreen.SORT_DOWN_ICONS, 3, 3).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.sort"), 0.8F)); 
        this.addElement(this.sortUpButton = new GUIButton(89, 18, 3, 3).setTexture(MenuGUIScreen.SORT_UP_ICONS, 3, 3).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.sort"), 0.8F)); 
        this.addElement(this.refreshButton = new GUIButton(0, 17, 10, 10).setTexture(MenuGUIScreen.REFRESH_ICONS, 10, 10).initSimpleTooltip(I18n.format("teleportation.menu.tooltip.refresh"), 0.8F));
        this.addElement(this.playersAmountLabel = new GUITextLabel(0, 18).setTextScale(0.7F).setDisplayText(String.valueOf(this.playerList.size()) + 
                " / " + String.valueOf(OxygenHelperClient.getMaxPlayers())));   
        this.playersAmountLabel.setX(325 - (int) ((float) this.width(this.playersAmountLabel.getDisplayText()) * 0.7F));
        this.refreshButton.setX(this.playersAmountLabel.getX() - 11);
        this.playersListPanel = new SearchableGUIButtonPanel(87, 43, 236, 10, 10);
        this.playersListPanel.setButtonsOffset(1);
        this.playersListPanel.setTextScale(0.8F);
        this.addElement(this.playersListPanel);
        this.addElement(this.searchField = new GUITextField(87, 18, 98, 16).setScale(0.8F).enableDynamicBackground().setDisplayText("...", false, 0.8F).disableFull());
        this.playersListPanel.initSearchField(this.searchField);
        GUIScroller panelScroller = new GUIScroller(this.playerList.size() > 10 ? this.playerList.size() : 10, 10);
        this.playersListPanel.initScroller(panelScroller);
        GUISlider panelSlider = new GUISlider(324, 43, 3, 109);
        panelScroller.initSlider(panelSlider);

        this.addElement(this.targetUsernameLabel = new GUITextLabel(3, 104));  
        this.addElement(this.targetDimensionLabel = new GUITextLabel(3, 114).setTextScale(0.7F));    
        this.addElement(this.moveButton = new GUIButton(22, 140,  40, 10).enableDynamicBackground(0xFF404040, 0xFF202020, 0xFF606060).setTextScale(0.8F).disableFull());

        this.updatePlayers();
        this.sortDownButton.toggle(); 

        this.addElement(new GUITextLabel(3, 36).setDisplayText(clientData.getUsername()));  
        this.addElement(new GUITextLabel(3, 46).setDisplayText(EnumDimensions.getLocalizedNameFromId(clientData.getDimension())).setTextScale(0.7F));   
        this.addElement(new GUITextLabel(3, 56).setDisplayText(I18n.format("teleportation.menu.players.profile") + ":").setTextScale(0.8F)); 
        PlayerProfile.EnumJumpProfile clientProfile = PlayersManagerClient.getPlayerJumpProfile();
        this.profileSettingsList = new GUIDropDownList(this.width(I18n.format("teleportation.menu.players.profile")), 56, 70, 10).setDisplayText(clientProfile.getLocalizedName()).setTextScale(0.8F).setTextAlignment(EnumGUIAlignment.LEFT, 1);
        GUIDropDownElement dropDownElement = new GUIDropDownElement().setDisplayText(clientProfile.getLocalizedName());
        this.profileSettingsList.addElement(dropDownElement);
        this.mappedProfiles.put(dropDownElement, clientProfile);
        this.currentProfile = dropDownElement;
        PlayerProfile.EnumJumpProfile profile;
        for (int i = 0; i < PlayerProfile.EnumJumpProfile.values().length; i++) {
            profile = PlayerProfile.EnumJumpProfile.values()[i];
            if (!this.mappedProfiles.values().contains(profile)) {
                dropDownElement = new GUIDropDownElement().setDisplayText(profile.getLocalizedName());
                this.profileSettingsList.addElement(dropDownElement);
                this.mappedProfiles.put(dropDownElement, profile);
            }
        }
        this.addElement(this.profileSettingsList);   

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.addElement(this.cooldownLabel = new GUITextLabel(64, 141).setTextScale(0.7F).disableFull());  
            this.cooldown = true;
        }
    }

    public void updatePlayers() {
        this.playersListPanel.reset();
        this.mappedPlayers.clear();
        PlayerGUIButton button;
        for (OxygenPlayerData playerData : this.playerList) {
            button = new PlayerGUIButton(
                    playerData.getUsername(), 
                    EnumDimensions.getLocalizedNameFromId(playerData.getDimension()), 
                    PlayersManagerClient.getPlayerJumpProfile(playerData.getUUID()).getLocalizedName());
            button.enableDynamicBackground(0xFF151515, 0xFF101010, 0xFF303030);
            button.setTextDynamicColor(0xFFB2B2B2, 0xFF8C8C8C, 0xFFD1D1D1);
            this.playersListPanel.addButton(button);
            this.mappedPlayers.put(button, playerData);
        }
    }

    private void sortPlayers(int mode) {
        List<OxygenPlayerData> players = new ArrayList<OxygenPlayerData>(this.playerList);
        Collections.sort(players, new Comparator<OxygenPlayerData>() {

            @Override
            public int compare(OxygenPlayerData player1, OxygenPlayerData player2) {
                if (mode == 0)
                    return player1.getUsername().compareTo(player2.getUsername());
                else
                    return player2.getUsername().compareTo(player1.getUsername());
            }
        });
        this.playersListPanel.reset();
        this.mappedPlayers.clear();
        PlayerGUIButton button;
        for (OxygenPlayerData playerData : players) {
            button = new PlayerGUIButton(
                    playerData.getUsername(), 
                    EnumDimensions.getLocalizedNameFromId(playerData.getDimension()), 
                    PlayersManagerClient.getPlayerJumpProfile(playerData.getUUID()).getLocalizedName());
            button.enableDynamicBackground(0xFF151515, 0xFF101010, 0xFF303030);
            button.setTextDynamicColor(0xFFB2B2B2, 0xFF8C8C8C, 0xFFD1D1D1);
            this.playersListPanel.addButton(button);
            this.mappedPlayers.put(button, playerData);
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
        if (element == this.campsPageButton)                  
            this.screen.campsSection.open();
        else if (element == this.locationsPageButton)
            this.screen.locationsSection.open();
        else if (element == this.searchButton)
            this.searchField.enableFull();
        else if (element == this.sortDownButton) {
            if (!this.sortDownButton.isToggled()) {
                this.sortPlayers(1);
                this.sortUpButton.setToggled(false);
                this.sortDownButton.toggle(); 
            }
        } else if (element == this.sortUpButton) {
            if (!this.sortUpButton.isToggled()) {
                this.sortPlayers(0);
                this.sortDownButton.setToggled(false);
                this.sortUpButton.toggle();
            }
        } else if (element == this.refreshButton) {
            this.searchField.reset();
            this.updatePlayers();
            this.resetPlayerInfo();
        } else if (element == this.moveButton) {
            PlayersManagerClient.instance().moveToPlayerSynced(this.currentPlayer.getUUID());
            this.screen.close();        
        } else {
            for (GUIDropDownElement button : this.mappedProfiles.keySet()) {
                if (element == button && button != this.currentProfile) {
                    PlayersManagerClient.instance().changeJumpProfileSynced(this.mappedProfiles.get(button));
                    this.currentProfile = button;
                    return;
                }
            }
            for (PlayerGUIButton button : this.mappedPlayers.keySet()) {
                if (element == button && button != this.currentButton) {
                    if (this.currentButton != null)
                        this.currentButton.setToggled(false);
                    this.currentButton = (PlayerGUIButton) button;
                    this.currentPlayer = this.mappedPlayers.get(button);
                    button.toggle();                    
                    this.showPlayerInfo();
                }
            }
        }
    }

    private void showPlayerInfo() {
        this.targetUsernameLabel.setDisplayText(this.currentPlayer.getUsername());
        this.targetDimensionLabel.setDisplayText(EnumDimensions.getLocalizedNameFromId(this.currentPlayer.getDimension()));
        PlayerProfile.EnumJumpProfile jumpProfile = PlayersManagerClient.getPlayerJumpProfile(this.currentPlayer.getUUID());
        switch (jumpProfile) {
        case DISABLED:
            if (CampsManagerClient.instance().isOpped())
                this.moveButton.setDisplayText(this.moveToStr, true);
            break;
        case FREE:
            this.moveButton.setDisplayText(this.moveToStr, true);
            break;
        case REQUEST:
            this.moveButton.setDisplayText(this.requestStr, true);
            break;
        }
        this.targetUsernameLabel.enableFull();
        this.targetDimensionLabel.enableFull();
        this.moveButton.enableFull();

        if (this.getCooldownElapsedTime() > 0 && this.getCooldownElapsedTime() != this.teleportationCooldown) {
            this.cooldownLabel.enableFull();
            this.moveButton.disable();
        }
    }

    private void resetPlayerInfo() {
        this.targetUsernameLabel.disableFull();
        this.targetDimensionLabel.disableFull();
        this.moveButton.disableFull();

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
        return MathHelper.clamp((int) (this.teleportationCooldown - (System.currentTimeMillis() - CampsManagerClient.instance().getPlayerProfile().getCooldownInfo().getLastJumpTime()))
                , 0, this.teleportationCooldown);
    }
}
