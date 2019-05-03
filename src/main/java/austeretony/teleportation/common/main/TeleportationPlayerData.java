package austeretony.teleportation.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.util.StreamUtils;
import austeretony.teleportation.common.CooldownInfo;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;

public class TeleportationPlayerData {

    private UUID playerUUID;

    private final Map<Long, WorldPoint> ownedCamps = new ConcurrentHashMap<Long, WorldPoint>();

    //TODO WIP. Insure all changes are synced with camp owner.
    private final Map<Long, UUID> otherCamps = new ConcurrentHashMap<Long, UUID>();

    //TODO WIP. Insure all changes are synced for invited players.
    private final Map<UUID, SharedCamps> sharedCamps = new ConcurrentHashMap<UUID, SharedCamps>();

    private final Map<Long, InvitedPlayers> invitedPlayers = new ConcurrentHashMap<Long, InvitedPlayers>();

    private long favoriteCampId;

    private EnumJumpProfile jumpProfile;

    private final CooldownInfo cooldownInfo = new CooldownInfo();

    public TeleportationPlayerData() {
        this.jumpProfile = EnumJumpProfile.values()[TeleportationConfig.DEFAULT_JUMP_PROFILE.getIntValue()];
    }

    public TeleportationPlayerData(UUID playerUUID) {
        this();
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public Collection<WorldPoint> getCamps() {
        return this.ownedCamps.values();
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

    public void clearSharedCamps() {
        this.sharedCamps.clear();;
    }

    public int getSharedCampsAmount() {
        return this.sharedCamps.size();
    }

    public Collection<SharedCamps> getSharedCamps() {
        return this.sharedCamps.values();
    }

    public SharedCamps getSharedCampsByPlayerUUID(UUID playerUUID) {
        return this.sharedCamps.get(playerUUID);
    }

    public void clearInvitedPlayers() {
        this.invitedPlayers.clear();;
    }

    public InvitedPlayers getInvitedPlayersByCampId(long pointId) {
        return this.invitedPlayers.get(pointId);
    }

    public void inviteToCamp(long pointId, UUID playerUUID) {
        if (this.sharedCamps.containsKey(playerUUID))
            this.sharedCamps.get(playerUUID).getCamps().add(pointId);
        else {
            SharedCamps sharedCamps = new SharedCamps(playerUUID);
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
        return this.jumpProfile;
    }

    public void setJumpProfile(EnumJumpProfile jumpProfile) {
        this.jumpProfile = jumpProfile;
    } 

    public CooldownInfo getCooldownInfo() {
        return this.cooldownInfo;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.playerUUID, bos);
        StreamUtils.write(this.getFavoriteCampId(), bos);
        StreamUtils.write((byte) this.getJumpProfile().ordinal(), bos);
        StreamUtils.write((short) this.getCampsAmount(), bos);
        for (WorldPoint worldPoint : this.getCamps())
            worldPoint.write(bos);
        StreamUtils.write((short) this.otherCamps.size(), bos);
        for (Map.Entry<Long, UUID> entry : this.otherCamps.entrySet()) {
            StreamUtils.write(entry.getKey(), bos);
            StreamUtils.write(entry.getValue(), bos);
        }
        StreamUtils.write((short) this.sharedCamps.size(), bos);
        for (SharedCamps sharedCamps : this.sharedCamps.values())
            sharedCamps.write(bos);
        this.cooldownInfo.write(bos);
    }

    public void read(BufferedInputStream bis) throws IOException {
        this.playerUUID = StreamUtils.readUUID(bis);
        this.setFavoriteCampId(StreamUtils.readLong(bis));
        this.setJumpProfile(EnumJumpProfile.values()[StreamUtils.readByte(bis)]);
        int i = 0;
        int campsAmount = StreamUtils.readShort(bis);
        for (; i < campsAmount; i++)
            this.addCamp(WorldPoint.read(bis));  
        int otherCampsAmount = StreamUtils.readShort(bis);
        for (i = 0; i < otherCampsAmount; i++)
            this.addOtherCamp(StreamUtils.readLong(bis), StreamUtils.readUUID(bis));
        int sharedrCampsAmount = StreamUtils.readShort(bis);
        SharedCamps sharedCamps;
        for (i = 0; i < sharedrCampsAmount; i++) {
            sharedCamps = SharedCamps.read(bis);
            for (long pointId : sharedCamps.getCamps())
                this.inviteToCamp(pointId, sharedCamps.playerUUID);
        }
        this.cooldownInfo.read(bis);     
    }

    public void resetData() {
        this.ownedCamps.clear();
        this.otherCamps.clear();
        this.sharedCamps.clear();
        this.invitedPlayers.clear();
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
