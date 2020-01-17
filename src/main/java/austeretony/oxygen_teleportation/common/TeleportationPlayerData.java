package austeretony.oxygen_teleportation.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;

public class TeleportationPlayerData extends AbstractPersistentData {

    private UUID playerUUID;

    private final Map<Long, WorldPoint> camps = new ConcurrentHashMap<>(5);

    private long favoriteCamp;

    private EnumJumpProfile jumpProfile;

    private final CooldownData cooldown = new CooldownData();

    private String dataPath;

    public TeleportationPlayerData() {
        this.jumpProfile = EnumJumpProfile.values()[TeleportationConfig.DEFAULT_PLAYER_TELEPORTATION_PROFILE.asInt()];
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.cooldown.setPlayerUUID(playerUUID);
    }

    public void setPath(String path) {
        this.dataPath = path;
    }

    public Collection<WorldPoint> getCamps() {
        return this.camps.values();
    }

    public int getCampsAmount() {
        return this.camps.size();
    }

    public boolean isCampsExist() {
        return !this.camps.isEmpty();
    }

    public Set<Long> getCampIds() {
        return this.camps.keySet();
    }

    public boolean campExist(long pointId) {
        return this.camps.containsKey(pointId);
    }

    public WorldPoint getCamp(long pointId) {
        return this.camps.get(pointId);
    }

    public void addCamp(WorldPoint worldPoint) {
        this.camps.put(worldPoint.getId(), worldPoint);
    }

    public void removeCamp(long pointId) {
        this.camps.remove(pointId);
        if (this.favoriteCamp == pointId)
            this.favoriteCamp = 0L;
    }

    public boolean isFavoriteCampExist() {
        return this.favoriteCamp != 0L;
    }

    public long getFavoriteCampId() {
        return this.favoriteCamp;
    }

    public WorldPoint getFavoriteCamp() {
        return this.camps.get(this.favoriteCamp);
    }

    public void setFavoriteCampId(long pointId) {
        this.favoriteCamp = pointId;
    }

    public int getOwnedCampsAmount() {
        int amount = 0;
        for (WorldPoint camp : this.camps.values())
            if (camp.isOwner(this.playerUUID))
                amount++;
        return amount;
    }

    public long createId(long seed) {
        seed++;
        while (this.camps.containsKey(seed))
            seed++;
        return seed;
    }

    public EnumJumpProfile getJumpProfile() {
        return this.jumpProfile;
    }

    public void setJumpProfile(EnumJumpProfile jumpProfile) {
        this.jumpProfile = jumpProfile;
    } 

    public CooldownData getCooldownData() {
        return this.cooldown;
    }

    @Override
    public String getDisplayName() {
        return "player_data";
    }

    @Override
    public String getPath() {
        return this.dataPath;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.playerUUID, bos);
        StreamUtils.write(this.getFavoriteCampId(), bos);
        StreamUtils.write((byte) this.getJumpProfile().ordinal(), bos);
        StreamUtils.write((short) this.getCampsAmount(), bos);
        for (WorldPoint worldPoint : this.getCamps())
            worldPoint.write(bos);
        this.cooldown.write(bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.playerUUID = StreamUtils.readUUID(bis);
        this.setFavoriteCampId(StreamUtils.readLong(bis));
        this.setJumpProfile(EnumJumpProfile.values()[StreamUtils.readByte(bis)]);
        int size = StreamUtils.readShort(bis);
        for (int i = 0; i < size; i++)
            this.addCamp(WorldPoint.read(bis)); 
        this.cooldown.read(bis);     
    }

    @Override
    public void reset() {
        this.camps.clear();
    }

    public enum EnumJumpProfile {

        FREE,
        REQUEST,
        DISABLED;     

        public String localizedName() {
            return ClientReference.localize("oxygen_teleportation.jumpProfile." + this.toString().toLowerCase());
        }
    }
}
