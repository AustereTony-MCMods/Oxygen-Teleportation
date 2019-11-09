package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPWorldPointCreated extends Packet {

    private int ordinal;

    private WorldPoint worldPoint;

    public CPWorldPointCreated() {}

    public CPWorldPointCreated(EnumWorldPoint type, WorldPoint worldPoint) {
        this.ordinal = type.ordinal();
        this.worldPoint = worldPoint;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        this.worldPoint.write(buffer);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EnumWorldPoint type = EnumWorldPoint.values()[buffer.readByte()];
        final WorldPoint worldPoint = new WorldPoint();
        worldPoint.read(buffer);
        switch (type) {
        case CAMP:
            OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getPlayerDataManager().campCreated(worldPoint));
            break;
        case LOCATION:
            OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getLocationsManager().locationCreated(worldPoint));
            break;
        }
    }
}
