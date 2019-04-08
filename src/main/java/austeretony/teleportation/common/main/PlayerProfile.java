package austeretony.teleportation.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.util.StreamUtils;
import austeretony.teleportation.common.CooldownInfo;
import austeretony.teleportation.common.camps.InvitedPlayers;
import austeretony.teleportation.common.camps.SharedCamps;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerProfile {

    public UUID playerUUID;

    private final Map<Long, WorldPoint> ownedCamps = new ConcurrentHashMap<Long, WorldPoint>();

    private final Map<Long, UUID> otherCamps = new ConcurrentHashMap<Long, UUID>();

    private final Map<UUID, SharedCamps> sharedCamps = new ConcurrentHashMap<UUID, SharedCamps>();

    private final Map<Long, InvitedPlayers> invitedPlayers = new ConcurrentHashMap<Long, InvitedPlayers>();

    private long favoriteCampId;

    private EnumJumpProfile jumpProfile;

    private boolean syncing;

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
        return this.ownedCamps.containsKey(pointId);
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
        //TODO mind shared camps
        if (this.invitedPlayers.containsKey(pointId)) {
            InvitedPlayers invitedPlayers = this.invitedPlayers.get(pointId);
            for (UUID playerUUID : invitedPlayers.getPlayers())
                this.uninviteFromCamp(pointId, playerUUID);
        }
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

    public boolean isOtherCamp(long pointId) {
        return this.otherCamps.containsKey(pointId);
    }

    public int getOtherCampsAmount() {
        return this.otherCamps.size();
    }

    public Set<Long> getOtherCampIds() {
        return this.otherCamps.keySet();
    }

    public UUID getOtherCampOwner(long pointId) {
        return this.otherCamps.get(pointId);
    }

    public void addOtherCamp(long pointId, UUID ownerUUID) {
        this.otherCamps.put(pointId, ownerUUID);
    }

    public void removeOtherCamp(long pointId) {
        this.otherCamps.remove(pointId);
    }

    public boolean haveInvitedPlayers(long pointId) {
        return this.invitedPlayers.containsKey(pointId);
    }

    public int getInvitedPlayersAmount(long pointId) {
        return this.invitedPlayers.get(pointId).getPlayers().size();
    }

    public Map<UUID, SharedCamps> getSharedCamps() {
        return this.sharedCamps;
    }

    public Map<Long, InvitedPlayers> getInvitedPlayers() {
        return this.invitedPlayers;
    }

    public void inviteToCamp(long pointId, UUID playerUUID, String username) {
        if (this.sharedCamps.containsKey(playerUUID))
            this.sharedCamps.get(playerUUID).getCamps().add(pointId);
        else {
            SharedCamps sharedCamps = new SharedCamps(playerUUID, username);
            sharedCamps.getCamps().add(pointId);
            this.sharedCamps.put(playerUUID, sharedCamps);
        }        
        if (this.invitedPlayers.containsKey(pointId))
            this.invitedPlayers.get(pointId).getPlayers().add(playerUUID);
        else {
            InvitedPlayers invitedPlayers = new InvitedPlayers();
            invitedPlayers.getPlayers().add(playerUUID);
            this.invitedPlayers.put(pointId, invitedPlayers);
        }
    }

    public void uninviteFromCamp(long pointId, UUID playerUUID) {
        if (this.sharedCamps.containsKey(playerUUID)) {
            this.sharedCamps.get(playerUUID).getCamps().remove(pointId);
            if (this.sharedCamps.get(playerUUID).getCamps().isEmpty())
                this.sharedCamps.remove(playerUUID);
        }
        if (this.invitedPlayers.containsKey(pointId)) {
            this.invitedPlayers.get(pointId).getPlayers().remove(playerUUID);
            if (this.invitedPlayers.get(pointId).getPlayers().isEmpty())
                this.invitedPlayers.remove(pointId);
        }
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
        for (SharedCamps sharedCamps : this.sharedCamps.values())
            sharedCamps.write(bos);
        this.cooldownInfo.write(bos);
    }

    public void read(BufferedInputStream bis) throws IOException {
        this.playerUUID = new UUID(StreamUtils.readLong(bis), StreamUtils.readLong(bis));
        this.setFavoriteCampId(StreamUtils.readLong(bis));
        this.setJumpProfile(EnumJumpProfile.values()[StreamUtils.readByte(bis)]);
        int camps = StreamUtils.readInt(bis);
        for (int i = 0; i < camps; i++)
            this.addCamp(WorldPoint.read(bis));  
        int otherCamps = StreamUtils.readInt(bis);
        for (int i = 0; i < otherCamps; i++)
            this.addOtherCamp(StreamUtils.readLong(bis), new UUID(StreamUtils.readLong(bis), StreamUtils.readLong(bis)));
        int sharedrCamps = StreamUtils.readInt(bis);
        SharedCamps sharedCamps;
        for (int i = 0; i < sharedrCamps; i++) {
            sharedCamps = SharedCamps.read(bis);
            this.getSharedCamps().put(sharedCamps.playerUUID, sharedCamps);
            for (long id : sharedCamps.getCamps()) {
                if (this.invitedPlayers.containsKey(id))
                    this.invitedPlayers.get(id).getPlayers().add(sharedCamps.playerUUID);
                else {
                    InvitedPlayers invitedPlayers = new InvitedPlayers();
                    invitedPlayers.getPlayers().add(sharedCamps.playerUUID);
                    this.invitedPlayers.put(id, invitedPlayers);
                }
            }
        }
        this.cooldownInfo.read(bis);     
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
