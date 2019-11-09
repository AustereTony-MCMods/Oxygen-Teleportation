package austeretony.oxygen_teleportation.server.event;

import austeretony.oxygen_core.server.api.event.OxygenPlayerLoadedEvent;
import austeretony.oxygen_core.server.api.event.OxygenPlayerUnloadedEvent;
import austeretony.oxygen_core.server.api.event.OxygenPrivilegesLoadedEvent;
import austeretony.oxygen_core.server.api.event.OxygenWorldLoadedEvent;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TeleportationEventsServer {

    @SubscribeEvent
    public void onWorldLoaded(OxygenWorldLoadedEvent event) {        
        TeleportationManagerServer.instance().worldLoaded();
    }

    @SubscribeEvent
    public void onPrivilegesLoaded(OxygenPrivilegesLoadedEvent event) {
        TeleportationMain.addDefaultPrivileges();
    }

    @SubscribeEvent
    public void onPlayerLoaded(OxygenPlayerLoadedEvent event) {        
        TeleportationManagerServer.instance().onPlayerLoaded(event.playerMP);
    }

    @SubscribeEvent
    public void onPlayerUnloaded(OxygenPlayerUnloadedEvent event) {        
        TeleportationManagerServer.instance().onPlayerUnloaded(event.playerMP);
    }
}
