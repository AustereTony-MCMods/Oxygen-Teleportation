package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter.EnumSorting;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.WorldPointPanelEntry;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.WorldPointPreview;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback.EditLocationCallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback.LocationCreationCallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback.LocationRemoveCallback;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.context.EditContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.context.LockContextAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.locations.context.RemoveContextAction;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import net.minecraft.client.gui.ScaledResolution;

public class LocationsSection extends AbstractGUISection {

    private final TeleportationMenuScreen screen;

    private OxygenTextLabel pointsAmountTextLabel, cooldownTextLabel;

    private OxygenKeyButton createButton, moveButton;

    private OxygenSorter timeSorter, nameSorter;

    private OxygenScrollablePanel pointsPanel;

    private OxygenTextField searchField;

    private WorldPointPreview worldPointPreview;

    private OxygenCurrencyValue feeValue, balanceValue;

    private AbstractGUICallback creationCallback, pointEditingCallback, removePointCallback;

    private final int cooldownTime = PrivilegesProviderClient.getAsInt(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.id(), 
            TeleportationConfig.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.asInt()) * 1000;

    //cache

    private WorldPointPanelEntry currentEntry; 

    private WorldPoint currentPoint;

    private boolean cooldownActive;

    public LocationsSection(TeleportationMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_teleportation.gui.menu.locations"));
    }

    @Override
    public void init() {   
        this.creationCallback = new LocationCreationCallback(this.screen, this, 140, 108).enableDefaultBackground();
        this.pointEditingCallback = new EditLocationCallback(this.screen, this, 140, 128).enableDefaultBackground();
        this.removePointCallback = new LocationRemoveCallback(this.screen, this, 140, 36).enableDefaultBackground();

        this.addElement(new OxygenDefaultBackgroundWithButtonsFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_teleportation.gui.menu.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.pointsAmountTextLabel = new OxygenTextLabel(0, 22, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.timeSorter = new OxygenSorter(6, 29, EnumSorting.DOWN, ClientReference.localize("oxygen_core.gui.time")));   
        this.timeSorter.setSortingListener((sorting)->{
            this.nameSorter.reset();
            this.sortPoints(sorting == EnumSorting.DOWN ? 0 : 1);
        });

        this.addElement(this.nameSorter = new OxygenSorter(12, 29, EnumSorting.INACTIVE, ClientReference.localize("oxygen_core.gui.name")));  
        this.nameSorter.setSortingListener((sorting)->{
            this.timeSorter.reset();
            this.sortPoints(sorting == EnumSorting.DOWN ? 2 : 3);
        });

        this.addElement(this.pointsPanel = new OxygenScrollablePanel(this.screen, 6, 36, 80, 10, 1, 100, 10, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));   
        this.addElement(this.searchField = new OxygenTextField(6, 16, 60, WorldPoint.MAX_NAME_LENGTH, ""));
        this.pointsPanel.initSearchField(this.searchField);

        this.pointsPanel.<WorldPointPanelEntry>setElementClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (this.currentEntry != clicked) {
                if (this.currentEntry != null)
                    this.currentEntry.setToggled(false);
                this.currentEntry = clicked;
                this.currentPoint = TeleportationManagerClient.instance().getLocationsContainer().getLocation(clicked.getWrapped());
                clicked.toggle();                    
                this.showPointInfo(clicked.getWrapped(), false);
            }
        });

        this.pointsPanel.initContextMenu(new OxygenContextMenu(
                new LockContextAction(this),
                new EditContextAction(this),
                new RemoveContextAction(this)));

        this.addElement(this.createButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_teleportation.gui.menu.button.createLocation"), Keyboard.KEY_R, this::openCreationCallback));     

        this.addElement(this.worldPointPreview = new WorldPointPreview(91, 15));
        this.addElement(this.cooldownTextLabel = new OxygenTextLabel(130, this.getHeight() - 5, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()).setVisible(false));
        this.addElement(this.moveButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_teleportation.gui.menu.button.moveToLocation"), Keyboard.KEY_F, this::move).disableFull());  

        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getCampsSection(), this.screen.getPlayersSection()));

        this.addElement(this.feeValue = new OxygenCurrencyValue(116, this.getHeight() - 11).disableFull());  
        this.addElement(this.balanceValue = new OxygenCurrencyValue(this.getWidth() - 14, this.getHeight() - 11).disableFull());  
        if (TeleportationConfig.FEE_MODE.asInt() == 1) {
            this.feeValue.setValue(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack(), 0);
            this.balanceValue.setValue(TeleportationManagerClient.instance().getFeeStackWrapper().getCachedItemStack(), (int) this.screen.balance);
        } else {
            this.feeValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, 0L);
            this.balanceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, this.screen.balance);
        }
    }

    private void calculateButtonsHorizontalPosition() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.createButton.setX((sr.getScaledWidth() - (12 + this.textWidth(this.createButton.getDisplayText(), this.createButton.getTextScale()))) / 2 - this.screen.guiLeft);
        this.moveButton.setX(sr.getScaledWidth() / 2 + 50 - this.screen.guiLeft);
    }

    public void sortPoints(int mode) { 
        List<WorldPoint> points = new ArrayList<>(TeleportationManagerClient.instance().getLocationsContainer().getLocations());

        if (mode == 0)
            Collections.sort(points, (p1, p2)->p1.getId() < p2.getId() ? - 1 : p1.getId() > p2.getId() ? 1 : 0);
        else if (mode == 1)
            Collections.sort(points, (p1, p2)->p2.getId() < p1.getId() ? - 1 : p2.getId() > p1.getId() ? 1 : 0);
        else if (mode == 2)
            Collections.sort(points, (p1, p2)->p1.getName().compareTo(p2.getName()));
        else if (mode == 3)
            Collections.sort(points, (p1, p2)->p2.getName().compareTo(p1.getName()));

        this.pointsPanel.reset();
        for (WorldPoint worldPoint : points)
            this.pointsPanel.addEntry(new WorldPointPanelEntry(EnumWorldPoint.LOCATION, worldPoint));

        int maxAmount = TeleportationConfig.LOCATIONS_MAX_AMOUNT.asInt();
        this.pointsAmountTextLabel.setDisplayText(String.valueOf(points.size()) + "/" + String.valueOf(maxAmount));     
        this.pointsAmountTextLabel.setX(89 - this.textWidth(this.pointsAmountTextLabel.getDisplayText(), this.pointsAmountTextLabel.getTextScale()));

        this.searchField.reset();

        this.pointsPanel.getScroller().reset();
        this.pointsPanel.getScroller().updateRowsAmount(MathUtils.clamp(points.size(), 10, maxAmount));
    }

    private void openCreationCallback() {
        if (!this.searchField.isDragged())
            this.creationCallback.open();
    }

    private void move() {
        if (!this.searchField.isDragged()) {
            TeleportationManagerClient.instance().getLocationsManager().moveToLocationSynced(this.currentEntry.getWrapped());
            this.screen.close();
        }
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.createButton)
                this.creationCallback.open();
            else if (element == this.moveButton)
                this.move();
        }
    }

    public void locationsSynchronized() {
        this.sortPoints(0);
        this.updateCreateButtonState();    

        this.calculateButtonsHorizontalPosition();
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
        WorldPointPanelEntry pointButton;
        for (GUIButton button : this.pointsPanel.buttonsBuffer) {
            pointButton = (WorldPointPanelEntry) button;
            if (pointButton.getWrapped() == worldPoint.getId()) {
                pointButton.toggle();
                this.currentEntry = pointButton;
                this.currentPoint = TeleportationManagerClient.instance().getLocationsContainer().getLocation(pointButton.getWrapped());
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
        this.worldPointPreview.show(this.currentPoint, reloadImage);
        this.moveButton.enableFull();
        this.cooldownTextLabel.setVisible(this.cooldownActive);
        this.moveButton.setEnabled(!this.cooldownActive 
                && PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_LOCKED_LOCATIONS.id(), !(this.currentPoint.isLocked() && !this.currentPoint.isOwner(OxygenHelperClient.getPlayerUUID())))
                && this.screen.locationsEnabled);

        long fee = PrivilegesProviderClient.getAsLong(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_FEE.id(), TeleportationConfig.LOCATION_TELEPORTATION_FEE.asLong());
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

    public void resetPointInfo() {
        this.worldPointPreview.hide();
        this.moveButton.disableFull();
        this.cooldownTextLabel.setVisible(false);

        this.feeValue.disableFull();
        this.balanceValue.disableFull();
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
                this.moveButton.enable();
            }
        }
    }

    private long getCooldownElapsedTimeSeconds() {
        return (TeleportationManagerClient.instance().getPlayerData().getCooldownData().getNextLocationTime() - System.currentTimeMillis()) / 1000;
    }

    public void updateCreateButtonState() {
        this.createButton.setEnabled(this.screen.locationsEnabled
                && (TeleportationConfig.ALLOW_LOCATIONS_CREATION_FOR_ALL.asBoolean() || PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.LOCATIONS_CREATION.id(), false) || PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.id(), false))
                && TeleportationManagerClient.instance().getLocationsContainer().getLocationsAmount() < TeleportationConfig.LOCATIONS_MAX_AMOUNT.asInt());
    }

    public void openPointEditingCallback() {
        this.pointEditingCallback.open();
    }

    public void openRemovePointCallback() {
        this.removePointCallback.open();
    }

    public WorldPointPanelEntry getCurrentEntry() {
        return this.currentEntry;
    }

    public WorldPoint getCurrentPoint() {
        return this.currentPoint;
    }
}
