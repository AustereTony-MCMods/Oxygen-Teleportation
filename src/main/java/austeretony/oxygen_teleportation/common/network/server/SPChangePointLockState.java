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

public class SPChangePointLockState extends Packet {

    private int ordinal;

    private long pointId;

    private boolean flag;

    public SPChangePointLockState() {}

    public SPChangePointLockState(EnumWorldPoint point, long pointId, boolean flag) {
        this.ordinal = point.ordinal();
        this.pointId = pointId;
        this.flag = flag;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.pointId);
        buffer.writeBoolean(this.flag);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), TeleportationMain.MANAGE_POINT_REQUEST_ID)) {
            final int ordinal = buffer.readByte();
            if (ordinal >= 0 && ordinal < EnumWorldPoint.values().length) {
                final EnumWorldPoint point = EnumWorldPoint.values()[ordinal];
                final long pointId = buffer.readLong();
                final boolean flag = buffer.readBoolean();
                switch (point) {
                case CAMP:            
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getPlayersDataManager().changeCampLockState(playerMP, pointId, flag));
                    break;
                case LOCATION:
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getLocationsManager().changeLocationLockState(playerMP, pointId, flag));
                    break;
                }
            }
        }
    }
}
