package austeretony.oxygen_teleportation.client.gui.menu;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.settings.EnumTeleportationClientSetting;

public class TeleportationMenuEntry implements OxygenMenuEntry {

    @Override
    public int getId() {
        return 10;
    }

    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_teleportation.gui.menu.title");
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_T;
    }

    @Override
    public boolean isValid() {
        return EnumTeleportationClientSetting.ADD_TELEPORTATION_MENU.get().asBoolean();
    }

    @Override
    public void open() {
        ClientReference.displayGuiScreen(null);
        TeleportationManagerClient.instance().getTeleportationMenuManager().openMenu();
    }
}
