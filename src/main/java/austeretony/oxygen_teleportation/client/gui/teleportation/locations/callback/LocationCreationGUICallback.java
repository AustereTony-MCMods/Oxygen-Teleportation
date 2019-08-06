package austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsGUISection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback.CampCreationCallbackGUIFiller;
import austeretony.oxygen_teleportation.common.main.WorldPoint;

public class LocationCreationGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final LocationsGUISection section;

    private GUITextField nameField, descriptionField;

    private GUIButton confirmButton, cancelButton;

    public LocationCreationGUICallback(TeleportationMenuGUIScreen screen, LocationsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new CampCreationCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 2).setDisplayText(ClientReference.localize("teleportation.gui.menu.locationCreationCallback"), true, GUISettings.instance().getTitleScale()));   
        this.addElement(new GUITextLabel(2, 16).setDisplayText(ClientReference.localize("oxygen.gui.name"), false, GUISettings.instance().getSubTextScale()));    
        this.addElement(new GUITextLabel(2, 36).setDisplayText(ClientReference.localize("oxygen.gui.description"), false, GUISettings.instance().getSubTextScale()));    

        this.addElement(this.nameField = new GUITextField(2, 25, 136, 9, WorldPoint.MAX_POINT_NAME_LENGTH).setTextScale(GUISettings.instance().getSubTextScale())
                .enableDynamicBackground(GUISettings.instance().getEnabledTextFieldColor(), GUISettings.instance().getDisabledTextFieldColor(), GUISettings.instance().getHoveredTextFieldColor())
                .setLineOffset(3).cancelDraggedElementLogic());
        this.addElement(this.descriptionField = new GUITextField(2, 45, 136, 9, WorldPoint.MAX_POINT_DESCRIPTION_LENGTH).setTextScale(GUISettings.instance().getSubTextScale())
                .enableDynamicBackground(GUISettings.instance().getEnabledTextFieldColor(), GUISettings.instance().getDisabledTextFieldColor(), GUISettings.instance().getHoveredTextFieldColor())
                .setLineOffset(3).cancelDraggedElementLogic());

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.confirmButton"), true, GUISettings.instance().getButtonTextScale()));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.cancelButton"), true, GUISettings.instance().getButtonTextScale()));
    }

    @Override
    protected void onClose() {
        this.nameField.reset();
        this.descriptionField.reset();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            String name = this.nameField.getTypedText().isEmpty() ? ClientReference.localize("teleportation.gui.menu.locationGenericName") 
                    + " #" + String.valueOf(TeleportationManagerClient.instance().getWorldData().getLocationsAmount() + 1) : this.nameField.getTypedText();
                    TeleportationManagerClient.instance().getLocationsManager().createLocationPointSynced(name, this.descriptionField.getTypedText());
                    this.section.sortPoints(0);
                    this.section.lockCreateButton();     
                    this.close();
        }
    }
}
