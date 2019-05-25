package austeretony.oxygen_teleportation.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.IPersistentData;
import austeretony.oxygen.common.util.StreamUtils;
import austeretony.oxygen_teleportation.common.SharedCampsManagerServer.PlayersContainer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class SharedCampsManagerClient implements IPersistentData {

    private final TeleportationManagerClient manager;

    private final InvitationsContainerClient invitations;

    public SharedCampsManagerClient(TeleportationManagerClient manager) {
        this.manager = manager;
        this.invitations = new InvitationsContainerClient();
    }

    public InvitationsContainerClient getInvitationsContainer() {
        return this.invitations;
    }

    public boolean invitedPlayersExist(long pointId) {
        return this.invitations.invitedPlayers.containsKey(pointId) 
                && !this.invitations.invitedPlayers.get(pointId).players.isEmpty();
    }

    public int getInvitedPlayersAmountForCamp(long pointId) {
        if (!this.invitations.invitedPlayers.containsKey(pointId))
            return 0;
        return this.invitations.invitedPlayers.get(pointId).players.size();
    }

    public Set<UUID> getInvitedPlayers(long pointId) {
        return this.invitations.invitedPlayers.get(pointId).players;
    }

    @Override
    public String getName() {
        return "shared camps manager";
    }

    @Override
    public String getModId() {
        return TeleportationMain.MODID;
    }

    @Override
    public String getPath() {
        return "teleportation/shared_camps.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.invitations.getId(), bos);
        StreamUtils.write((short) this.invitations.invitedPlayers.size(), bos);
        for (Map.Entry<Long, PlayersContainer> entry : this.invitations.invitedPlayers.entrySet()) {
            StreamUtils.write((short) entry.getValue().players.size(), bos);
            for (UUID playerUUID : entry.getValue().players)
                StreamUtils.write(playerUUID, bos);
            StreamUtils.write(entry.getKey(), bos);
        }
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.invitations.setId(StreamUtils.readLong(bis));
        int 
        amountOuter = StreamUtils.readShort(bis),
        amountInner,
        i = 0,
        j;        
        PlayersContainer players;
        for (; i < amountOuter; i++) {
            players = new PlayersContainer();
            amountInner = StreamUtils.readShort(bis);
            for (j = 0; j < amountInner; j++)
                players.players.add(StreamUtils.readUUID(bis));
            this.invitations.invitedPlayers.put(StreamUtils.readLong(bis), players);
        }
    }

    public void reset() {
        this.invitations.invitedPlayers.clear();
        this.invitations.setId(0L);
    }

    public static class InvitationsContainerClient {

        private long id;

        public void setId(long id) {
            this.id = id;
        }

        public long getId() {
            return this.id;
        }

        public final Map<Long, PlayersContainer> invitedPlayers = new ConcurrentHashMap<Long, PlayersContainer>();
    }
}
