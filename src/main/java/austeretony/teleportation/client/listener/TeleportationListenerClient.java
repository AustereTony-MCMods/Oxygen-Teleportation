package austeretony.teleportation.client.listener;

import austeretony.oxygen.common.core.api.listeners.client.IChatMessageInfoListener;
import austeretony.oxygen.common.core.api.listeners.client.ICientInitListener;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.main.EnumChatMessages;
import austeretony.teleportation.common.main.TeleportationMain;

public class TeleportationListenerClient implements ICientInitListener, IChatMessageInfoListener {

    @Override
    public String getModId() {
        return OxygenMain.MODID;
    }

    @Override
    public void onClientInit() {
        TeleportationMain.LOGGER.info("Initialized client data.");
        TeleportationManagerClient.create();
        TeleportationManagerClient.instance().getCampsLoader().loadCampsDataDelegated();
        TeleportationManagerClient.instance().getLocationsLoader().loadLocationsDataDelegated();
    }

    @Override
    public void onChatMessage(int mod, int message, String... args) {
        if (mod == TeleportationMain.TELEPORTATION_MOD_INDEX)
            EnumChatMessages.values()[message].show(args);
    }
}
