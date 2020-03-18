package austeretony.oxygen_teleportation.client.input;

import javax.annotation.Nullable;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.TeleportationMenuManager;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class TeleportationKeyHandler {

    private KeyBinding teleportationMenuKeybinding, moveToFavCampKeybinding;

    public TeleportationKeyHandler() {        
        if (TeleportationConfig.ENABLE_TELEPORTATION_MENU_KEY.asBoolean() && !OxygenGUIHelper.isOxygenMenuEnabled())
            ClientReference.registerKeyBinding(this.teleportationMenuKeybinding = new KeyBinding("key.teleportation.teleportationMenu", TeleportationConfig.TELEPORTATION_MENU_KEY.asInt(), "Oxygen"));
        if (TeleportationConfig.ENABLE_FAVORITE_CAMP_KEY.asBoolean())
            ClientReference.registerKeyBinding(this.moveToFavCampKeybinding = new KeyBinding("key.teleportation.moveToFavCamp", TeleportationConfig.FAVORITE_CAMP_KEY.asInt(), "Oxygen"));
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (this.teleportationMenuKeybinding != null && this.teleportationMenuKeybinding.isPressed())
            TeleportationMenuManager.openTeleportationMenu();
        else if (this.moveToFavCampKeybinding != null && this.moveToFavCampKeybinding.isPressed()) 
            TeleportationManagerClient.instance().getPlayerDataManager().moveToFavoriteCampSynced();       
    }   

    @Nullable
    public KeyBinding getTeleportationMenuKeybinding() {
        return this.teleportationMenuKeybinding;
    }

    @Nullable
    public KeyBinding getMoveToFavCampKeybinding() {
        return this.moveToFavCampKeybinding;
    }
}
