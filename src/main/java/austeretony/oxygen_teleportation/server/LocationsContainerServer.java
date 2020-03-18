package austeretony.oxygen_teleportation.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.WorldPoint;

public class LocationsContainerServer extends AbstractPersistentData {

    private final Map<Long, WorldPoint> locations = new ConcurrentHashMap<>();

    protected LocationsContainerServer() {}

    public Collection<WorldPoint> getLocations() {
        return this.locations.values();
    }

    public int getLocationsAmount() {
        return this.locations.size();
    }

    public Set<Long> getLocationIds() {
        return this.locations.keySet();
    }

    public boolean locationExist(long pointId) {
        return this.locations.containsKey(pointId);
    }

    @Nullable
    public WorldPoint getLocation(long pointId) {
        return this.locations.get(pointId);
    }

    public void addLocation(WorldPoint worldPoint) {
        this.locations.put(worldPoint.getId(), worldPoint);
    }

    public void removeLocation(long pointId) {
        this.locations.remove(pointId);
    }

    public long createId(long seed) {
        long id = ++seed;
        while (this.locations.containsKey(id))
            id++;
        return id;
    }

    @Override
    public String getDisplayName() {
        return "teleportation:locations_server";
    }

    @Override
    public String getPath() {
        return OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write((short) this.getLocationsAmount(), bos);
        for (WorldPoint worldPoint : this.getLocations())
            worldPoint.write(bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        int size = StreamUtils.readShort(bis);
        for (int i = 0; i < size; i++)
            this.addLocation(WorldPoint.read(bis));    
    }

    @Override
    public void reset() {
        this.locations.clear();
    }
}
