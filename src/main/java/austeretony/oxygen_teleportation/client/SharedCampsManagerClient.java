package austeretony.oxygen_teleportation.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.common.api.IPersistentData;
import austeretony.oxygen.util.ConcurrentSetWrapper;
import austeretony.oxygen.util.PacketBufferUtils;
import austeretony.oxygen.util.StreamUtils;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraft.network.PacketBuffer;

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
                && !this.invitations.invitedPlayers.get(pointId).isEmpty();
    }

    public int getInvitedPlayersAmountForCamp(long pointId) {
        if (!this.invitations.invitedPlayers.containsKey(pointId))
            return 0;
        return this.invitations.invitedPlayers.get(pointId).size();
    }

    public Set<UUID> getInvitedPlayers(long pointId) {
        return this.invitations.invitedPlayers.get(pointId).set;
    }

    public void uninvite(long pointId, UUID invitedUUID) {
        this.invitations.invitedPlayers.get(pointId).remove(invitedUUID);
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
        return "players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/invitations.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.invitations.getId(), bos);
        StreamUtils.write((short) this.invitations.invitedPlayers.size(), bos);
        for (Map.Entry<Long, ConcurrentSetWrapper<UUID>> entry : this.invitations.invitedPlayers.entrySet()) {
            StreamUtils.write((short) entry.getValue().size(), bos);
            for (UUID playerUUID : entry.getValue().set)
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
        ConcurrentSetWrapper<UUID> players;
        for (; i < amountOuter; i++) {
            players = new ConcurrentSetWrapper<UUID>();
            amountInner = StreamUtils.readShort(bis);
            for (j = 0; j < amountInner; j++)
                players.add(StreamUtils.readUUID(bis));
            this.invitations.invitedPlayers.put(StreamUtils.readLong(bis), players);
        }
    }

    public void reset() {
        this.invitations.invitedPlayers.clear();
        this.invitations.setId(0L);
    }

    public static class InvitationsContainerClient {

        private long id;

        public final Map<Long, ConcurrentSetWrapper<UUID>> invitedPlayers = new ConcurrentHashMap<Long, ConcurrentSetWrapper<UUID>>();

        public void setId(long id) {
            this.id = id;
        }

        public long getId() {
            return this.id;
        }

        public void read(PacketBuffer buffer) {
            this.setId(buffer.readLong());
            int 
            amountOuter = buffer.readShort(),
            amountInner,
            i = 0,
            j;
            ConcurrentSetWrapper<UUID> players;
            for (; i < amountOuter; i ++) {
                players = new ConcurrentSetWrapper<UUID>();
                amountInner = buffer.readShort();
                for (j = 0; j < amountInner; j++)
                    players.add(PacketBufferUtils.readUUID(buffer));
                this.invitedPlayers.put(buffer.readLong(), players);
            }
        }
    }
}
