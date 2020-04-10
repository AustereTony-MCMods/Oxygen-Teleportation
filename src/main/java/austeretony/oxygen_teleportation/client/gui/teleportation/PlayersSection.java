package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.elements.OxygenActivityStatusSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListWrapperEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter.EnumSorting;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.EnumActivityStatus;
import austeretony.oxygen_core.common.PlayerSharedData;
import austeretony.oxygen_core.common.main.EnumOxygenPrivilege;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.players.PlayerPanelEntry;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData.EnumJumpProfile;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraft.client.gui.ScaledResolution;

public class PlayersSection extends AbstractGUISection {

    private final TeleportationMenuScreen screen;

    private OxygenTextLabel playersAmountTextLabel, cooldownTextLabel;

    private OxygenKeyButton moveButton;

    private OxygenSorter statusSorter, usernameSorter;

    private OxygenScrollablePanel playersPanel;

    private OxygenTextField searchField;

    private OxygenActivityStatusSwitcher activityStatusSwitcher;

    private OxygenDropDownList jumpProfileSwitcher;

    private OxygenCurrencyValue feeValue, balanceValue;

    private final int cooldownTime = PrivilegesProviderClient.getAsInt(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.id(), 
            TeleportationConfig.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.asInt()) * 1000;

    //cache

    private PlayerPanelEntry currentEntry;

    private boolean cooldownActive;

    public PlayersSection(TeleportationMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_teleportation.gui.menu.players"));
    }

    @Override
    public void init() {              
        this.addElement(new OxygenDefaultBackgroundWithButtonsFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_teleportation.gui.menu.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.playersAmountTextLabel = new OxygenTextLabel(0, 22, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.statusSorter = new OxygenSorter(13, 28, EnumSorting.DOWN, ClientReference.localize("oxygen_core.gui.status")));   
        this.statusSorter.setSortingListener((sorting)->{
            this.usernameSorter.reset();
            this.sortPlayers(sorting == EnumSorting.DOWN ? 0 : 1);
        });

        this.addElement(this.usernameSorter = new OxygenSorter(19, 28, EnumSorting.INACTIVE, ClientReference.localize("oxygen_core.gui.username")));  
        this.usernameSorter.setSortingListener((sorting)->{
            this.statusSorter.reset();
            this.sortPlayers(sorting == EnumSorting.DOWN ? 2 : 3);
        });

        this.addElement(this.playersPanel = new OxygenScrollablePanel(this.screen, 6, 36, this.getWidth() - 15, 10, 1, 100, 10, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));   
        this.addElement(this.searchField = new OxygenTextField(180, 16, 60, WorldPoint.MAX_NAME_LENGTH, ""));
        this.playersPanel.initSearchField(this.searchField);

        this.playersPanel.<PlayerPanelEntry>setElementClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (this.currentEntry != clicked) {
                if (this.currentEntry != null)
                    this.currentEntry.setToggled(false);
                this.currentEntry = clicked;
                clicked.toggle();                    
                this.showPlayerInfo(clicked.getWrapped());
            }
        });

        this.addElement(this.activityStatusSwitcher = new OxygenActivityStatusSwitcher(7, 16));

        this.addElement(this.jumpProfileSwitcher = new OxygenDropDownList(80, 16, 75, ""));
        for (EnumJumpProfile jumpProfile : EnumJumpProfile.values())
            this.jumpProfileSwitcher.addElement(new OxygenDropDownListWrapperEntry<EnumJumpProfile>(jumpProfile, jumpProfile.localizedName()));

        this.jumpProfileSwitcher.<OxygenDropDownListWrapperEntry<EnumJumpProfile>>setElementClickListener(
                (element)->TeleportationManagerClient.instance().getPlayerDataManager().changeJumpProfileSynced(element.getWrapped()));

        this.addElement(this.cooldownTextLabel = new OxygenTextLabel(0, this.getY() + this.getHeight() + this.screen.guiTop - 2, "",  EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()).setVisible(false));  
        this.addElement(this.moveButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_teleportation.gui.menu.button.moveToPlayer"), Keyboard.KEY_F, this::move).disable());  

        this.addElement(this.feeValue = new OxygenCurrencyValue(0, this.getY() + this.getHeight() + this.screen.guiTop - 8).disableFull());  
        this.addElement(this.balanceValue = new OxygenCurrencyValue(0, this.getY() + this.getHeight() + this.screen.guiTop - 8).disableFull());  
        if (TeleportationConfig.FEE_MODE.asInt() == 1) {
            this.feeValue.setValue(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack(), 0);
            this.balanceValue.setValue(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack(), (int) this.screen.balance);
        } else {
            this.feeValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, 0L);
            this.balanceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, this.screen.balance);
        }

        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getCampsSection(), this.screen.getLocationsSection()));
    }

    private void calculateButtonsHorizontalPosition() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.moveButton.setX((sr.getScaledWidth() - (12 + this.textWidth(this.moveButton.getDisplayText(), this.moveButton.getTextScale()))) / 2 - this.screen.guiLeft);

        long fee = PrivilegesProviderClient.getAsLong(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_FEE.id(), TeleportationConfig.TELEPORTATION_TO_PLAYER_FEE.asLong());
        if (fee > 0L) {            
            this.cooldownTextLabel.setX(this.moveButton.getX() - 40);
            this.feeValue.setX(this.moveButton.getX() - 10);
            this.balanceValue.setX(sr.getScaledWidth() - 10 - this.screen.guiLeft);
        }
    }

    public void sortPlayers(int mode) {    
        List<PlayerSharedData> players = OxygenHelperClient.getPlayersSharedData()
                .stream()
                .filter(s->OxygenHelperClient.isPlayerOnline(s.getPlayerUUID()) 
                        && PrivilegesProviderClient.getAsBoolean(EnumOxygenPrivilege.EXPOSE_OFFLINE_PLAYERS.id(), OxygenHelperClient.getPlayerActivityStatus(s) != EnumActivityStatus.OFFLINE)
                        && (getJumpProfile(s) != EnumJumpProfile.DISABLED || PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.id(), false)))
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
            this.playersPanel.addEntry(new PlayerPanelEntry(sharedData));

        this.playersAmountTextLabel.setDisplayText(String.format("%d/%d", players.size(), OxygenHelperClient.getMaxPlayers()));   
        this.playersAmountTextLabel.setX(327 - this.textWidth(this.playersAmountTextLabel.getDisplayText(), this.playersAmountTextLabel.getTextScale()));

        this.searchField.reset();

        this.playersPanel.getScroller().reset();
        this.playersPanel.getScroller().updateRowsAmount(MathUtils.clamp(players.size(), 10, MathUtils.greaterOfTwo(players.size(), OxygenHelperClient.getMaxPlayers())));
    }

    public static EnumJumpProfile getJumpProfile(PlayerSharedData sharedData) {
        return EnumJumpProfile.values()[sharedData.getByte(TeleportationMain.JUMP_PROFILE_SHARED_DATA_ID)];
    }

    private void move() {
        if (!this.searchField.isDragged()) {
            TeleportationManagerClient.instance().getPlayerDataManager().moveToPlayerSynced(this.currentEntry.getWrapped());
            this.screen.close();
        }
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.moveButton)
                this.move();
        }
    }

    public void sharedDataSynchronized() {
        this.activityStatusSwitcher.updateActivityStatus();
        this.jumpProfileSwitcher.setDisplayText(getJumpProfile(OxygenHelperClient.getPlayerSharedData()).localizedName());
        this.sortPlayers(0);

        this.calculateButtonsHorizontalPosition();
    }

    public void cooldownSynchronized() {
        this.cooldownActive = this.getCooldownElapsedTimeSeconds() > 0;
    }

    private void showPlayerInfo(int playerIndex) {
        this.moveButton.setEnabled(this.screen.jumpsEnabled);
        this.cooldownTextLabel.setVisible(this.cooldownActive);

        PlayerSharedData sharedData = OxygenHelperClient.getPlayerSharedData(playerIndex);
        this.moveButton.setDisplayText(getJumpProfile(sharedData) == EnumJumpProfile.FREE 
                ? ClientReference.localize("oxygen_teleportation.gui.menu.button.moveToPlayer") 
                        : ClientReference.localize("oxygen_teleportation.gui.menu.button.requestTeleportation"));

        long fee = PrivilegesProviderClient.getAsLong(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_FEE.id(), TeleportationConfig.TELEPORTATION_TO_PLAYER_FEE.asLong());
        if (fee > 0L) {
            this.feeValue.updateValue(fee);
            this.feeValue.enableFull();
            this.balanceValue.enableFull();

            if (fee > this.balanceValue.getValue()) {
                this.moveButton.disable();
                this.feeValue.setRed(true);
            }
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.searchField.isDragged() && !this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == TeleportationMenuScreen.TELEPORTATIOIN_MENU_ENTRY.getKeyCode())
                    this.screen.close();
            } else if (TeleportationConfig.ENABLE_TELEPORTATION_MENU_KEY.asBoolean() 
                    && keyCode == TeleportationManagerClient.instance().getKeyHandler().getTeleportationMenuKeybinding().getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    @Override
    public void update() {
        if (this.cooldownActive) {
            if (this.getCooldownElapsedTimeSeconds() > 0)
                this.cooldownTextLabel.setDisplayText("[" + String.valueOf(this.getCooldownElapsedTimeSeconds()) + "]");
            else if (this.cooldownActive) {
                this.cooldownActive = false;
                this.cooldownTextLabel.setVisible(false);
            }
        }
    }

    private long getCooldownElapsedTimeSeconds() {
        return (TeleportationManagerClient.instance().getPlayerData().getCooldownData().getNextJumpTime() - System.currentTimeMillis()) / 1000;
    }

    public boolean isCooldownActive() {
        return this.cooldownActive;
    }
}
