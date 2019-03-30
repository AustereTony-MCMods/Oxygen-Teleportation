package austeretony.teleportation.common.network.server;

import java.util.UUID;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.menu.players.PlayersManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPMoveToPlayer extends ProxyPacket {

    private UUID targetUUID;

    public SPMoveToPlayer() {}

    public SPMoveToPlayer(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeLong(this.targetUUID.getMostSignificantBits());
        buffer.writeLong(this.targetUUID.getLeastSignificantBits());
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        PlayersManagerServer.instance().moveToPlayer(getEntityPlayerMP(netHandler), new UUID(buffer.readLong(), buffer.readLong()));
    }
}
