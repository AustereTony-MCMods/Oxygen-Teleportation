package austeretony.oxygen_teleportation.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;

public class LocationsContainerClient extends AbstractPersistentData {

    private final Map<Long, WorldPoint> locations = new ConcurrentHashMap<>();

    protected LocationsContainerClient() {}

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

    @Override
    public String getDisplayName() {
        return "locations";
    }

    @Override
    public String getPath() {
        return OxygenHelperClient.getDataFolder() + "/client/world/teleportation/locations.dat";
    }

    @Override
    public long getSaveDelayMinutes() {
        return TeleportationConfig.LOCATIONS_SAVE_DELAY_MINUTES.getIntValue();
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
