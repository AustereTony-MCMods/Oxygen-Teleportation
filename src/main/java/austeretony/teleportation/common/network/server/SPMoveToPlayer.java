package austeretony.teleportation.common.network.server;

import java.util.UUID;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.util.PacketBufferUtils;
import austeretony.teleportation.common.TeleportationManagerServer;
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
        PacketBufferUtils.writeUUID(this.targetUUID, buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerServer.instance().getPlayersManager().moveToPlayer(getEntityPlayerMP(netHandler), PacketBufferUtils.readUUID(buffer));
    }
}
