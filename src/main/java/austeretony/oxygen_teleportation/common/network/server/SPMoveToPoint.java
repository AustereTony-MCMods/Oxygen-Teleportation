package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.main.WorldPoint;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPMoveToPoint extends ProxyPacket {

    private WorldPoint.EnumWorldPoint point;

    private long pointId;

    public SPMoveToPoint() {}

    public SPMoveToPoint(WorldPoint.EnumWorldPoint point, long pointId) {
        this.point = point;
        this.pointId = pointId;
    }   

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.point.ordinal());
        buffer.writeLong(this.pointId);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.point = WorldPoint.EnumWorldPoint.values()[buffer.readByte()];
        switch (this.point) {
        case CAMP:
            TeleportationManagerServer.instance().getCampsManager().moveToCamp(getEntityPlayerMP(netHandler), buffer.readLong());
            break;
        case LOCATION:
            TeleportationManagerServer.instance().getLocationsManager().moveToLocation(getEntityPlayerMP(netHandler), buffer.readLong());
            break;
        }
    }
}
