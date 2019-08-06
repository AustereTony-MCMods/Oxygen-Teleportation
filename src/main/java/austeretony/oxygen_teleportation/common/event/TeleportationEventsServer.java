package austeretony.oxygen_teleportation.common.event;

import austeretony.oxygen.common.api.event.OxygenPlayerLoadedEvent;
import austeretony.oxygen.common.api.event.OxygenPlayerUnloadedEvent;
import austeretony.oxygen.common.api.event.OxygenPrivilegesLoadedEvent;
import austeretony.oxygen.common.api.event.OxygenWorldLoadedEvent;
import austeretony.oxygen_teleportation.common.TeleportationLoaderServer;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TeleportationEventsServer {

    @SubscribeEvent
    public void onWorldLoaded(OxygenWorldLoadedEvent event) {        
        TeleportationManagerServer.instance().reset();
        TeleportationLoaderServer.loadPersistentDataDelegated(TeleportationManagerServer.instance().getWorldData());
        TeleportationManagerServer.instance().getImagesLoader().loadLocationPreviewImagesDelegated();
        TeleportationLoaderServer.loadPersistentDataDelegated(TeleportationManagerServer.instance().getSharedCampsManager());
    }

    @SubscribeEvent
    public void onPrivilegesLoaded(OxygenPrivilegesLoadedEvent event) {
        TeleportationMain.addDefaultPrivileges();
    }

    @SubscribeEvent
    public void onPlayerLoaded(OxygenPlayerLoadedEvent event) {        
        TeleportationManagerServer.instance().onPlayerLoaded(event.player);
    }

    @SubscribeEvent
    public void onPlayerUnloaded(OxygenPlayerUnloadedEvent event) {        
        TeleportationManagerServer.instance().onPlayerUnloaded(event.player);
    }
}
