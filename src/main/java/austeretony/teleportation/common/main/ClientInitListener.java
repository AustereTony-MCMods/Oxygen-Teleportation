package austeretony.teleportation.common.main;

import austeretony.oxygen.common.api.ICientInitListener;
import austeretony.teleportation.client.TeleportationManagerClient;

public class ClientInitListener implements ICientInitListener {

    @Override
    public String getModId() {
        return TeleportationMain.MODID;
    }

    @Override
    public void init() {
        TeleportationMain.LOGGER.info("Initialized client data.");
        TeleportationManagerClient.create();
        TeleportationManagerClient.instance().getCampsLoader().loadCampsDataDelegated();
    }
}
