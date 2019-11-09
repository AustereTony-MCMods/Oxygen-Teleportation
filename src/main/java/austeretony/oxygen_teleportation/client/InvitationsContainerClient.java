package austeretony.oxygen_teleportation.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.ConcurrentSetWrapper;
import io.netty.buffer.ByteBuf;

public class InvitationsContainerClient {

    private long id;

    public final Map<Long, ConcurrentSetWrapper<UUID>> invitedPlayers = new ConcurrentHashMap<>();

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void replace(long oldId, long newId) {
        this.invitedPlayers.put(newId, this.invitedPlayers.remove(oldId));
    }

    public void read(ByteBuf buffer) {
        this.setId(buffer.readLong());
        int 
        amountOuter = buffer.readShort(),
        amountInner,
        i = 0,
        j;
        ConcurrentSetWrapper<UUID> players;
        for (; i < amountOuter; i ++) {
            players = new ConcurrentSetWrapper<>();
            amountInner = buffer.readShort();
            for (j = 0; j < amountInner; j++)
                players.add(ByteBufUtils.readUUID(buffer));
            this.invitedPlayers.put(buffer.readLong(), players);
        }
    }
}
