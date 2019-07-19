package austeretony.oxygen_teleportation.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.IPersistentData;
import austeretony.oxygen.util.StreamUtils;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;

public class TeleportationPlayerData implements IPersistentData {

    private UUID playerUUID;

    private final Map<Long, WorldPoint> camps = new ConcurrentHashMap<Long, WorldPoint>();

    private long favoriteCampId;

    private volatile int owned;//used by client only for GUI purposes

    private EnumJumpProfile jumpProfile;

    private final CooldownInfo cooldownInfo = new CooldownInfo();

    public final String dataPath;

    public TeleportationPlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.jumpProfile = EnumJumpProfile.values()[TeleportationConfig.DEFAULT_JUMP_PROFILE.getIntValue()];
        this.dataPath = "players/" + this.playerUUID + "/teleportation/profile.dat";
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public Collection<WorldPoint> getCamps() {
        return this.camps.values();
    }

    public int getCampsAmount() {
        return this.camps.size();
    }

    public int getOwnedCampsAmount() {
        return this.owned;
    }

    public void resetOwnedAmount() {
        this.owned = 0;
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
        if (worldPoint.isOwner(this.playerUUID))
            this.owned++;
    }

    public void removeCamp(long pointId) {
        WorldPoint worldPoint = this.camps.remove(pointId);
        if (this.favoriteCampId == pointId)
            this.favoriteCampId = 0;
        if (worldPoint != null && worldPoint.isOwner(this.playerUUID))
            this.owned--;
    }

    public boolean isFavoriteCampExist() {
        return this.favoriteCampId != 0;
    }

    public long getFavoriteCampId() {
        return this.favoriteCampId;
    }

    public void setFavoriteCampId(long pointId) {
        this.favoriteCampId = pointId;
    }

    public EnumJumpProfile getJumpProfile() {
        return this.jumpProfile;
    }

    public void setJumpProfile(EnumJumpProfile jumpProfile) {
        this.jumpProfile = jumpProfile;
    } 

    public CooldownInfo getCooldownInfo() {
        return this.cooldownInfo;
    }

    @Override
    public String getName() {
        return "player_data";
    }

    @Override
    public String getModId() {
        return TeleportationMain.MODID;
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
        this.cooldownInfo.write(bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.playerUUID = StreamUtils.readUUID(bis);
        this.setFavoriteCampId(StreamUtils.readLong(bis));
        this.setJumpProfile(EnumJumpProfile.values()[StreamUtils.readByte(bis)]);
        int amount = StreamUtils.readShort(bis);
        for (int i = 0; i < amount; i++)
            this.addCamp(WorldPoint.read(bis)); 
        this.cooldownInfo.read(bis);     
    }

    public void reset() {
        this.camps.clear();
        this.owned = 0;
        this.cooldownInfo.reset();
    }

    public enum EnumJumpProfile {

        FREE,
        REQUEST,
        DISABLED;     

        public String localizedName() {
            return I18n.format("teleportation.jumpProfile." + this.toString().toLowerCase());
        }
    }
}
