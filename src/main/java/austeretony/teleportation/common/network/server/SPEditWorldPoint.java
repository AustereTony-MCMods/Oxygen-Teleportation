package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.util.PacketBufferUtils;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPEditWorldPoint extends ProxyPacket {

    private WorldPoint.EnumWorldPoints type;

    private long oldPointId;

    private String name, description;

    private boolean updateName, updateDescription, updateImage, updatePosition;

    public SPEditWorldPoint() {}

    public SPEditWorldPoint(WorldPoint.EnumWorldPoints type, long oldPointId, String name, String description, boolean updateName, 
            boolean updateDescription, boolean updateImage, boolean updatePosition) {
        this.type = type;
        this.oldPointId = oldPointId;
        this.name = name;
        this.description = description;
        this.updateName = updateName;
        this.updateDescription = updateDescription;
        this.updateImage = updateImage;
        this.updatePosition = updatePosition;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.type.ordinal());
        buffer.writeBoolean(this.updateName);
        buffer.writeBoolean(this.updateDescription);
        buffer.writeBoolean(this.updateImage);
        buffer.writeBoolean(this.updatePosition);
        buffer.writeLong(this.oldPointId);
        PacketBufferUtils.writeString(this.updateName ? this.name : "", buffer);
        PacketBufferUtils.writeString(this.updateDescription ? this.description : "", buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.type = WorldPoint.EnumWorldPoints.values()[buffer.readByte()];
        this.updateName = buffer.readBoolean();
        this.updateDescription = buffer.readBoolean();
        this.updateImage = buffer.readBoolean();
        this.updatePosition = buffer.readBoolean();
        switch (this.type) {
        case CAMP:            
            TeleportationManagerServer.instance().getCampsManager().editCamp(getEntityPlayerMP(netHandler), buffer.readLong(), PacketBufferUtils.readString(buffer), 
                    PacketBufferUtils.readString(buffer), this.updateName, this.updateDescription, this.updateImage, this.updatePosition);
            break;
        case LOCATION:
            TeleportationManagerServer.instance().getLocationsManager().editLocation(getEntityPlayerMP(netHandler), buffer.readLong(), PacketBufferUtils.readString(buffer), 
                    PacketBufferUtils.readString(buffer), this.updateName, this.updateDescription, this.updateImage, this.updatePosition);
            break;
        }
    }
}
