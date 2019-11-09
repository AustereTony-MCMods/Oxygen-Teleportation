package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.RequestsFilterHelper;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPCreateWorldPoint extends Packet {

    private int ordinal;

    private String name, description;

    public SPCreateWorldPoint() {}

    public SPCreateWorldPoint(EnumWorldPoint point, String name, String description) {
        this.ordinal = point.ordinal();
        this.name = name;
        this.description = description;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        ByteBufUtils.writeString(this.name, buffer);
        ByteBufUtils.writeString(this.description, buffer);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (RequestsFilterHelper.getLock(CommonReference.getPersistentUUID(playerMP), TeleportationMain.MANAGE_POINT_REQUEST_ID)) {
            final int ordinal = buffer.readByte();
            if (ordinal >= 0 && ordinal < EnumWorldPoint.values().length) {
                final EnumWorldPoint point = EnumWorldPoint.values()[ordinal];
                final String 
                name = ByteBufUtils.readString(buffer),
                description = ByteBufUtils.readString(buffer);
                switch (point) {
                case CAMP:            
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getPlayersDataManager().createCamp(playerMP, name, description));
                    break;
                case LOCATION:
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getLocationsManager().createLocation(playerMP, name, description));
                    break;
                }
            }
        }
    }
}
