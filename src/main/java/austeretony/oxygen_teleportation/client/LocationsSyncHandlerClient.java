package austeretony.oxygen_teleportation.client;

import java.util.Set;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class LocationsSyncHandlerClient implements DataSyncHandlerClient<WorldPoint> {

    @Override
    public int getDataId() {
        return TeleportationMain.LOCATIONS_DATA_ID;
    }

    @Override
    public Class<WorldPoint> getDataContainerClass() {
        return WorldPoint.class;
    }

    @Override
    public Set<Long> getIds() {
        return TeleportationManagerClient.instance().getLocationsContainer().getLocationIds();
    }

    @Override
    public void clearData() {
        TeleportationManagerClient.instance().getLocationsContainer().reset();
    }

    @Override
    public WorldPoint getEntry(long entryId) {
        return TeleportationManagerClient.instance().getLocationsContainer().getLocation(entryId);
    }

    @Override
    public void addEntry(WorldPoint entry) {
        TeleportationManagerClient.instance().getLocationsContainer().addLocation(entry);
    }

    @Override
    public void save() {
        TeleportationManagerClient.instance().getLocationsContainer().setChanged(true);
    }

    @Override
    public DataSyncListener getSyncListener() {
        return (updated)->TeleportationManagerClient.instance().getTeleportationMenuManager().locationsSynchronized();
    }
}
