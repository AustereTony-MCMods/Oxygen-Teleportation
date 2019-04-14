package austeretony.teleportation.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.util.StreamUtils;
import austeretony.teleportation.common.world.WorldPoint;

public class TeleportationWorldData {

    private final Map<Long, WorldPoint> locations = new ConcurrentHashMap<Long, WorldPoint>();

    public Collection<WorldPoint> getLocations() {
        return this.locations.values();
    }

    public int getLocationsAmount() {
        return this.locations.size();
    }

    public boolean isLocationsExist() {
        return !this.locations.isEmpty();
    }

    public Set<Long> getLocationIds() {
        return this.locations.keySet();
    }

    public boolean locationExist(long pointId) {
        return this.locations.containsKey(pointId);
    }

    public WorldPoint getLocation(long pointId) {
        return this.locations.get(pointId);
    }

    public void addLocation(WorldPoint worldPoint) {
        this.locations.put(worldPoint.getId(), worldPoint);
    }

    public void removeLocation(long pointId) {
        this.locations.remove(pointId);
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write((short) this.getLocationsAmount(), bos);
        for (WorldPoint worldPoint : this.getLocations())
            worldPoint.write(bos);
    }

    public void read(BufferedInputStream bis) throws IOException {
        int locations = StreamUtils.readShort(bis);
        for (int i = 0; i < locations; i++)
            this.addLocation(WorldPoint.read(bis));    
    }
}
