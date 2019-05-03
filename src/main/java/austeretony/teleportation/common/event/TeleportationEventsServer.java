package austeretony.teleportation.common.event;

import austeretony.oxygen.common.event.OxygenPlayerLoadedEvent;
import austeretony.teleportation.common.TeleportationManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class TeleportationEventsServer {

    @SubscribeEvent
    public void onPlayerLoaded(OxygenPlayerLoadedEvent event) {        
        TeleportationManagerServer.instance().onPlayerLoaded(event.player);
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerLoggedOutEvent event) {
        TeleportationManagerServer.instance().onPlayerLoggedOut(event.player);
    }
}
