package austeretony.oxygen_teleportation.client.input;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class TeleportationKeyHandler {

    public static final KeyBinding 
    OPEN_MENU = new KeyBinding("key.teleportation.openMenu", Keyboard.KEY_Y, OxygenMain.NAME),
    MOVE_TO_CAMP = new KeyBinding("key.teleportation.moveToCamp", Keyboard.KEY_H, OxygenMain.NAME);

    public TeleportationKeyHandler() {
        ClientReference.registerKeyBinding(OPEN_MENU);
        ClientReference.registerKeyBinding(MOVE_TO_CAMP);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (OPEN_MENU.isPressed())
            TeleportationManagerClient.instance().openMenuSynced();
        else if (MOVE_TO_CAMP.isPressed()) 
            TeleportationManagerClient.instance().getCampsManager().moveToFavoriteCampSynced(TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId());       
    }   
}
