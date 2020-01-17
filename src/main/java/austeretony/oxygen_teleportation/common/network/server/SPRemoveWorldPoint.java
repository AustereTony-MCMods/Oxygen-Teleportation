package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPRemoveWorldPoint extends Packet {

    private int ordinal;

    private long pointId;

    public SPRemoveWorldPoint() {}

    public SPRemoveWorldPoint(EnumWorldPoint point, long pointId) {
        this.ordinal = point.ordinal();
        this.pointId = pointId;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.pointId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), TeleportationMain.MANAGE_POINT_REQUEST_ID)) {
            final int ordinal = buffer.readByte();
            if (ordinal >= 0 && ordinal < EnumWorldPoint.values().length) {
                final EnumWorldPoint point = EnumWorldPoint.values()[ordinal];
                final long pointId = buffer.readLong();
                switch (point) {
                case CAMP:            
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getPlayersDataManager().removeCamp(playerMP, pointId));
                    break;
                case LOCATION:
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getLocationsManager().removeLocation(playerMP, pointId));
                    break;
                }
            }
        }
    }
}
