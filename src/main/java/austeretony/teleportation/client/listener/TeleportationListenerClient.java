package austeretony.teleportation.client.listener;

import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.core.api.listeners.client.IChatMessageInfoListener;
import austeretony.oxygen.common.core.api.listeners.client.ICientInitListener;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.main.EnumTeleportationChatMessages;
import austeretony.teleportation.common.main.TeleportationMain;

public class TeleportationListenerClient implements ICientInitListener, IChatMessageInfoListener {

    @Override
    public String getModId() {
        return TeleportationMain.MODID;
    }

    @Override
    public void onClientInit() {
        TeleportationMain.LOGGER.info("Initialized client data.");
        TeleportationManagerClient.instance().reset();
        TeleportationManagerClient.instance().getPlayerData().setPlayerUUID(OxygenHelperClient.getPlayerUUID());
        TeleportationManagerClient.instance().getCampsLoader().loadCampsDataDelegated();
        TeleportationManagerClient.instance().getLocationsLoader().loadLocationsDataDelegated();
    }

    @Override
    public void onChatMessage(int mod, int message, String... args) {
        if (mod == TeleportationMain.TELEPORTATION_MOD_INDEX)
            EnumTeleportationChatMessages.values()[message].show(args);
    }
}
