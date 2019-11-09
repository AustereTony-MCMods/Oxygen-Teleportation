package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPWorldPointEdited extends Packet {

    private int ordinal;

    private long oldPointId;

    private WorldPoint worldPoint;

    private boolean updateImage;

    public CPWorldPointEdited() {}

    public CPWorldPointEdited(EnumWorldPoint type, long oldPointId, WorldPoint worldPoint, boolean updateImage) {
        this.ordinal = type.ordinal();
        this.oldPointId = oldPointId;
        this.worldPoint = worldPoint;
        this.updateImage = updateImage;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.oldPointId);
        this.worldPoint.write(buffer);
        buffer.writeBoolean(this.updateImage);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EnumWorldPoint type = EnumWorldPoint.values()[buffer.readByte()];
        final long oldPointId = buffer.readLong();
        final WorldPoint worldPoint = new WorldPoint();
        worldPoint.read(buffer);
        final boolean updateImage = buffer.readBoolean();
        switch (type) {
        case CAMP:
            OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getPlayerDataManager().campEdited(oldPointId, worldPoint, updateImage));
            break;
        case LOCATION:
            OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getLocationsManager().locationEdited(oldPointId, worldPoint, updateImage));
            break;
        }
    }
}
