package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.menu.locations.LocationsManagerServer;
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
            CampsManagerServer.instance().moveToCamp(getEntityPlayerMP(netHandler), buffer.readLong());
            break;
        case LOCATION:
            LocationsManagerServer.instance().moveToLocation(getEntityPlayerMP(netHandler), buffer.readLong());
            break;
        }
    }
}
