package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPFavoriteCampUpdated extends Packet {

    private long pointId;

    public CPFavoriteCampUpdated() {}

    public CPFavoriteCampUpdated(long pointId) {
        this.pointId = pointId;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeLong(this.pointId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final long pointId = buffer.readLong();
        OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getPlayerDataManager().favoriteCampSet(pointId));
    }
}
