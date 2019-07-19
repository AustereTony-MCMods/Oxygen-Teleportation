package austeretony.oxygen_teleportation.common.event;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.event.OxygenPlayerLoadedEvent;
import austeretony.oxygen.common.api.event.OxygenWorldLoadedEvent;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TeleportationEventsServer {

    @SubscribeEvent
    public void onWorldLoaded(OxygenWorldLoadedEvent event) {        
        TeleportationManagerServer.instance().reset();
        OxygenHelperServer.loadPersistentDataDelegated(TeleportationManagerServer.instance().getWorldData());
        TeleportationManagerServer.instance().getImagesLoader().loadLocationPreviewImagesDelegated();
        OxygenHelperServer.loadPersistentDataDelegated(TeleportationManagerServer.instance().getSharedCampsManager());

        TeleportationMain.addDefaultPrivilegesDelegated();
    }

    @SubscribeEvent
    public void onPlayerLoaded(OxygenPlayerLoadedEvent event) {        
        TeleportationManagerServer.instance().onPlayerLoaded(event.player);
    }
}
