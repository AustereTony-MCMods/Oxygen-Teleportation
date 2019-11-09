package austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackGUIFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.elements.UsernameGUITextField;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsGUISection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;

public class InviteGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final CampsGUISection section;

    private UsernameGUITextField usernameField;

    private OxygenGUIButton confirmButton, cancelButton;

    private boolean initialized;

    public InviteGUICallback(TeleportationMenuGUIScreen screen, CampsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new OxygenCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));        
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_teleportation.gui.menu.callback.invitePlayer"), GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()));   
        this.addElement(new OxygenGUIText(6, 18, ClientReference.localize("oxygen_teleportation.gui.menu.callback.invitePlayer.request"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColor()));  

        this.addElement(this.confirmButton = new OxygenGUIButton(15, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.confirmButton")).disable());
        this.addElement(this.cancelButton = new OxygenGUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.cancelButton")));

        this.addElement(this.usernameField = new UsernameGUITextField(6, 25, this.getWidth() - 12));    

        this.usernameField.setUsernameSelectListener((sharedData)->this.confirmButton.enable());
    }

    @Override
    protected void onOpen() {
        if (!this.initialized) {
            this.initialized = true;
            this.usernameField.load();
        }
    }

    @Override
    protected void onClose() {
        this.usernameField.reset();
        this.confirmButton.disable();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        boolean flag = super.keyTyped(typedChar, keyCode);   
        if (this.usernameField.isDragged())
            this.confirmButton.setEnabled(OxygenHelperClient.isPlayerAvailable(this.usernameField.getTypedText()));
        return flag;   
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton) {
                TeleportationManagerClient.instance().getPlayerDataManager().invitePlayerSynced(
                        this.section.getCurrentPoint().getId(),
                        OxygenHelperClient.getPlayerSharedData(this.usernameField.getTypedText()).getPlayerUUID());
                this.close();
            }
        }
    }
}
