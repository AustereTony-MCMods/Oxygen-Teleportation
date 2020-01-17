package austeretony.oxygen_teleportation.common.network.server;

import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPManageInvitation extends Packet {

    private int ordinal;

    private long pointId;

    private UUID playerUUID;

    public SPManageInvitation() {}

    public SPManageInvitation(EnumOperation operation, long pointId, UUID playerUUID) {
        this.ordinal = operation.ordinal();
        this.pointId = pointId;
        this.playerUUID = playerUUID;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.pointId);
        ByteBufUtils.writeUUID(this.playerUUID, buffer);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), TeleportationMain.MANAGE_POINT_REQUEST_ID)) {
            final int ordinal = buffer.readByte();
            if (ordinal >= 0 && ordinal < EnumOperation.values().length) {
                final EnumOperation operation = EnumOperation.values()[ordinal];
                final long pointId = buffer.readLong();
                final UUID playerUUID = ByteBufUtils.readUUID(buffer);
                switch (operation) {
                case INVITE:
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getPlayersDataManager().invitePlayer(playerMP, pointId, playerUUID));
                    break;
                case UNINVITE:
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getPlayersDataManager().uninvitePlayer(playerMP, pointId, playerUUID));
                    break;
                }
            }
        }
    }

    public enum EnumOperation {

        INVITE,
        UNINVITE
    }
}
