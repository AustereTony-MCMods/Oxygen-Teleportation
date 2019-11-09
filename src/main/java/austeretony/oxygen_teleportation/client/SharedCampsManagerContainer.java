package austeretony.oxygen_teleportation.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.ConcurrentSetWrapper;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import io.netty.buffer.ByteBuf;

public class SharedCampsManagerContainer extends AbstractPersistentData {

    private final TeleportationManagerClient manager;

    private final InvitationsContainerClient invitations = new InvitationsContainerClient();

    protected SharedCampsManagerContainer(TeleportationManagerClient manager) {
        this.manager = manager;
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

    public void invitationsDataReceived(ByteBuf buffer) {
        try {
            this.reset();
            this.getInvitationsContainer().read(buffer);
            this.setChanged(true);
        } finally {
            if (buffer != null)
                buffer.release();
        }
    }

    @Override
    public String getDisplayName() {
        return "shared_camps";
    }

    @Override
    public String getPath() {
        return OxygenHelperClient.getDataFolder() + "/server/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/invitations.dat";
    }

    @Override
    public long getSaveDelayMinutes() {
        return TeleportationConfig.CAMPS_SAVE_DELAY_MINUTES.getIntValue();
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
            players = new ConcurrentSetWrapper<>();
            amountInner = StreamUtils.readShort(bis);
            for (j = 0; j < amountInner; j++)
                players.add(StreamUtils.readUUID(bis));
            this.invitations.invitedPlayers.put(StreamUtils.readLong(bis), players);
        }
    }

    @Override
    public void reset() {
        this.invitations.invitedPlayers.clear();
        this.invitations.setId(0L);
    }
}
