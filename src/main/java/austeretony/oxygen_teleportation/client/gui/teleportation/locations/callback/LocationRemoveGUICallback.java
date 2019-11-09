package austeretony.oxygen_teleportation.client.gui.teleportation.locations.callback;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackGUIFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsGUISection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;

public class LocationRemoveGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final LocationsGUISection section;

    private OxygenGUIButton confirmButton, cancelButton;

    public LocationRemoveGUICallback(TeleportationMenuGUIScreen screen, LocationsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;   
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new OxygenCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_teleportation.gui.menu.callback.removeLocation"), GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()));
        this.addElement(new OxygenGUIText(6, 18, ClientReference.localize("oxygen_teleportation.gui.menu.callback.removeLocation.request"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.confirmButton = new OxygenGUIButton(15, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.confirmButton")));
        this.addElement(this.cancelButton = new OxygenGUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.cancelButton")));
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton) {
                TeleportationManagerClient.instance().getLocationsManager().removeLocationPointSynced(this.section.getCurrentPoint().getId());
                this.close();
            }
        }
    }
}