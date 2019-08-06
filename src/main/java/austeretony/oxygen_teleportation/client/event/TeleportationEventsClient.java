package austeretony.oxygen_teleportation.client.event;

import austeretony.oxygen.client.api.event.OxygenChatMessageEvent;
import austeretony.oxygen.client.api.event.OxygenClientInitEvent;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationChatMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TeleportationEventsClient {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        TeleportationManagerClient.instance().init();
    }

    @SubscribeEvent
    public void onChatMessage(OxygenChatMessageEvent event) {
        if (event.modIndex == TeleportationMain.TELEPORTATION_MOD_INDEX)
            EnumTeleportationChatMessage.values()[event.messageIndex].show(event.args);
    }
}
