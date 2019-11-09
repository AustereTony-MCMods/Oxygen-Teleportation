package austeretony.oxygen_teleportation.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.ConcurrentSetWrapper;
import io.netty.buffer.ByteBuf;

public class InvitationsContainerServer {

    private long id;

    public final Map<UUID, ConcurrentSetWrapper<Long>> invitedPlayers = new ConcurrentHashMap<>();

    public final Map<Long, ConcurrentSetWrapper<UUID>> access = new ConcurrentHashMap<>();

    public void updateId() {
        this.id = System.currentTimeMillis();
    }

    public void setId(long id) {    
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void write(ByteBuf buffer) {
        buffer.writeLong(this.getId());
        buffer.writeShort(this.access.size());
        for (Map.Entry<Long, ConcurrentSetWrapper<UUID>> entry : this.access.entrySet()) {
            buffer.writeShort(entry.getValue().size());
            for (UUID playerUUID : entry.getValue().set)    
                ByteBufUtils.writeUUID(playerUUID, buffer);
            buffer.writeLong(entry.getKey());
        }
    }
}
