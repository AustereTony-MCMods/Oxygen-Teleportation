package austeretony.oxygen_teleportation.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.IPersistentData;
import austeretony.oxygen.util.ConcurrentSetWrapper;
import austeretony.oxygen.util.PacketBufferUtils;
import austeretony.oxygen.util.StreamUtils;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.WorldPoint;
import net.minecraft.network.PacketBuffer;

public class SharedCampsManagerServer implements IPersistentData {

    private final TeleportationManagerServer manager;

    private final Map<Long, WorldPoint> cachedCamps = new ConcurrentHashMap<Long, WorldPoint>();

    private final Map<Long, UUID> ownersAccess = new ConcurrentHashMap<Long, UUID>();

    private final Map<UUID, ConcurrentSetWrapper<Long>> forInvited = new ConcurrentHashMap<UUID, ConcurrentSetWrapper<Long>>();

    private final Map<UUID, InvitationsContainerServer> forOwners = new ConcurrentHashMap<UUID, InvitationsContainerServer>();

    public SharedCampsManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public UUID getCampOwner(long pointId) {
        return this.ownersAccess.get(pointId);
    }

    public boolean campExist(long pointId) {
        return this.cachedCamps.containsKey(pointId);
    }

    public WorldPoint getCamp(long pointId) {
        return this.cachedCamps.get(pointId);
    }

    public void removeCamp(UUID playerUUID, long pointId) {
        this.cachedCamps.remove(pointId);

        this.ownersAccess.remove(pointId);

        InvitationsContainerServer invitations = this.forOwners.get(playerUUID);
        for (UUID invitedUUID : invitations.access.get(pointId).set) {
            invitations.invitedPlayers.get(invitedUUID).remove(pointId);
            invitations.access.remove(pointId);

            this.forInvited.get(invitedUUID).remove(pointId);
        }
    }

    public void replaceCamp(UUID playerUUID, long oldPointId, WorldPoint worldPoint) {
        this.cachedCamps.remove(oldPointId);
        this.cachedCamps.put(worldPoint.getId(), worldPoint);

        this.ownersAccess.remove(oldPointId);
        this.ownersAccess.put(worldPoint.getId(), playerUUID);

        InvitationsContainerServer invitations = this.forOwners.get(playerUUID);
        invitations.updateId();

        ConcurrentSetWrapper<Long> campsOwner, campsInvited;
        ConcurrentSetWrapper<UUID> players = invitations.access.get(oldPointId);
        for (UUID invitedUUID : players.set) {
            campsOwner = invitations.invitedPlayers.get(invitedUUID);
            campsOwner.remove(oldPointId);
            campsOwner.add(worldPoint.getId());      

            campsInvited = this.forInvited.get(invitedUUID);
            campsInvited.remove(oldPointId);
            campsInvited.add(worldPoint.getId());
        }

        invitations.access.put(worldPoint.getId(), players);
        invitations.access.remove(oldPointId);
    }

    public boolean haveInvitations(UUID playerUUID) {
        return this.forInvited.containsKey(playerUUID)
                && !this.forInvited.get(playerUUID).isEmpty();
    }

    public boolean haveInvitation(UUID playerUUID, long pointId) {
        return this.forInvited.containsKey(playerUUID) 
                && this.forInvited.get(playerUUID).contains(pointId);
    }

    public int getInvitationsAmount(UUID playerUUID) {
        if (!this.forInvited.containsKey(playerUUID))
            return 0;
        return this.forInvited.get(playerUUID).size();
    }

    public Set<Long> getInvitations(UUID playerUUID) {
        return this.forInvited.get(playerUUID).set;
    }

    public boolean haveInvitedPlayers(UUID playerUUID) {
        return this.forOwners.containsKey(playerUUID) 
                && !this.forOwners.get(playerUUID).invitedPlayers.isEmpty();
    }

    public boolean haveInvitedPlayers(UUID playerUUID, long pointId) {
        return this.forOwners.containsKey(playerUUID) 
                && this.forOwners.get(playerUUID).access.containsKey(pointId);
    }

    public int getInvitedPlayersAmountForCamp(UUID playerUUID, long pointId) {
        if (!this.forOwners.containsKey(playerUUID))
            return 0;
        InvitationsContainerServer invitations = this.forOwners.get(playerUUID);
        if (!invitations.access.containsKey(pointId))
            return 0;
        return invitations.access.get(pointId).size();
    }

    public InvitationsContainerServer getInvitationsContainer(UUID playerUUID) {
        return this.forOwners.get(playerUUID);
    }

    public void invite(UUID ownerUUID, long pointId, UUID invitedUUID) {
        if (!this.campExist(pointId))
            this.cachedCamps.put(pointId, this.manager.getPlayerData(ownerUUID).getCamp(pointId));

        if (!this.ownersAccess.containsKey(pointId))
            this.ownersAccess.put(pointId, ownerUUID);

        ConcurrentSetWrapper<Long> camps;
        if (!this.forInvited.containsKey(invitedUUID)) {
            camps = new ConcurrentSetWrapper<Long>();
            camps.add(pointId);
            this.forInvited.put(invitedUUID, camps);
        } else
            this.forInvited.get(invitedUUID).add(pointId);

        InvitationsContainerServer invitations;
        ConcurrentSetWrapper<UUID> players;
        if (!this.forOwners.containsKey(ownerUUID)) {
            invitations = new InvitationsContainerServer();
            invitations.updateId();
            camps = new ConcurrentSetWrapper<Long>();
            camps.add(pointId);
            invitations.invitedPlayers.put(invitedUUID, camps);

            players = new ConcurrentSetWrapper<UUID>();
            players.add(invitedUUID);
            invitations.access.put(pointId, players);

            this.forOwners.put(ownerUUID, invitations);
        } else {
            invitations = this.forOwners.get(ownerUUID);
            invitations.updateId();

            if (!invitations.invitedPlayers.containsKey(invitedUUID)) {
                camps = new ConcurrentSetWrapper<Long>();
                camps.add(pointId);
                invitations.invitedPlayers.put(invitedUUID, camps);
            } else {
                invitations.invitedPlayers.get(invitedUUID).add(pointId);
            }

            if (!invitations.access.containsKey(pointId)) {
                players = new ConcurrentSetWrapper<UUID>();
                players.add(invitedUUID);
                invitations.access.put(pointId, players);
            } else {
                invitations.access.get(pointId).add(invitedUUID);
            }
        }
    }

    public void uninvite(UUID ownerUUID, long pointId, UUID invitedUUID) {
        this.forInvited.get(invitedUUID).remove(pointId);

        InvitationsContainerServer invitations = this.forOwners.get(ownerUUID);
        invitations.updateId();
        invitations.invitedPlayers.get(invitedUUID).remove(pointId);
        invitations.access.get(pointId).remove(invitedUUID);
    }

    @Override
    public String getName() {
        return "shared_camps";
    }

    @Override
    public String getModId() {
        return TeleportationMain.MODID;
    }

    @Override
    public String getPath() {
        return "world/teleportation/shared_camps.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.cachedCamps.size(), bos);
        for (WorldPoint worldPoint : this.cachedCamps.values())
            worldPoint.write(bos);

        StreamUtils.write(this.forInvited.size(), bos);
        for (Map.Entry<UUID, ConcurrentSetWrapper<Long>> entry : this.forInvited.entrySet()) {
            StreamUtils.write(entry.getKey(), bos);
            StreamUtils.write((short) entry.getValue().size(), bos);
            for (long pointId : entry.getValue().set)
                StreamUtils.write(pointId, bos);
        }

        StreamUtils.write(this.forOwners.size(), bos);
        for (Map.Entry<UUID, InvitationsContainerServer> outerEntry : this.forOwners.entrySet()) {
            StreamUtils.write(outerEntry.getKey(), bos);
            StreamUtils.write(outerEntry.getValue().getId(), bos);
            StreamUtils.write((short) outerEntry.getValue().invitedPlayers.size(), bos);
            for (Map.Entry<UUID, ConcurrentSetWrapper<Long>> innerEntry : outerEntry.getValue().invitedPlayers.entrySet()) {
                StreamUtils.write(innerEntry.getKey(), bos);
                StreamUtils.write((short) innerEntry.getValue().size(), bos);
                for (long pointId : innerEntry.getValue().set)
                    StreamUtils.write(pointId, bos);
            }
        }
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException { 
        int 
        amountOuter = StreamUtils.readInt(bis),
        i = 0;
        WorldPoint worldPoint;
        for (; i < amountOuter; i++) {
            worldPoint = WorldPoint.read(bis);
            this.cachedCamps.put(worldPoint.getId(), worldPoint);
        }

        amountOuter = StreamUtils.readInt(bis);

        int j, amountInner;
        UUID playerUUID;

        ConcurrentSetWrapper<Long> camps;
        for (i = 0; i < amountOuter; i++) {
            playerUUID = StreamUtils.readUUID(bis);
            amountInner = StreamUtils.readShort(bis);
            camps = new ConcurrentSetWrapper<Long>();
            for (j = 0; j < amountInner; j++)
                camps.add(StreamUtils.readLong(bis));
            this.forInvited.put(playerUUID, camps);
        }

        amountOuter = StreamUtils.readInt(bis);

        int amountCamps, k;
        UUID invitedUUID;
        InvitationsContainerServer invitations;
        long pointId;
        for (i = 0; i < amountOuter; i++) {
            invitations = new InvitationsContainerServer();
            playerUUID = StreamUtils.readUUID(bis);
            invitations.setId(StreamUtils.readLong(bis));
            amountInner = StreamUtils.readShort(bis);
            for (j = 0; j < amountInner; j++) {
                invitedUUID = StreamUtils.readUUID(bis);
                amountCamps = StreamUtils.readShort(bis);
                camps = new ConcurrentSetWrapper<Long>();
                for (k = 0; k < amountCamps; k++) {
                    pointId = StreamUtils.readLong(bis);
                    camps.add(pointId);
                    this.ownersAccess.put(pointId, playerUUID);
                }
                invitations.invitedPlayers.put(invitedUUID, camps);
            }
            this.forOwners.put(playerUUID, invitations);
        }

        ConcurrentSetWrapper<UUID> players;
        for (InvitationsContainerServer invitation : this.forOwners.values()) {
            for (Map.Entry<UUID, ConcurrentSetWrapper<Long>> entry : invitation.invitedPlayers.entrySet()) {
                for (long id : entry.getValue().set) {
                    if (!invitation.access.containsKey(id)) {
                        players = new ConcurrentSetWrapper<UUID>();
                        players.add(entry.getKey());
                        invitation.access.put(id, players);
                    } else
                        invitation.access.get(id).add(entry.getKey());
                }
            }
        }
    }

    public void reset() {
        this.cachedCamps.clear();
        this.ownersAccess.clear();
        this.forInvited.clear();
        this.forOwners.clear();
    }

    public static class InvitationsContainerServer {

        private long id;

        public final Map<UUID, ConcurrentSetWrapper<Long>> invitedPlayers = new ConcurrentHashMap<UUID, ConcurrentSetWrapper<Long>>();

        public final Map<Long, ConcurrentSetWrapper<UUID>> access = new ConcurrentHashMap<Long, ConcurrentSetWrapper<UUID>>();

        public void updateId() {
            this.id = System.currentTimeMillis();
        }

        public void setId(long id) {    
            this.id = id;
        }

        public long getId() {
            return this.id;
        }

        public void write(PacketBuffer buffer) {
            buffer.writeLong(this.getId());
            buffer.writeShort(this.access.size());
            for (Map.Entry<Long, ConcurrentSetWrapper<UUID>> entry : this.access.entrySet()) {
                buffer.writeShort(entry.getValue().size());
                for (UUID playerUUID : entry.getValue().set)    
                    PacketBufferUtils.writeUUID(playerUUID, buffer);
                buffer.writeLong(entry.getKey());
            }
        }
    }
}
