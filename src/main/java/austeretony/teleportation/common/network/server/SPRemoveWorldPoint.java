package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.menu.locations.LocationsManagerServer;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPRemoveWorldPoint extends ProxyPacket {

    private WorldPoint.EnumWorldPoints type;

    private long pointId;

    public SPRemoveWorldPoint() {}

    public SPRemoveWorldPoint(WorldPoint.EnumWorldPoints type, long pointId) {
        this.type = type;
        this.pointId = pointId;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.type.ordinal());
        buffer.writeLong(this.pointId);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.type = WorldPoint.EnumWorldPoints.values()[buffer.readByte()];
        switch (type) {
        case CAMP:            
            CampsManagerServer.instance().removeCamp(getEntityPlayerMP(netHandler), buffer.readLong());
            break;
        case LOCATION:
            LocationsManagerServer.instance().removeLocation(getEntityPlayerMP(netHandler), buffer.readLong());
            break;
        }
    }
}
