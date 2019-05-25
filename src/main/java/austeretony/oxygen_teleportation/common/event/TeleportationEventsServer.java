package austeretony.oxygen_teleportation.common.event;

import austeretony.oxygen.common.api.event.OxygenPlayerLoadedEvent;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TeleportationEventsServer {

    @SubscribeEvent
    public void onPlayerLoaded(OxygenPlayerLoadedEvent event) {        
        TeleportationManagerServer.instance().onPlayerLoaded(event.player);
    }
}
