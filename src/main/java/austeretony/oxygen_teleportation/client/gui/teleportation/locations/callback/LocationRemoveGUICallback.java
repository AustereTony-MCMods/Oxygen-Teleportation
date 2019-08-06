package austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsGUISection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback.CampRemoveCallbackGUIFiller;

public class LocationRemoveGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final LocationsGUISection section;

    private GUITextLabel requestLabel;

    private GUIButton confirmButton, cancelButton;

    public LocationRemoveGUICallback(TeleportationMenuGUIScreen screen, LocationsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;   
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new CampRemoveCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 2).setDisplayText(ClientReference.localize("teleportation.gui.menu.removeLocationCallback"), true, GUISettings.instance().getTitleScale()));
        this.addElement(this.requestLabel = new GUITextLabel(2, 16));     

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.confirmButton"), true, GUISettings.instance().getButtonTextScale()));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.cancelButton"), true, GUISettings.instance().getButtonTextScale()));
    }

    @Override
    protected void onOpen() {
        this.requestLabel.setDisplayText(ClientReference.localize("teleportation.gui.menu.removeCallback.request", this.section.getCurrentPoint().getName()), true, GUISettings.instance().getTextScale());
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            TeleportationManagerClient.instance().getLocationsManager().removeLocationPointSynced(this.section.getCurrentPoint().getId());
            this.section.sortPoints(0);
            this.section.unlockCreateButton();
            this.close();
        }
    }
}
