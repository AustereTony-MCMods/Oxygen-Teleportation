package austeretony.teleportation.common.network.server;

import java.util.UUID;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.TeleportationManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPManageInvitation extends ProxyPacket {

    private EnumOperation operation;

    private long pointId;

    private UUID playerUUID;

    public SPManageInvitation() {}

    public SPManageInvitation(EnumOperation operation, long pointId, UUID playerUUID) {
        this.operation = operation;
        this.pointId = pointId;
        this.playerUUID = playerUUID;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.operation.ordinal());
        buffer.writeLong(this.pointId);
        buffer.writeLong(this.playerUUID.getMostSignificantBits());
        buffer.writeLong(this.playerUUID.getLeastSignificantBits());
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.operation = EnumOperation.values()[buffer.readByte()];
        switch (this.operation) {
        case INVITE:
            TeleportationManagerServer.instance().getCampsManager().invitePlayer(getEntityPlayerMP(netHandler), buffer.readLong(), new UUID(buffer.readLong(), buffer.readLong()));
            break;
        case UNINVITE:
            TeleportationManagerServer.instance().getCampsManager().uninvitePlayer(getEntityPlayerMP(netHandler), buffer.readLong(), new UUID(buffer.readLong(), buffer.readLong()));
            break;
        }
    }

    public enum EnumOperation {

        INVITE,
        UNINVITE
    }
}
