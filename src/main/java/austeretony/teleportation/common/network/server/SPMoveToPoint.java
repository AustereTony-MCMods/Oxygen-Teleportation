package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPMoveToPoint extends ProxyPacket {

    private WorldPoint.EnumWorldPoints point;

    private long pointId;

    public SPMoveToPoint() {}

    public SPMoveToPoint(WorldPoint.EnumWorldPoints point, long pointId) {
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
        this.point = WorldPoint.EnumWorldPoints.values()[buffer.readByte()];
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
