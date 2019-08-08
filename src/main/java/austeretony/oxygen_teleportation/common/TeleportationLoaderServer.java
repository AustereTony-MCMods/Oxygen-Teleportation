package austeretony.oxygen_teleportation.common;

import austeretony.oxygen.common.OxygenLoaderServer;
import austeretony.oxygen.common.api.IPersistentData;

public class TeleportationLoaderServer {

    public static void loadPersistentDataDelegated(IPersistentData persistentData) {
        TeleportationManagerServer.instance().getIOThread().addTask(()->OxygenLoaderServer.loadPersistentData(persistentData));
    }


    public static void savePersistentDataDelegated(IPersistentData persistentData) {
        TeleportationManagerServer.instance().getIOThread().addTask(()->OxygenLoaderServer.savePersistentData(persistentData));
    }
}