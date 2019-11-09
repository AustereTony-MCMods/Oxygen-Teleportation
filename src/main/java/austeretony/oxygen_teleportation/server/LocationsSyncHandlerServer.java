package austeretony.oxygen_teleportation.server;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class LocationsSyncHandlerServer implements DataSyncHandlerServer<WorldPoint> {

    @Override
    public int getDataId() {
        return TeleportationMain.LOCATIONS_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return true;
    }

    @Override
    public Set<Long> getIds(UUID playerUUID) {
        return TeleportationManagerServer.instance().getLocationsContainer().getLocationIds();
    }

    @Override
    public WorldPoint getEntry(UUID playerUUID, long entryId) {
        TeleportationManagerServer.instance().getImagesManager().downloadLocationPreviewToClientAsync(CommonReference.playerByUUID(playerUUID), entryId);
        return TeleportationManagerServer.instance().getLocationsContainer().getLocation(entryId);
    }
}
