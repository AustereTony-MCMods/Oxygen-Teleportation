package austeretony.oxygen_teleportation.client.gui;

import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.AbstractMenuEntry;
import austeretony.oxygen.client.sync.gui.api.ComplexGUIHandlerClient;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraft.util.ResourceLocation;

public class TeleportationMenuEntry extends AbstractMenuEntry {

    @Override
    public String getName() {
        return "teleportation.gui.menu.title";
    }

    @Override
    public ResourceLocation getIcon() {
        //TODO
        return null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void open() {
        ClientReference.getGameSettings().hideGUI = true;
        ComplexGUIHandlerClient.openScreen(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
    }
}
