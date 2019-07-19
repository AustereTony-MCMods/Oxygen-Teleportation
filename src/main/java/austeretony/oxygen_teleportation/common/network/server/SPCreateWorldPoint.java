package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.util.PacketBufferUtils;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.world.WorldPoint;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPCreateWorldPoint extends ProxyPacket {

    private WorldPoint.EnumPointType type;

    private long pointId;

    private String name, description;

    public SPCreateWorldPoint() {}

    public SPCreateWorldPoint(WorldPoint.EnumPointType type, WorldPoint worldPoint) {
        this.type = type;
        this.pointId = worldPoint.getId();
        this.name = worldPoint.getName();
        this.description = worldPoint.getDescription();
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.type.ordinal());
        buffer.writeLong(this.pointId);
        PacketBufferUtils.writeString(this.name, buffer);
        PacketBufferUtils.writeString(this.description, buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.type = WorldPoint.EnumPointType.values()[buffer.readByte()];
        switch (type) {
        case CAMP:            
            TeleportationManagerServer.instance().getCampsManager().createCamp(getEntityPlayerMP(netHandler), buffer.readLong(), PacketBufferUtils.readString(buffer), PacketBufferUtils.readString(buffer));
            break;
        case LOCATION:
            TeleportationManagerServer.instance().getLocationsManager().createLocation(getEntityPlayerMP(netHandler), buffer.readLong(), PacketBufferUtils.readString(buffer), PacketBufferUtils.readString(buffer));
            break;
        }
    }
}
