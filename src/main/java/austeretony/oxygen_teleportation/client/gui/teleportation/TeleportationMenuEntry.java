package austeretony.oxygen_teleportation.client.gui.teleportation;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.menu.AbstractMenuEntry;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;

public class TeleportationMenuEntry extends AbstractMenuEntry {

    @Override
    public String getName() {
        return "oxygen_teleportation.gui.menu.title";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void open() {
        ClientReference.displayGuiScreen(null);
        TeleportationManagerClient.instance().getTeleportationMenuManager().openMenu();
    }
}
