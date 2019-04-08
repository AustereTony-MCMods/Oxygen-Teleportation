package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.TeleportationManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPSetFavoriteCamp extends ProxyPacket {

    private long pointId;

    public SPSetFavoriteCamp() {}

    public SPSetFavoriteCamp(long pointId) {
        this.pointId = pointId;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeLong(this.pointId);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerServer.instance().getCampsManager().setFavoriteCamp(getEntityPlayerMP(netHandler), buffer.readLong());
    }
}
