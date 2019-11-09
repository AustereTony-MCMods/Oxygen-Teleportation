package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegeProviderClient;
import austeretony.oxygen_core.client.gui.elements.CurrencyItemValueGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButtonPanel;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenu;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.elements.OxygenGUITextField;
import austeretony.oxygen_core.client.gui.elements.OxygenSorterGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenSorterGUIElement.EnumSorting;
import austeretony.oxygen_core.client.gui.elements.SectionsGUIDDList;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.PointsBackgroundGUIFiller;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.WorldPointDataGUIElement;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.WorldPointGUIButton;
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
import austeretony.oxygen_teleportation.client.input.TeleportationMenuKeyHandler;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;

public class CampsGUISection extends AbstractGUISection {

    private final TeleportationMenuGUIScreen screen;

    private OxygenGUIText pointsAmountTextLabel, cooldownTextLabel;

    private OxygenGUIButton createButton, moveButton;

    private OxygenSorterGUIElement timeSorter, nameSorter;

    private OxygenGUIButtonPanel pointsPanel;

    private OxygenGUITextField searchField;

    private WorldPointDataGUIElement pointDataElement;

    private CurrencyItemValueGUIElement feeElement, balanceElement;

    private AbstractGUICallback creationCallback, inviteCallback, invitationsCallback, pointEditingCallback, 
    removePointCallback, leavePointCallback;

    private final int cooldownTime = PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN_SECONDS.toString(), 
            TeleportationConfig.CAMP_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()) * 1000;

    //cache

    private WorldPointGUIButton currentButton;   

    private WorldPoint currentPoint;

    private boolean cooldownActive;

    public CampsGUISection(TeleportationMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {	
        this.addElement(new PointsBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_teleportation.gui.menu.camps"), GUISettings.get().getTitleScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.pointsAmountTextLabel = new OxygenGUIText(0, 18, "", GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColor()));

        this.addElement(this.timeSorter = new OxygenSorterGUIElement(6, 29, EnumSorting.DOWN, ClientReference.localize("oxygen.sorting.time")));   
        this.timeSorter.setClickListener((sorting)->{
            this.nameSorter.reset();
            this.sortPoints(sorting == EnumSorting.DOWN ? 0 : 1);
        });

        this.addElement(this.nameSorter = new OxygenSorterGUIElement(12, 29, EnumSorting.INACTIVE, ClientReference.localize("oxygen.sorting.name")));  
        this.nameSorter.setClickListener((sorting)->{
            this.timeSorter.reset();
            this.sortPoints(sorting == EnumSorting.DOWN ? 2 : 3);
        });

        this.addElement(this.pointsPanel = new OxygenGUIButtonPanel(this.screen, 6, 36, 80, 10, 1, 100, 9, GUISettings.get().getPanelTextScale(), true));   
        this.addElement(this.searchField = new OxygenGUITextField(6, 16, 60, 8, WorldPoint.MAX_NAME_LENGTH, "...", 3, false, - 1L));
        this.pointsPanel.initSearchField(this.searchField);

        this.pointsPanel.<WorldPointGUIButton>setClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (this.currentButton != clicked) {
                if (this.currentButton != null)
                    this.currentButton.setToggled(false);
                this.currentButton = clicked;
                this.currentPoint = TeleportationManagerClient.instance().getPlayerData().getCamp(clicked.index);
                clicked.toggle();                    
                this.showPointInfo(clicked.index, false);
            }
        });

        this.pointsPanel.initContextMenu(new OxygenGUIContextMenu(GUISettings.get().getContextMenuWidth(), 9, 
                new MakeFavoriteContextAction(this),
                new LockContextAction(this),
                new InviteContextAction(this),
                new InvitationsContextAction(this),
                new EditContextAction(this),
                new RemoveContextAction(this)));

        this.addElement(this.createButton = new OxygenGUIButton(25, 138, 40, 10, ClientReference.localize("oxygen_teleportation.gui.menu.createButton")));     

        this.addElement(this.pointDataElement = new WorldPointDataGUIElement(91, 15));
        this.addElement(this.cooldownTextLabel = new OxygenGUIText(164, this.getHeight() - 10, "", GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColor()).setVisible(false));  
        this.addElement(this.moveButton = new OxygenGUIButton(94, 138, 40, 10, ClientReference.localize("oxygen_teleportation.gui.menu.moveButton")).disableFull());     

        this.addElement(new SectionsGUIDDList(this.getWidth() - 4, 5, this, this.screen.getLocationsSection(), this.screen.getPlayersSection()));

        this.addElement(this.feeElement = new CurrencyItemValueGUIElement(152, this.getHeight() - 10).disableFull());  
        this.addElement(this.balanceElement = new CurrencyItemValueGUIElement(this.getWidth() - 10, this.getHeight() - 10).disableFull());  
        this.balanceElement.setValue(this.screen.balance);
        if (TeleportationConfig.FEE_MODE.getIntValue() == 1) {
            this.feeElement.setItemStack(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack());
            this.balanceElement.setItemStack(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack());
        }

        this.creationCallback = new CampCreationGUICallback(this.screen, this, 140, 110).enableDefaultBackground();
        this.pointEditingCallback = new EditCampGUICallback(this.screen, this, 140, 130).enableDefaultBackground();
        this.removePointCallback = new CampRemoveGUICallback(this.screen, this, 140, 38).enableDefaultBackground();
        this.leavePointCallback = new LeaveCampGUICallback(this.screen, this, 140, 38).enableDefaultBackground();

        this.inviteCallback = new InviteGUICallback(this.screen, this, 140, 48).enableDefaultBackground();
        this.invitationsCallback = new InvitationsGUICallback(this.screen, this, 140, 81).enableDefaultBackground();
    }

    private void sortPoints(int mode) {
        List<WorldPoint> points = new ArrayList<>(TeleportationManagerClient.instance().getPlayerData().getCamps());

        if (mode == 0)
            Collections.sort(points, (p1, p2)-> (int) ((p1.getId() - p2.getId()) / 5000L));
        else if (mode == 1)
            Collections.sort(points, (p1, p2)-> (int) ((p2.getId() - p1.getId()) / 5000L));
        else if (mode == 2)
            Collections.sort(points, (p1, p2)->p1.getName().compareTo(p2.getName()));
        else if (mode == 3)
            Collections.sort(points, (p1, p2)->p2.getName().compareTo(p1.getName()));

        this.pointsPanel.reset();
        for (WorldPoint worldPoint : points)
            this.pointsPanel.addButton(new WorldPointGUIButton(EnumWorldPoint.CAMP, worldPoint));

        int maxAmount = PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue());
        this.pointsAmountTextLabel.setDisplayText(String.valueOf(TeleportationManagerClient.instance().getPlayerData().getCampsAmount()) + "/" + String.valueOf(maxAmount));     
        this.pointsAmountTextLabel.setX(89 - this.textWidth(this.pointsAmountTextLabel.getDisplayText(), GUISettings.get().getSubTextScale() - 0.05F));

        this.searchField.reset();

        int maxRows = MathUtils.clamp(points.size(), 9, MathUtils.greaterOfTwo(points.size(), maxAmount));
        this.pointsPanel.getScroller().updateRowsAmount(maxRows);

        this.pointsPanel.getScroller().resetPosition();
        this.pointsPanel.getScroller().getSlider().reset();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.createButton)
                this.creationCallback.open();
            else if (element == this.moveButton) {
                TeleportationManagerClient.instance().getPlayerDataManager().moveToCampSynced(this.currentButton.index);
                this.screen.close();
            }
        }
    }

    public void campsSynchronized() {
        this.sortPoints(0);
        this.updateCreateButtonState();     
    }

    public void cooldownSynchronized() {
        this.cooldownActive = this.getCooldownElapsedTime() > 0;
    }

    public void campCreated(WorldPoint worldPoint) {
        this.resetPointInfo();
        this.timeSorter.setSorting(EnumSorting.DOWN);
        this.nameSorter.reset();
        this.sortPoints(0);

        this.updateCreateButtonState();
    }

    public void campEdited(long oldPoiintId, WorldPoint worldPoint, boolean updateImage) {
        this.resetPointInfo();
        this.timeSorter.setSorting(EnumSorting.DOWN);
        this.nameSorter.reset();
        this.sortPoints(0);

        WorldPointGUIButton pointButton;
        for (GUIButton button : this.pointsPanel.buttonsBuffer) {
            pointButton = (WorldPointGUIButton) button;
            if (pointButton.index == worldPoint.getId()) {
                pointButton.toggle();
                this.currentButton = pointButton;
                this.currentPoint = TeleportationManagerClient.instance().getPlayerData().getCamp(pointButton.index);
            }
        }
        this.showPointInfo(worldPoint.getId(), false);
    }

    public void campRemoved(long pointId) {
        this.resetPointInfo();
        this.timeSorter.setSorting(EnumSorting.DOWN);
        this.nameSorter.reset();
        this.sortPoints(0);

        this.updateCreateButtonState();
    }

    public void favoriteCampSet(long pointId) {
        WorldPointGUIButton pointButton;
        for (GUIButton button : this.pointsPanel.buttonsBuffer) {
            pointButton = (WorldPointGUIButton) button;
            pointButton.setFavorite(false);
            if (pointButton.index == pointId)
                pointButton.setFavorite(true);
        }
    }

    public void playerUninvited(long pointId, UUID playerUUID) {
        if (this.currentPoint.getId() == pointId) {
            if (this.getCurrentCallback() != null 
                    && this.getCurrentCallback() instanceof InvitationsGUICallback)
                ((InvitationsGUICallback) this.invitationsCallback).playerUninvited(pointId, playerUUID);
            if (TeleportationManagerClient.instance().getSharedCampsContainer().getInvitedPlayers(this.currentPoint.getId()).size() == 0) {
                WorldPointGUIButton pointButton;
                for (GUIButton button : this.pointsPanel.buttonsBuffer) {
                    pointButton = (WorldPointGUIButton) button;
                    if (pointButton.index == pointId)
                        pointButton.setShared(false);
                }
            }
        }
    }

    public void showPointInfo(long pointId, boolean reloadImage) {
        this.pointDataElement.show(this.currentPoint, reloadImage);
        this.moveButton.enableFull();
        this.cooldownTextLabel.setVisible(this.cooldownActive);
        this.moveButton.setEnabled(!this.cooldownActive && !(this.currentPoint.isLocked() && !this.currentPoint.isOwner(OxygenHelperClient.getPlayerUUID())));

        long fee = PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.CAMP_TELEPORTATION_FEE.toString(), TeleportationConfig.CAMP_TELEPORTATION_FEE.getLongValue());
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

    public void resetPointInfo() {
        this.pointDataElement.hide();
        this.moveButton.disableFull();
        this.cooldownTextLabel.setVisible(false);

        this.feeElement.disableFull();
        this.balanceElement.disableFull();
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
                this.moveButton.enable();
            }
        }
    }

    private int getCooldownElapsedTime() {
        return (int) (this.cooldownTime - (System.currentTimeMillis() - TeleportationManagerClient.instance().getPlayerData().getCooldownData().getLastCampTime()));
    }

    public void updateCreateButtonState() {
        this.createButton.setEnabled(TeleportationConfig.ENABLE_CAMPS.getBooleanValue() 
                && TeleportationManagerClient.instance().getPlayerData().getOwnedCampsAmount() < PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue()));
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

    public WorldPointGUIButton getCurrentButton() {
        return this.currentButton;
    }

    public WorldPoint getCurrentPoint() {
        return this.currentPoint;
    }
}