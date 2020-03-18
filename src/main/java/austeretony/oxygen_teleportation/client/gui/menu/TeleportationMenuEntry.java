package austeretony.oxygen_teleportation.client.gui.menu;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_teleportation.client.TeleportationMenuManager;
import austeretony.oxygen_teleportation.client.settings.EnumTeleportationClientSetting;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class TeleportationMenuEntry implements OxygenMenuEntry {

    @Override
    public int getId() {
        return TeleportationMain.TELEPORTATION_MENU_SCREEN_ID;
    }

    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_teleportation.gui.menu.title");
    }

    @Override
    public int getKeyCode() {
        return TeleportationConfig.TELEPORTATION_MENU_KEY.asInt();
    }

    @Override
    public boolean isValid() {
        return EnumTeleportationClientSetting.ADD_TELEPORTATION_MENU.get().asBoolean();
    }

    @Override
    public void open() {
        TeleportationMenuManager.openTeleportationMenu();
    }
}
