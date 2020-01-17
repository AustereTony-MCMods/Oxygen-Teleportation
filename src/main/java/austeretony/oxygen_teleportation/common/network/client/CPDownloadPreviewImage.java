package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPDownloadPreviewImage extends Packet {

    private int ordinal;

    private long pointId;

    private byte[] imageRaw;

    public CPDownloadPreviewImage() {}

    public CPDownloadPreviewImage(EnumWorldPoint type, long pointId, byte[] imageRaw) {
        this.ordinal = type.ordinal();
        this.pointId = pointId;
        this.imageRaw = imageRaw;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.pointId);
        buffer.writeInt(this.imageRaw.length);
        buffer.writeBytes(this.imageRaw);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EnumWorldPoint type = EnumWorldPoint.values()[buffer.readByte()];
        final long pointId = buffer.readLong();
        byte[] imageRaw = new byte[buffer.readInt()];
        buffer.readBytes(imageRaw);
        OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getImagesManager().processDownloadedPreviewImage(type, pointId, imageRaw));
    }
}
