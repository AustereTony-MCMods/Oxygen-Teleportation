package austeretony.oxygen_teleportation.client;

import java.util.Set;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class CampsSyncHandlerClient implements DataSyncHandlerClient<WorldPoint> {

    @Override
    public int getDataId() {
        return TeleportationMain.CAMPS_DATA_ID;
    }

    @Override
    public Class<WorldPoint> getDataContainerClass() {
        return WorldPoint.class;
    }

    @Override
    public Set<Long> getIds() {
        return TeleportationManagerClient.instance().getPlayerData().getCampIds();
    }

    @Override
    public void clearData() {
        TeleportationManagerClient.instance().getPlayerData().reset();
    }

    @Override
    public WorldPoint getEntry(long entryId) {
        return TeleportationManagerClient.instance().getPlayerData().getCamp(entryId);
    }

    @Override
    public void addEntry(WorldPoint entry) {
        TeleportationManagerClient.instance().getPlayerData().addCamp(entry);
    }

    @Override
    public void save() {
        TeleportationManagerClient.instance().getPlayerData().setChanged(true);
    }

    @Override
    public DataSyncListener getSyncListener() {
        return (updated)->TeleportationManagerClient.instance().getTeleportationMenuManager().campsSynchronized();
    }
}
