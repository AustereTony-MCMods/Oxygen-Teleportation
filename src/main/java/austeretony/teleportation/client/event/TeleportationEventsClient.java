package austeretony.teleportation.client.event;

import austeretony.oxygen.client.event.OxygenChatMessageEvent;
import austeretony.oxygen.client.event.OxygenClientInitEvent;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.main.EnumTeleportationChatMessages;
import austeretony.teleportation.common.main.TeleportationMain;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TeleportationEventsClient {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        TeleportationManagerClient.instance().reset();
        TeleportationManagerClient.instance().getPlayerData().setPlayerUUID(OxygenHelperClient.getPlayerUUID());
        TeleportationManagerClient.instance().getCampsLoader().loadCampsDataDelegated();
        TeleportationManagerClient.instance().getLocationsLoader().loadLocationsDataDelegated();
    }

    @SubscribeEvent
    public void onChatMessage(OxygenChatMessageEvent event) {
        if (event.modIndex == TeleportationMain.TELEPORTATION_MOD_INDEX)
            EnumTeleportationChatMessages.values()[event.messageIndex].show(event.args);
    }
}
