package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPLockPoint extends ProxyPacket {

    private WorldPoint.EnumWorldPoints type;

    private long oldPointId;

    private boolean flag;

    public SPLockPoint() {}

    public SPLockPoint(WorldPoint.EnumWorldPoints type, long pointId, boolean flag) {
        this.type = type;
        this.oldPointId = pointId;
        this.flag = flag;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.type.ordinal());
        buffer.writeLong(this.oldPointId);
        buffer.writeBoolean(this.flag);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.type = WorldPoint.EnumWorldPoints.values()[buffer.readByte()];
        switch (this.type) {
        case CAMP:           
            TeleportationManagerServer.instance().getCampsManager().lockCamp(getEntityPlayerMP(netHandler), buffer.readLong(), buffer.readBoolean());
            break;
        case LOCATION:
            TeleportationManagerServer.instance().getLocationsManager().lockLocation(getEntityPlayerMP(netHandler), buffer.readLong(), buffer.readBoolean());
            break;
        }
    }
}
