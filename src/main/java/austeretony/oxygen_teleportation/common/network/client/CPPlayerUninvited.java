package austeretony.oxygen_teleportation.common.network.client;

import java.util.UUID;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPPlayerUninvited extends Packet {

    private long pointId;

    private UUID playerUUID;

    public CPPlayerUninvited() {}

    public CPPlayerUninvited(long pointId, UUID playerUUID) {
        this.pointId = pointId;
        this.playerUUID = playerUUID;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeLong(this.pointId);
        ByteBufUtils.writeUUID(this.playerUUID, buffer);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final long pointId = buffer.readLong();
        final UUID playerUUID = ByteBufUtils.readUUID(buffer);
        OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getPlayerDataManager().playerUninvited(pointId, playerUUID));
    }
}
