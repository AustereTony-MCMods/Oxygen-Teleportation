package austeretony.oxygen_teleportation.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.ConcurrentSetWrapper;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.WorldPoint;

public class SharedCampsContainerServer extends AbstractPersistentData {

    private final Map<Long, WorldPoint> cache = new ConcurrentHashMap<>();

    private final Map<Long, UUID> access = new ConcurrentHashMap<>();

    private final Map<UUID, ConcurrentSetWrapper<Long>> invited = new ConcurrentHashMap<>();

    private final Map<UUID, InvitationsContainerServer> owners = new ConcurrentHashMap<>();

    protected SharedCampsContainerServer() {}

    public UUID getCampOwner(long pointId) {
        return this.access.get(pointId);
    }

    public boolean campExist(long pointId) {
        return this.cache.containsKey(pointId);
    }

    public WorldPoint getCamp(long pointId) {
        return this.cache.get(pointId);
    }

    public void removeCamp(UUID playerUUID, long pointId) {
        this.cache.remove(pointId);
        this.access.remove(pointId);

        InvitationsContainerServer invitations = this.owners.get(playerUUID);
        for (UUID invitedUUID : invitations.access.get(pointId).set) {
            invitations.invitedPlayers.get(invitedUUID).remove(pointId);
            invitations.access.remove(pointId);

            this.invited.get(invitedUUID).remove(pointId);
        }
    }

    public void replaceCamp(UUID playerUUID, long oldPointId, WorldPoint worldPoint) {
        this.cache.remove(oldPointId);
        this.cache.put(worldPoint.getId(), worldPoint);

        this.access.remove(oldPointId);
        this.access.put(worldPoint.getId(), playerUUID);

        InvitationsContainerServer invitations = this.owners.get(playerUUID);
        invitations.updateId();

        ConcurrentSetWrapper<Long> campsOwner, campsInvited;
        ConcurrentSetWrapper<UUID> players = invitations.access.get(oldPointId);
        for (UUID invitedUUID : players.set) {
            campsOwner = invitations.invitedPlayers.get(invitedUUID);
            campsOwner.remove(oldPointId);
            campsOwner.add(worldPoint.getId());      

            campsInvited = this.invited.get(invitedUUID);
            campsInvited.remove(oldPointId);
            campsInvited.add(worldPoint.getId());
        }

        invitations.access.put(worldPoint.getId(), players);
        invitations.access.remove(oldPointId);
    }

    public boolean haveInvitations(UUID playerUUID) {
        return this.invited.containsKey(playerUUID)
                && !this.invited.get(playerUUID).isEmpty();
    }

    public boolean haveInvitation(UUID playerUUID, long pointId) {
        return this.invited.containsKey(playerUUID) 
                && this.invited.get(playerUUID).contains(pointId);
    }

    public int getInvitationsAmount(UUID playerUUID) {
        if (!this.invited.containsKey(playerUUID))
            return 0;
        return this.invited.get(playerUUID).size();
    }

    public Set<Long> getInvitations(UUID playerUUID) {
        return this.invited.get(playerUUID).set;
    }

    public boolean haveInvitedPlayers(UUID playerUUID) {
        return this.owners.containsKey(playerUUID) 
                && !this.owners.get(playerUUID).invitedPlayers.isEmpty();
    }

    public boolean haveInvitedPlayers(UUID playerUUID, long pointId) {
        return this.owners.containsKey(playerUUID) 
                && this.owners.get(playerUUID).access.containsKey(pointId);
    }

    public int getInvitedPlayersAmountForCamp(UUID playerUUID, long pointId) {
        if (!this.owners.containsKey(playerUUID))
            return 0;
        InvitationsContainerServer invitations = this.owners.get(playerUUID);
        if (!invitations.access.containsKey(pointId))
            return 0;
        return invitations.access.get(pointId).size();
    }

    public InvitationsContainerServer getInvitationsContainer(UUID playerUUID) {
        return this.owners.get(playerUUID);
    }

    public void invite(UUID ownerUUID, long pointId, UUID invitedUUID) {
        if (!this.campExist(pointId))
            this.cache.put(pointId, TeleportationManagerServer.instance().getPlayersDataContainer().getPlayerData(ownerUUID).getCamp(pointId));

        if (!this.access.containsKey(pointId))
            this.access.put(pointId, ownerUUID);

        ConcurrentSetWrapper<Long> camps;
        if (!this.invited.containsKey(invitedUUID)) {
            camps = new ConcurrentSetWrapper<>();
            camps.add(pointId);
            this.invited.put(invitedUUID, camps);
        } else
            this.invited.get(invitedUUID).add(pointId);

        InvitationsContainerServer invitations;
        ConcurrentSetWrapper<UUID> players;
        if (!this.owners.containsKey(ownerUUID)) {
            invitations = new InvitationsContainerServer();
            invitations.updateId();
            camps = new ConcurrentSetWrapper<>();
            camps.add(pointId);
            invitations.invitedPlayers.put(invitedUUID, camps);

            players = new ConcurrentSetWrapper<>();
            players.add(invitedUUID);
            invitations.access.put(pointId, players);

            this.owners.put(ownerUUID, invitations);
        } else {
            invitations = this.owners.get(ownerUUID);
            invitations.updateId();

            if (!invitations.invitedPlayers.containsKey(invitedUUID)) {
                camps = new ConcurrentSetWrapper<>();
                camps.add(pointId);
                invitations.invitedPlayers.put(invitedUUID, camps);
            } else {
                invitations.invitedPlayers.get(invitedUUID).add(pointId);
            }

            if (!invitations.access.containsKey(pointId)) {
                players = new ConcurrentSetWrapper<>();
                players.add(invitedUUID);
                invitations.access.put(pointId, players);
            } else {
                invitations.access.get(pointId).add(invitedUUID);
            }
        }
    }

    public void uninvite(UUID ownerUUID, long pointId, UUID invitedUUID) {
        this.invited.get(invitedUUID).remove(pointId);

        InvitationsContainerServer invitations = this.owners.get(ownerUUID);
        invitations.updateId();
        invitations.invitedPlayers.get(invitedUUID).remove(pointId);
        invitations.access.get(pointId).remove(invitedUUID);
    }

    @Override
    public String getDisplayName() {
        return "shared_camps";
    }

    @Override
    public String getPath() {
        return OxygenHelperServer.getDataFolder() + "/server/world/teleportation/shared_camps.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.cache.size(), bos);
        for (WorldPoint worldPoint : this.cache.values())
            worldPoint.write(bos);

        StreamUtils.write(this.invited.size(), bos);
        for (Map.Entry<UUID, ConcurrentSetWrapper<Long>> entry : this.invited.entrySet()) {
            StreamUtils.write(entry.getKey(), bos);
            StreamUtils.write((short) entry.getValue().size(), bos);
            for (long pointId : entry.getValue().set)
                StreamUtils.write(pointId, bos);
        }

        StreamUtils.write(this.owners.size(), bos);
        for (Map.Entry<UUID, InvitationsContainerServer> outerEntry : this.owners.entrySet()) {
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
        sizeOuter = StreamUtils.readInt(bis),
        i = 0;
        WorldPoint worldPoint;
        for (; i < sizeOuter; i++) {
            worldPoint = WorldPoint.read(bis);
            this.cache.put(worldPoint.getId(), worldPoint);
        }

        sizeOuter = StreamUtils.readInt(bis);

        int j, sizeInner;
        UUID playerUUID;

        ConcurrentSetWrapper<Long> camps;
        for (i = 0; i < sizeOuter; i++) {
            playerUUID = StreamUtils.readUUID(bis);
            sizeInner = StreamUtils.readShort(bis);
            camps = new ConcurrentSetWrapper<Long>();
            for (j = 0; j < sizeInner; j++)
                camps.add(StreamUtils.readLong(bis));
            this.invited.put(playerUUID, camps);
        }

        sizeOuter = StreamUtils.readInt(bis);

        int sizeCamps, k;
        UUID invitedUUID;
        InvitationsContainerServer invitations;
        long pointId;
        for (i = 0; i < sizeOuter; i++) {
            invitations = new InvitationsContainerServer();
            playerUUID = StreamUtils.readUUID(bis);
            invitations.setId(StreamUtils.readLong(bis));
            sizeInner = StreamUtils.readShort(bis);
            for (j = 0; j < sizeInner; j++) {
                invitedUUID = StreamUtils.readUUID(bis);
                sizeCamps = StreamUtils.readShort(bis);
                camps = new ConcurrentSetWrapper<Long>();
                for (k = 0; k < sizeCamps; k++) {
                    pointId = StreamUtils.readLong(bis);
                    camps.add(pointId);
                    this.access.put(pointId, playerUUID);
                }
                invitations.invitedPlayers.put(invitedUUID, camps);
            }
            this.owners.put(playerUUID, invitations);
        }

        ConcurrentSetWrapper<UUID> players;
        for (InvitationsContainerServer invitation : this.owners.values()) {
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

    @Override
    public void reset() {
        this.cache.clear();
        this.access.clear();
        this.invited.clear();
        this.owners.clear();
    }
}
