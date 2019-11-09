package austeretony.oxygen_teleportation.client.input;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class TeleportationMenuKeyHandler {

    public static final KeyBinding TELEPORTATION_MENU = new KeyBinding("key.teleportation.openMenu", Keyboard.KEY_Y, "Oxygen");

    public TeleportationMenuKeyHandler() {
        ClientReference.registerKeyBinding(TELEPORTATION_MENU);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (TELEPORTATION_MENU.isPressed())
            TeleportationManagerClient.instance().getTeleportationMenuManager().openMenu();
    }   
}