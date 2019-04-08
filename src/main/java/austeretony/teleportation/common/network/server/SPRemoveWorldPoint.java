package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.TeleportationManagerServer;
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
            TeleportationManagerServer.instance().getCampsManager().removeCamp(getEntityPlayerMP(netHandler), buffer.readLong());
            break;
        case LOCATION:
            TeleportationManagerServer.instance().getLocationsManager().removeLocation(getEntityPlayerMP(netHandler), buffer.readLong());
            break;
        }
    }
}
