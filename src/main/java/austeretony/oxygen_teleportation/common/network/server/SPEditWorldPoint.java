package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPEditWorldPoint extends Packet {

    private int ordinal;

    private long pointId;

    private String name, description;

    private boolean updatePosition, updateImage;

    public SPEditWorldPoint() {}

    public SPEditWorldPoint(EnumWorldPoint point, long pointId, String name, String description, boolean updatePosition, boolean updateImage) {
        this.ordinal = point.ordinal();
        this.pointId = pointId;
        this.name = name;
        this.description = description;
        this.updatePosition = updatePosition;
        this.updateImage = updateImage;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.pointId);
        ByteBufUtils.writeString(this.name, buffer);
        ByteBufUtils.writeString(this.description, buffer);
        buffer.writeBoolean(this.updatePosition);
        buffer.writeBoolean(this.updateImage);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), TeleportationMain.MANAGE_POINT_REQUEST_ID)) {
            final int ordinal = buffer.readByte();
            if (ordinal >= 0 && ordinal < EnumWorldPoint.values().length) {
                final EnumWorldPoint point = EnumWorldPoint.values()[ordinal];
                final long pointId = buffer.readLong();
                final String 
                name = ByteBufUtils.readString(buffer),
                description = ByteBufUtils.readString(buffer);
                final boolean 
                updatePosition = buffer.readBoolean(),
                updateImage = buffer.readBoolean();
                switch (point) {
                case CAMP:            
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getPlayersDataManager().editCamp(playerMP, pointId, name, description, updatePosition, updateImage));
                    break;
                case LOCATION:
                    OxygenHelperServer.addRoutineTask(()->TeleportationManagerServer.instance().getLocationsManager().editLocation(playerMP, pointId, name, description, updatePosition, updateImage));
                    break;
                }
            }
        }
    }
}
