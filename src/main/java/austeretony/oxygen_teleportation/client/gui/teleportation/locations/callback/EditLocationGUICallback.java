package austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUICheckBoxButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextBoxField;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsGUISection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback.EditCampCallbackGUIFiller;
import austeretony.oxygen_teleportation.common.main.WorldPoint;

public class EditLocationGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final LocationsGUISection section;

    public GUITextField nameField;

    private GUITextBoxField descriptionField;

    private GUICheckBoxButton updateImageButton, updatePositionButton;

    private GUITextLabel updateImageTextLabel, updatePositionTextLabel;

    private GUIButton confirmButton, cancelButton;

    public EditLocationGUICallback(TeleportationMenuGUIScreen screen, LocationsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new EditCampCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 2).setDisplayText(ClientReference.localize("teleportation.gui.menu.editLocationCallback"), true, GUISettings.instance().getTitleScale()));       
        this.addElement(new GUITextLabel(2, 16).setDisplayText(ClientReference.localize("oxygen.gui.username"), false, GUISettings.instance().getSubTextScale()));    
        this.addElement(new GUITextLabel(2, 36).setDisplayText(ClientReference.localize("oxygen.gui.note"), false, GUISettings.instance().getSubTextScale()));    

        this.addElement(this.nameField = new GUITextField(2, 25, 136, 9, WorldPoint.MAX_NAME_LENGTH).setTextScale(GUISettings.instance().getSubTextScale())
                .enableDynamicBackground(GUISettings.instance().getEnabledTextFieldColor(), GUISettings.instance().getDisabledTextFieldColor(), GUISettings.instance().getHoveredTextFieldColor())
                .setLineOffset(3).cancelDraggedElementLogic());

        this.addElement(this.descriptionField = new GUITextBoxField(2, 45, 136, 50, WorldPoint.MAX_DESCRIPTION_LENGTH).setLineOffset(2).setTextScale(GUISettings.instance().getSubTextScale()).enableDynamicBackground().cancelDraggedElementLogic());

        this.addElement(this.updateImageButton = new GUICheckBoxButton(2, 99, 6).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor()));
        this.addElement(this.updatePositionButton = new GUICheckBoxButton(2, 109, 6).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor()));
        this.addElement(this.updateImageTextLabel = new GUITextLabel(10, 98).setDisplayText(ClientReference.localize("teleportation.gui.menu.updateImage"), false, GUISettings.instance().getSubTextScale())); 
        this.addElement(this.updatePositionTextLabel = new GUITextLabel(10, 108).setDisplayText(ClientReference.localize("teleportation.gui.menu.updatePosition"), false, GUISettings.instance().getSubTextScale()));    

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.confirmButton"), true, GUISettings.instance().getButtonTextScale()));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.cancelButton"), true, GUISettings.instance().getButtonTextScale()));
    }

    @Override
    protected void onOpen() {
        this.nameField.setText(this.section.getCurrentPoint().getName());
        this.descriptionField.setText(this.section.getCurrentPoint().getDescription());
    }

    @Override
    protected void onClose() {
        this.nameField.reset();
        this.descriptionField.reset();
        this.updateImageButton.setToggled(false);
        this.updatePositionButton.setToggled(false);
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            String 
            name = this.nameField.getTypedText(),
            description = this.descriptionField.getTypedText();
            TeleportationManagerClient.instance().getLocationsManager().editLocationPointSynced(this.section.getCurrentPoint(), name, description, this.updateImageButton.isToggled(), this.updatePositionButton.isToggled());
            this.section.sortPoints(0);
            this.close();
        }
    }
}
