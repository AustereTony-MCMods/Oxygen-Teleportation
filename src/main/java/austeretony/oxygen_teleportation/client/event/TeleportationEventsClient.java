package austeretony.oxygen_teleportation.client.event;

import austeretony.oxygen_core.client.api.event.OxygenClientInitEvent;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TeleportationEventsClient {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        TeleportationManagerClient.instance().init();
    }
}
