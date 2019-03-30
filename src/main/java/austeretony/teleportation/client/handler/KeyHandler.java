package austeretony.teleportation.client.handler;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen.client.reference.ClientReference;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.menu.players.PlayersManagerClient;
import austeretony.teleportation.common.network.server.SPJumpRequestReply;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyHandler {

    public static final KeyBinding 
    ACCEPT = registerKeyBinding("key.oxygen.accept", Keyboard.KEY_R, OxygenMain.NAME),
    REJECT = registerKeyBinding("key.oxygen.reject", Keyboard.KEY_X, OxygenMain.NAME),
    OPEN_MENU = registerKeyBinding("key.teleportation.openMenu", Keyboard.KEY_P, TeleportationMain.NAME),
    MOVE_TO_CAMP = registerKeyBinding("key.teleportation.moveToCamp", Keyboard.KEY_H, TeleportationMain.NAME);

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        int keyCode = Keyboard.getEventKey();
        boolean isDown = Keyboard.getEventKeyState();
        if (OPEN_MENU.isPressed())
            CampsManagerClient.instance().openMenuSynced();
        else if (MOVE_TO_CAMP.isPressed()) {
            if (TeleportationConfig.ENABLE_FAVORITE_CAMP.getBooleanValue())
                CampsManagerClient.instance().moveToCampSynced(CampsManagerClient.instance().getPlayerProfile().getFavoriteCampId());
        } else if (ACCEPT.isPressed())
            PlayersManagerClient.instance().replyJumpRequestSynced(SPJumpRequestReply.EnumReply.ACCEPT);
        else if (REJECT.isPressed())
            PlayersManagerClient.instance().replyJumpRequestSynced(SPJumpRequestReply.EnumReply.REJECT);
    }

    public static KeyBinding registerKeyBinding(String name, int keyCode, String category) {
        KeyBinding keyBinding = new KeyBinding(name, keyCode, category);
        ClientReference.registerKeyBinding(keyBinding);
        return keyBinding;
    }
}
