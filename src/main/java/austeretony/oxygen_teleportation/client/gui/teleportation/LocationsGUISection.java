package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback.EditLocationGUICallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback.LocationCreationGUICallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback.LocationRemoveGUICallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.context.EditContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.context.LockContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.context.RemoveContextAction;
import austeretony.oxygen_teleportation.client.input.TeleportationMenuKeyHandler;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;

public class LocationsGUISection extends AbstractGUISection {

    private final TeleportationMenuGUIScreen screen;

    private OxygenGUIText pointsAmountTextLabel, cooldownTextLabel;

    private OxygenGUIButton createButton, moveButton;

    private OxygenSorterGUIElement timeSorter, nameSorter;

    private OxygenGUIButtonPanel pointsPanel;

    private OxygenGUITextField searchField;

    private WorldPointDataGUIElement pointDataElement;

    private CurrencyItemValueGUIElement feeElement, balanceElement;

    private AbstractGUICallback creationCallback, pointEditingCallback, removePointCallback;

    private final int cooldownTime = PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.toString(), 
            TeleportationConfig.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()) * 1000;

    //cache

    private WorldPointGUIButton currentButton; 

    private WorldPoint currentPoint;

    private boolean cooldownActive;

    public LocationsGUISection(TeleportationMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {     
        this.addElement(new PointsBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_teleportation.gui.menu.locations"), GUISettings.get().getTitleScale(), GUISettings.get().getEnabledTextColor()));

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
                this.currentPoint = TeleportationManagerClient.instance().getLocationsContainer().getLocation(clicked.index);
                clicked.toggle();                    
                this.showPointInfo(clicked.index, false);
            }
        });

        this.pointsPanel.initContextMenu(new OxygenGUIContextMenu(GUISettings.get().getContextMenuWidth(), 9, 
                new LockContextAction(this),
                new EditContextAction(this),
                new RemoveContextAction(this)));

        this.addElement(this.createButton = new OxygenGUIButton(25, 138, 40, 10, ClientReference.localize("oxygen_teleportation.gui.menu.createButton")));     

        this.addElement(this.pointDataElement = new WorldPointDataGUIElement(91, 15));
        this.addElement(this.cooldownTextLabel = new OxygenGUIText(162, this.getHeight() - 9, "", GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColor()).setVisible(false));  
        this.addElement(this.moveButton = new OxygenGUIButton(94, 138,  40, 10, ClientReference.localize("oxygen_teleportation.gui.menu.moveButton")).disableFull());  

        this.addElement(new SectionsGUIDDList(this.getWidth() - 4, 5, this, this.screen.getCampsSection(), this.screen.getPlayersSection()));

        this.addElement(this.feeElement = new CurrencyItemValueGUIElement(152, this.getHeight() - 10).disableFull());  
        this.addElement(this.balanceElement = new CurrencyItemValueGUIElement(this.getWidth() - 10, this.getHeight() - 10).disableFull());  
        this.balanceElement.setValue(this.screen.balance);
        if (TeleportationConfig.FEE_MODE.getIntValue() == 1) {
            this.feeElement.setItemStack(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack());
            this.balanceElement.setItemStack(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack());
        }

        this.creationCallback = new LocationCreationGUICallback(this.screen, this, 140, 110).enableDefaultBackground();
        this.pointEditingCallback = new EditLocationGUICallback(this.screen, this, 140, 130).enableDefaultBackground();
        this.removePointCallback = new LocationRemoveGUICallback(this.screen, this, 140, 38).enableDefaultBackground();
    }

    public void sortPoints(int mode) { 
        List<WorldPoint> points = new ArrayList<>(TeleportationManagerClient.instance().getLocationsContainer().getLocations());

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
            this.pointsPanel.addButton(new WorldPointGUIButton(EnumWorldPoint.LOCATION, worldPoint));

        int maxAmount = TeleportationConfig.LOCATIONS_MAX_AMOUNT.getIntValue();
        this.pointsAmountTextLabel.setDisplayText(String.valueOf(TeleportationManagerClient.instance().getLocationsContainer().getLocationsAmount()) + "/" + String.valueOf(maxAmount));     
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
                TeleportationManagerClient.instance().getLocationsManager().moveToLocationSynced(this.currentButton.index);
                this.screen.close();
            }
        }
    }

    public void locationsSynchronized() {
        this.sortPoints(0);
        this.updateCreateButtonState();     
    }

    public void cooldownSynchronized() {
        this.cooldownActive = this.getCooldownElapsedTimeSeconds() > 0;
    }

    public void locationCreated(WorldPoint worldPoint) {
        this.resetPointInfo();
        this.timeSorter.setSorting(EnumSorting.DOWN);
        this.nameSorter.reset();
        this.sortPoints(0);

        this.updateCreateButtonState();
    }

    public void locationEdited(long oldPoiintId, WorldPoint worldPoint, boolean updateImage) {
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
                this.currentPoint = TeleportationManagerClient.instance().getLocationsContainer().getLocation(pointButton.index);
            }
        }
        this.showPointInfo(worldPoint.getId(), false);
    }

    public void locationRemoved(long pointId) {
        this.resetPointInfo();
        this.timeSorter.setSorting(EnumSorting.DOWN);
        this.nameSorter.reset();
        this.sortPoints(0);

        this.updateCreateButtonState();
    }

    public void showPointInfo(long pointId, boolean reloadImage) {
        this.pointDataElement.show(this.currentPoint, reloadImage);
        this.moveButton.enableFull();
        this.cooldownTextLabel.setVisible(this.cooldownActive);
        this.moveButton.setEnabled(!this.cooldownActive && !(this.currentPoint.isLocked() && !this.currentPoint.isOwner(OxygenHelperClient.getPlayerUUID())));

        long fee = PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_FEE.toString(), TeleportationConfig.LOCATION_TELEPORTATION_FEE.getLongValue());
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
            if (this.getCooldownElapsedTimeSeconds() > 0)
                this.cooldownTextLabel.setDisplayText("[" + String.valueOf(this.getCooldownElapsedTimeSeconds()) + "]");
            else if (this.cooldownActive) {
                this.cooldownActive = false;
                this.cooldownTextLabel.setVisible(false);
                this.moveButton.enable();
            }
        }
    }

    private long getCooldownElapsedTimeSeconds() {
        return (TeleportationManagerClient.instance().getPlayerData().getCooldownData().getNextLocationTime() - System.currentTimeMillis()) / 1000;
    }

    public void updateCreateButtonState() {
        this.createButton.setEnabled(TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue()
                && (TeleportationConfig.ALLOW_LOCATIONS_CREATION_FOR_ALL.getBooleanValue() || PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.LOCATIONS_CREATION.toString(), false) || PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false))
                && TeleportationManagerClient.instance().getLocationsContainer().getLocationsAmount() < TeleportationConfig.LOCATIONS_MAX_AMOUNT.getIntValue());
    }

    public void openPointEditingCallback() {
        this.pointEditingCallback.open();
    }

    public void openRemovePointCallback() {
        this.removePointCallback.open();
    }

    public WorldPointGUIButton getCurrentButton() {
        return this.currentButton;
    }

    public WorldPoint getCurrentPoint() {
        return this.currentPoint;
    }
}
