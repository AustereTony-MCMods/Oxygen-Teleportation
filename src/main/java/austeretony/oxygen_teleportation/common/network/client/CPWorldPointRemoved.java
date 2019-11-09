package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPWorldPointRemoved extends Packet {

    private int ordinal;

    private long pointId;

    public CPWorldPointRemoved() {}

    public CPWorldPointRemoved(EnumWorldPoint type, long pointId) {
        this.ordinal = type.ordinal();
        this.pointId = pointId;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.pointId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EnumWorldPoint type = EnumWorldPoint.values()[buffer.readByte()];
        final long pointId = buffer.readLong();
        switch (type) {
        case CAMP:
            OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getPlayerDataManager().campRemoved(pointId));
            break;
        case LOCATION:
            OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getLocationsManager().locationRemoved(pointId));
            break;
        }
    }
}
