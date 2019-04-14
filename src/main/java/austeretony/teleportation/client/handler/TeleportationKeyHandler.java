package austeretony.teleportation.client.handler;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen.client.handler.KeyBindingWrapper;
import austeretony.oxygen.common.core.api.ClientReference;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.TeleportationMain;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class TeleportationKeyHandler {

    public static final KeyBindingWrapper 
    OPEN_MENU = new KeyBindingWrapper(),
    MOVE_TO_CAMP = new KeyBindingWrapper();

    public TeleportationKeyHandler() {
        OPEN_MENU.register("key.teleportation.openMenu", Keyboard.KEY_P, TeleportationMain.NAME);
        if (TeleportationConfig.ENABLE_FAVORITE_CAMP.getBooleanValue())
            MOVE_TO_CAMP.register("key.teleportation.moveToCamp", Keyboard.KEY_H, TeleportationMain.NAME);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (OPEN_MENU.registered() && OPEN_MENU.getKeyBinding().isPressed())
            TeleportationManagerClient.instance().openMenuSynced();
        else if (MOVE_TO_CAMP.registered() && MOVE_TO_CAMP.getKeyBinding().isPressed()) 
            TeleportationManagerClient.instance().getCampsManager().moveToCampSynced(TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId());       
    }   
}
