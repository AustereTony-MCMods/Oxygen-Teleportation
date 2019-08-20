package austeretony.oxygen_teleportation.client.input;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.sync.gui.api.ComplexGUIHandlerClient;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
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
        if (TELEPORTATION_MENU.isPressed()) {
            ClientReference.getGameSettings().hideGUI = true;
            ComplexGUIHandlerClient.openScreen(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
        }
    }   
}