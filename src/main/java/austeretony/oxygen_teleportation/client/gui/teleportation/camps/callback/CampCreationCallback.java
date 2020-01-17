package austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.elements.OxygenButton;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackBackgroundFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenTextBoxField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsSection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuScreen;
import austeretony.oxygen_teleportation.common.WorldPoint;

public class CampCreationCallback extends AbstractGUICallback {

    private final TeleportationMenuScreen screen;

    private final CampsSection section;

    private OxygenTextField nameField;

    private OxygenTextBoxField descriptionBoxField;

    private OxygenButton confirmButton, cancelButton;

    public CampCreationCallback(TeleportationMenuScreen screen, CampsSection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override   
    public void init() {
        this.enableDefaultBackground(EnumBaseGUISetting.FILL_CALLBACK_COLOR.get().asInt());
        this.addElement(new OxygenCallbackBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_teleportation.gui.menu.callback.campCreation"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 23, ClientReference.localize("oxygen_core.gui.name"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 43, ClientReference.localize("oxygen_core.gui.description"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.nameField = new OxygenTextField(6, 25, this.getWidth() - 12, WorldPoint.MAX_NAME_LENGTH, ""));
        this.addElement(this.descriptionBoxField = new OxygenTextBoxField(6, 45, this.getWidth() - 12, 50, WorldPoint.MAX_DESCRIPTION_LENGTH));

        this.addElement(this.confirmButton = new OxygenButton(15, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen_core.gui.confirm")));
        this.confirmButton.setKeyPressListener(Keyboard.KEY_R, ()->this.confirm(false));

        this.addElement(this.cancelButton = new OxygenButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen_core.gui.cancel")));
        this.cancelButton.setKeyPressListener(Keyboard.KEY_X, ()->this.close(false));
    }

    @Override
    protected void onOpen() {
        this.nameField.reset();
        this.descriptionBoxField.reset();
    }

    private void confirm(boolean mouseClick) {
        if (mouseClick || !this.nameField.isDragged()
                && !this.descriptionBoxField.isDragged()) {
            TeleportationManagerClient.instance().getPlayerDataManager().createCampPointSynced(this.nameField.getTypedText(), this.descriptionBoxField.getTypedText());
            this.close();
        }
    }

    private void close(boolean mouseClick) {
        if (mouseClick || (!this.nameField.isDragged()
                && !this.descriptionBoxField.isDragged()))
            this.close();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close(true);
            else if (element == this.confirmButton)
                this.confirm(true);
        }
    }
}
