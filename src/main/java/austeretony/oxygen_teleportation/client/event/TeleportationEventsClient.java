package austeretony.oxygen_teleportation.client.event;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.api.event.OxygenChatMessageEvent;
import austeretony.oxygen.client.api.event.OxygenClientInitEvent;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationChatMessages;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TeleportationEventsClient {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        TeleportationManagerClient.instance().reset();
        TeleportationManagerClient.instance().initPlayerData();

        OxygenHelperClient.loadPersistentDataDelegated(TeleportationManagerClient.instance().getPlayerData());
        TeleportationManagerClient.instance().getImagesLoader().loadCampPreviewImagesDelegated();

        OxygenHelperClient.loadPersistentDataDelegated(TeleportationManagerClient.instance().getSharedCampsManager());

        OxygenHelperClient.loadPersistentDataDelegated(TeleportationManagerClient.instance().getWorldData());
        TeleportationManagerClient.instance().getImagesLoader().loadLocationPreviewImagesDelegated();   
    }

    @SubscribeEvent
    public void onChatMessage(OxygenChatMessageEvent event) {
        if (event.modIndex == TeleportationMain.TELEPORTATION_MOD_INDEX)
            EnumTeleportationChatMessages.values()[event.messageIndex].show(event.args);
    }
}
