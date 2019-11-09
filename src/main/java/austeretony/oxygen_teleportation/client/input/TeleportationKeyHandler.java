package austeretony.oxygen_teleportation.client.input;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class TeleportationKeyHandler {

    public static final KeyBinding MOVE_TO_CAMP = new KeyBinding("key.teleportation.moveToCamp", Keyboard.KEY_H, "Oxygen");

    public TeleportationKeyHandler() {
        ClientReference.registerKeyBinding(MOVE_TO_CAMP);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (MOVE_TO_CAMP.isPressed()) 
            TeleportationManagerClient.instance().getPlayerDataManager().moveToFavoriteCampSynced();       
    }   
}
