package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.RequestsFilterHelper;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPMoveToFavoriteCamp extends Packet {

    private long pointId;

    public SPMoveToFavoriteCamp() {}    

    public SPMoveToFavoriteCamp(long pointId) {
        this.pointId = pointId;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeLong(this.pointId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (RequestsFilterHelper.getLock(CommonReference.getPersistentUUID(playerMP), TeleportationMain.TELEPORT_REQUEST_ID)) {
            final long pointId = buffer.readLong();
            OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getPlayersDataManager().moveToFavoriteCamp(playerMP, pointId));
        }
    }
}
