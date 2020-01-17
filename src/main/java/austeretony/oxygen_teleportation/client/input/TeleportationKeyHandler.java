package austeretony.oxygen_teleportation.client.input;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class TeleportationKeyHandler {

    private KeyBinding teleportationMenuKeybinding, moveToFavCampKeybinding;

    public TeleportationKeyHandler() {        
        if (TeleportationConfig.ENABLE_TELEPORTATION_MENU_KEY.asBoolean() && !OxygenGUIHelper.isOxygenMenuEnabled())
            ClientReference.registerKeyBinding(this.teleportationMenuKeybinding = new KeyBinding("key.teleportation.teleportationMenu", Keyboard.KEY_Y, "Oxygen"));
        if (TeleportationConfig.ENABLE_FAVORITE_CAMP_KEY.asBoolean())
            ClientReference.registerKeyBinding(this.moveToFavCampKeybinding = new KeyBinding("key.teleportation.moveToFavCamp", Keyboard.KEY_C, "Oxygen"));
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (this.teleportationMenuKeybinding != null && this.teleportationMenuKeybinding.isPressed())
            TeleportationManagerClient.instance().getTeleportationMenuManager().openMenu();
        else if (this.moveToFavCampKeybinding != null && this.moveToFavCampKeybinding.isPressed()) 
            TeleportationManagerClient.instance().getPlayerDataManager().moveToFavoriteCampSynced();       
    }   

    public KeyBinding getTeleportationMenuKeybinding() {
        return this.teleportationMenuKeybinding;
    }

    public KeyBinding getMoveToFavCampKeybinding() {
        return this.moveToFavCampKeybinding;
    }
}
