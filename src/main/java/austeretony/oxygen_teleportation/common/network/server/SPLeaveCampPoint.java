package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPLeaveCampPoint extends ProxyPacket {

    private long pointId;

    public SPLeaveCampPoint() {}

    public SPLeaveCampPoint(long pointId) {
        this.pointId = pointId;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeLong(this.pointId);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {     
        TeleportationManagerServer.instance().getCampsManager().leaveCamp(getEntityPlayerMP(netHandler), buffer.readLong());
    }
}
