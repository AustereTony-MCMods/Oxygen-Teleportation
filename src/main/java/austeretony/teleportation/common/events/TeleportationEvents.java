package austeretony.teleportation.common.events;

import austeretony.teleportation.common.TeleportationManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class TeleportationEvents {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        TeleportationManagerServer.instance().onPlayerLoggedIn(event.player);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        TeleportationManagerServer.instance().onPlayerLoggedOut(event.player);
    }
}
