package austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackBackgroundFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.elements.OxygenUsernameField;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsSection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuScreen;

public class InviteCallback extends AbstractGUICallback {

    private final TeleportationMenuScreen screen;

    private final CampsSection section;

    private OxygenUsernameField usernameField;

    private OxygenKeyButton confirmButton, cancelButton;

    private boolean initialized;

    public InviteCallback(TeleportationMenuScreen screen, CampsSection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.enableDefaultBackground(EnumBaseGUISetting.FILL_CALLBACK_COLOR.get().asInt());
        this.addElement(new OxygenCallbackBackgroundFiller(0, 0, this.getWidth(), this.getHeight())); 
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_teleportation.gui.menu.callback.invitePlayer"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));
        this.addElement(new OxygenTextLabel(6, 23, ClientReference.localize("oxygen_teleportation.gui.menu.callback.invitePlayer.request"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.confirmButton = new OxygenKeyButton(15, this.getHeight() - 10, ClientReference.localize("oxygen_core.gui.confirm"), Keyboard.KEY_R, this::confirm).disable());
        this.addElement(this.cancelButton = new OxygenKeyButton(this.getWidth() - 55, this.getHeight() - 10, ClientReference.localize("oxygen_core.gui.cancel"), Keyboard.KEY_X, this::close));

        this.addElement(this.usernameField = new OxygenUsernameField(6, 25, this.getWidth() - 12));    
        this.usernameField.setUsernameSelectListener((sharedData)->this.confirmButton.enable());
    }

    @Override
    protected void onOpen() {
        this.usernameField.reset();
        this.confirmButton.disable();
        if (!this.initialized) {
            this.initialized = true;
            this.usernameField.load();
        }
    }

    private void confirm() {
        if (!this.usernameField.isDragged()) {
            TeleportationManagerClient.instance().getPlayerDataManager().invitePlayerSynced(
                    this.section.getCurrentPoint().getId(),
                    OxygenHelperClient.getPlayerSharedData(this.usernameField.getTypedText()).getPlayerUUID());
            this.close();
        }
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton)
                this.confirm();
        }
    }
}
