package austeretony.teleportation.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.util.StreamUtils;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.menu.camps.CampsList;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerProfile {

    public final UUID playerUUID;

    //Belong to this player
    private final Map<Long, WorldPoint> ownedCamps = new ConcurrentHashMap<Long, WorldPoint>();

    //TODO Part of the invitation mechanic - WIP
    //Shared WITH this player
    private final Map<Long, UUID> otherCamps = new ConcurrentHashMap<Long, UUID>();

    //TODO Part of the invitation mechanic - WIP
    //Shared BY this player
    private final Map<UUID, CampsList> sharedCamps = new ConcurrentHashMap<UUID, CampsList>();

    private long favoriteCampId;

    private EnumJumpProfile jumpProfile;

    private volatile boolean syncing;

    private final CooldownInfo cooldownInfo = new CooldownInfo();

    public PlayerProfile(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public boolean isSyncing() {
        return this.syncing;
    }

    public void setSyncing(boolean value) {
        this.syncing = value;
    }

    public Map<Long, WorldPoint> getCamps() {
        return this.ownedCamps;
    }

    public int getCampsAmount() {
        return this.ownedCamps.size();
    }

    public boolean isCampsExist() {
        return !this.ownedCamps.isEmpty();
    }

    public Set<Long> getCampIds() {
        return this.ownedCamps.keySet();
    }

    public boolean campExist(long pointId) {
        return this.ownedCamps.containsKey(pointId) || this.otherCamps.containsKey(pointId);
    }

    public WorldPoint getCamp(long pointId) {
        return this.ownedCamps.get(pointId);
    }

    public void addCamp(WorldPoint worldPoint) {
        this.ownedCamps.put(worldPoint.getId(), worldPoint);
    }

    public void removeCamp(long pointId) {
        this.ownedCamps.remove(pointId);
        if (this.favoriteCampId == pointId)
            this.favoriteCampId = 0;
        for (CampsList list : this.sharedCamps.values())
            if (list.getCamps().contains(pointId))
                list.getCamps().remove(pointId);
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

    public Map<Long, UUID> getOtherCamps() {
        return this.otherCamps;
    }

    public int getOtherCampsAmount() {
        return this.otherCamps.size();
    }

    public Set<Long> getOtherCampIds() {
        return this.otherCamps.keySet();
    }

    public UUID getOtherCampOwner(long id) {
        return this.otherCamps.get(id);
    }

    public void addOtherCamp(Long id, UUID ownerUUID) {
        this.otherCamps.put(id, ownerUUID);
    }

    public void removeOtherCamp(Long id) {
        this.otherCamps.remove(id);
    }

    public Map<UUID, CampsList> getSharedCamps() {
        return this.sharedCamps;
    }

    public void shareCamp(long id, UUID playerUUID) {
        if (this.sharedCamps.containsKey(playerUUID))
            this.sharedCamps.get(playerUUID).getCamps().add(id);
        else {
            CampsList list = new CampsList();
            list.getCamps().add(id);
            this.sharedCamps.put(playerUUID, list);
        }
    }

    public void unshareCamp(long id, UUID playerUUID) {
        if (this.sharedCamps.containsKey(playerUUID))
            this.sharedCamps.get(playerUUID).getCamps().remove(id);
    }

    public EnumJumpProfile getJumpProfile() {
        return this.jumpProfile != null ? this.jumpProfile : EnumJumpProfile.values()[TeleportationConfig.DEFAULT_JUMP_PROFILE.getIntValue()];
    }

    public void setJumpProfile(EnumJumpProfile jumpProfile) {
        this.jumpProfile = jumpProfile;
    } 

    public CooldownInfo getCooldownInfo() {
        return this.cooldownInfo;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.playerUUID.getMostSignificantBits(), bos);
        StreamUtils.write(this.playerUUID.getLeastSignificantBits(), bos);
        StreamUtils.write(this.getFavoriteCampId(), bos);
        StreamUtils.write((byte) this.getJumpProfile().ordinal(), bos);
        StreamUtils.write(this.getCampsAmount(), bos);
        for (WorldPoint worldPoint : this.getCamps().values())
            worldPoint.write(bos);
        StreamUtils.write(this.otherCamps.size(), bos);
        for (Map.Entry<Long, UUID> entry : this.otherCamps.entrySet()) {
            StreamUtils.write(entry.getKey(), bos);
            StreamUtils.write(entry.getValue().getMostSignificantBits(), bos);
            StreamUtils.write(entry.getValue().getLeastSignificantBits(), bos);
        }
        StreamUtils.write(this.sharedCamps.size(), bos);
        for (Map.Entry<UUID, CampsList> entry : this.sharedCamps.entrySet()) {
            StreamUtils.write(entry.getKey().getMostSignificantBits(), bos);
            StreamUtils.write(entry.getKey().getLeastSignificantBits(), bos);
            entry.getValue().write(bos);
        }
        this.cooldownInfo.write(bos);
    }

    public static PlayerProfile read(BufferedInputStream bis) throws IOException {
        PlayerProfile playerProfile = new PlayerProfile(new UUID(StreamUtils.readLong(bis), StreamUtils.readLong(bis)));
        playerProfile.setFavoriteCampId(StreamUtils.readLong(bis));
        playerProfile.setJumpProfile(EnumJumpProfile.values()[StreamUtils.readByte(bis)]);
        int camps = StreamUtils.readInt(bis);
        for (int i = 0; i < camps; i++)
            playerProfile.addCamp(WorldPoint.read(bis));  
        int otherCamps = StreamUtils.readInt(bis);
        for (int i = 0; i < otherCamps; i++)
            playerProfile.addOtherCamp(StreamUtils.readLong(bis), new UUID(StreamUtils.readLong(bis), StreamUtils.readLong(bis)));
        int sharedrCamps = StreamUtils.readInt(bis);
        for (int i = 0; i < sharedrCamps; i++)
            playerProfile.getSharedCamps().put(new UUID(StreamUtils.readLong(bis), StreamUtils.readLong(bis)), CampsList.read(bis));
        playerProfile.cooldownInfo.read(bis);
        return playerProfile;       
    }

    public enum EnumJumpProfile {

        FREE("teleportation.jumpProfile.free"),
        REQUEST("teleportation.jumpProfile.request"),
        DISABLED("teleportation.jumpProfile.disabled");

        public final String key;

        EnumJumpProfile(String key) {
            this.key = key;
        }       

        @SideOnly(Side.CLIENT)
        public String getLocalizedName() {
            return I18n.format(this.key);
        }
    }
}
