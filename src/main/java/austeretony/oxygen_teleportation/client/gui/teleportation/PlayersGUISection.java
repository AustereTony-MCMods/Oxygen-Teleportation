package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegeProviderClient;
import austeretony.oxygen_core.client.gui.elements.ActivityStatusGUIDDList;
import austeretony.oxygen_core.client.gui.elements.CurrencyItemValueGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButtonPanel;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIDDList;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIDDListElement;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.elements.OxygenGUITextField;
import austeretony.oxygen_core.client.gui.elements.OxygenSorterGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenSorterGUIElement.EnumSorting;
import austeretony.oxygen_core.client.gui.elements.SectionsGUIDDList;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.PlayerSharedData;
import austeretony.oxygen_core.common.main.EnumOxygenPrivilege;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.server.OxygenPlayerData.EnumActivityStatus;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.players.PlayerGUIButton;
import austeretony.oxygen_teleportation.client.gui.teleportation.players.PlayersBackgroundGUIFiller;
import austeretony.oxygen_teleportation.client.input.TeleportationMenuKeyHandler;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData.EnumJumpProfile;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class PlayersGUISection extends AbstractGUISection {

    private final TeleportationMenuGUIScreen screen;

    private OxygenGUIText playersAmountTextLabel, cooldownTextLabel;

    private OxygenGUIButton moveButton;

    private OxygenSorterGUIElement statusSorter, usernameSorter;

    private OxygenGUIButtonPanel playersPanel;

    private OxygenGUITextField searchField;

    private ActivityStatusGUIDDList activityStatusSwitcher;

    private OxygenGUIDDList jumpProfileSwitcher;

    private CurrencyItemValueGUIElement feeElement, balanceElement;

    private final int cooldownTime = PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.toString(), 
            TeleportationConfig.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()) * 1000;

    //cache

    private PlayerGUIButton currentButton;

    private boolean cooldownActive;

    public PlayersGUISection(TeleportationMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {              
        this.addElement(new PlayersBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_teleportation.gui.menu.players"), GUISettings.get().getTitleScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.playersAmountTextLabel = new OxygenGUIText(0, 18, "", GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColor()));   

        this.addElement(this.statusSorter = new OxygenSorterGUIElement(13, 28, EnumSorting.DOWN, ClientReference.localize("oxygen.sorting.status")));   
        this.statusSorter.setClickListener((sorting)->{
            this.usernameSorter.reset();
            this.sortPlayers(sorting == EnumSorting.DOWN ? 0 : 1);
        });

        this.addElement(this.usernameSorter = new OxygenSorterGUIElement(19, 28, EnumSorting.INACTIVE, ClientReference.localize("oxygen.sorting.username")));  
        this.usernameSorter.setClickListener((sorting)->{
            this.statusSorter.reset();
            this.sortPlayers(sorting == EnumSorting.DOWN ? 2 : 3);
        });

        this.addElement(this.playersPanel = new OxygenGUIButtonPanel(this.screen, 6, 36, this.getWidth() - 15, 10, 1, 100, 9, GUISettings.get().getPanelTextScale(), true));   
        this.addElement(this.searchField = new OxygenGUITextField(180, 16, 65, 8, WorldPoint.MAX_NAME_LENGTH, "...", 3, false, - 1L));
        this.playersPanel.initSearchField(this.searchField);

        this.playersPanel.<PlayerGUIButton>setClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (this.currentButton != clicked) {
                if (this.currentButton != null)
                    this.currentButton.setToggled(false);
                this.currentButton = clicked;
                clicked.toggle();                    
                this.showPlayerInfo();
            }
        });

        this.addElement(this.activityStatusSwitcher = new ActivityStatusGUIDDList(7, 16));

        this.addElement(this.jumpProfileSwitcher = new OxygenGUIDDList(80, 16, 75, 9, ""));
        for (EnumJumpProfile jumpProfile : EnumJumpProfile.values())
            this.jumpProfileSwitcher.addElement(new OxygenGUIDDListElement<EnumJumpProfile>(jumpProfile, jumpProfile.localizedName()));

        this.jumpProfileSwitcher.<OxygenGUIDDListElement<EnumJumpProfile>>setClickListener(
                (element)->TeleportationManagerClient.instance().getPlayerDataManager().changeJumpProfileSynced(element.index));

        this.addElement(this.cooldownTextLabel = new OxygenGUIText(76, this.getHeight() - 10, "", GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColor()).setVisible(false));  
        this.addElement(this.moveButton = new OxygenGUIButton(6, 138,  40, 10, ClientReference.localize("oxygen_teleportation.gui.menu.moveButton")).disable());  

        this.addElement(this.feeElement = new CurrencyItemValueGUIElement(66, this.getHeight() - 10).disableFull());  
        this.addElement(this.balanceElement = new CurrencyItemValueGUIElement(this.getWidth() - 10, this.getHeight() - 10).disableFull());  
        this.balanceElement.setValue(this.screen.balance);
        if (TeleportationConfig.FEE_MODE.getIntValue() == 1) {
            this.feeElement.setItemStack(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack());
            this.balanceElement.setItemStack(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack());
        }

        this.addElement(new SectionsGUIDDList(this.getWidth() - 4, 5, this, this.screen.getCampsSection(), this.screen.getLocationsSection()));
    }

    public void sortPlayers(int mode) {    
        List<PlayerSharedData> players = OxygenHelperClient.getPlayersSharedData()
                .stream()
                .filter(s->OxygenHelperClient.isPlayerOnline(s.getPlayerUUID()) 
                        && (OxygenHelperClient.getPlayerActivityStatus(s) != EnumActivityStatus.OFFLINE || PrivilegeProviderClient.getValue(EnumOxygenPrivilege.EXPOSE_PLAYERS_OFFLINE.toString(), false))
                        && (getJumpProfile(s) != EnumJumpProfile.DISABLED || PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false)))
                .collect(Collectors.toList());
        players.remove(OxygenHelperClient.getPlayerSharedData());

        if (mode == 0)
            Collections.sort(players, (s1, s2)->OxygenHelperClient.getPlayerActivityStatus(s1).ordinal() - OxygenHelperClient.getPlayerActivityStatus(s2).ordinal());
        else if (mode == 1)
            Collections.sort(players, (s1, s2)->OxygenHelperClient.getPlayerActivityStatus(s2).ordinal() - OxygenHelperClient.getPlayerActivityStatus(s1).ordinal());
        else if (mode == 2)
            Collections.sort(players, (s1, s2)->s1.getUsername().compareTo(s2.getUsername()));
        else if (mode == 3)
            Collections.sort(players, (s1, s2)->s2.getUsername().compareTo(s1.getUsername()));

        this.playersPanel.reset();
        for (PlayerSharedData sharedData : players)
            this.playersPanel.addButton(new PlayerGUIButton(sharedData));

        this.playersAmountTextLabel.setDisplayText(String.valueOf(players.size()) + "/" + String.valueOf(OxygenHelperClient.getMaxPlayers()));   
        this.playersAmountTextLabel.setX(327 - this.textWidth(this.playersAmountTextLabel.getDisplayText(), GUISettings.get().getSubTextScale() - 0.05F));

        this.searchField.reset();

        int maxRows = MathUtils.clamp(players.size(), 9, MathUtils.greaterOfTwo(players.size(), OxygenHelperClient.getMaxPlayers()));
        this.playersPanel.getScroller().updateRowsAmount(maxRows);

        this.playersPanel.getScroller().resetPosition();
        this.playersPanel.getScroller().getSlider().reset();
    }

    public static EnumJumpProfile getJumpProfile(PlayerSharedData sharedData) {
        return EnumJumpProfile.values()[sharedData.getByte(TeleportationMain.JUMP_PROFILE_SHARED_DATA_ID)];
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.moveButton) {
                TeleportationManagerClient.instance().getPlayerDataManager().moveToPlayerSynced(OxygenHelperClient.getPlayerSharedData(this.currentButton.index).getIndex());
                this.screen.close();
            }
        }
    }

    public void sharedDataSynchronized() {
        this.activityStatusSwitcher.updateActivityStatus();
        this.jumpProfileSwitcher.setDisplayText(getJumpProfile(OxygenHelperClient.getPlayerSharedData()).localizedName());
        this.sortPlayers(0);
    }

    public void cooldownSynchronized() {
        this.cooldownActive = this.getCooldownElapsedTime() > 0;
    }

    private void showPlayerInfo() {
        this.moveButton.enable();
        this.cooldownTextLabel.setVisible(this.cooldownActive);

        long fee = PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.JUMP_TO_PLAYER_FEE.toString(), TeleportationConfig.JUMP_TO_PLAYER_FEE.getLongValue());
        if (fee > 0L) {
            this.feeElement.setValue(fee);
            this.feeElement.enableFull();
            this.balanceElement.enableFull();

            if (fee > this.balanceElement.getValue()) {
                this.moveButton.disable();
                this.feeElement.setRed(true);
            }
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.searchField.isDragged() && !this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == TeleportationMenuGUIScreen.TELEPORTATIOIN_MENU_ENTRY.getIndex() + 2)
                    this.screen.close();
            } else if (keyCode == TeleportationMenuKeyHandler.TELEPORTATION_MENU.getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    @Override
    public void update() {
        if (this.cooldownActive) {
            if (this.getCooldownElapsedTime() > 0)
                this.cooldownTextLabel.setDisplayText("[" + (this.getCooldownElapsedTime() / 1000) + "]");
            else if (this.cooldownActive) {
                this.cooldownActive = false;
                this.cooldownTextLabel.setVisible(false);
            }
        }
    }

    private int getCooldownElapsedTime() {
        return (int) (this.cooldownTime - (System.currentTimeMillis() - TeleportationManagerClient.instance().getPlayerData().getCooldownData().getLastJumpTime()));
    }

    public boolean isCooldownActive() {
        return this.cooldownActive;
    }
}
