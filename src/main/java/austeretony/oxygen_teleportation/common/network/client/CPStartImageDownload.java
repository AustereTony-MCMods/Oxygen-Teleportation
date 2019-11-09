package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.common.util.ImageTransferingClientBuffer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPStartImageDownload extends Packet {

    private int ordinal, fragmentsAmount;

    private long pointId;

    public CPStartImageDownload() {}

    public CPStartImageDownload(ImageTransferingClientBuffer.EnumImageTransfer operation, long pointId, int fragmentsAmount) {
        this.ordinal = operation.ordinal();
        this.pointId = pointId;
        this.fragmentsAmount = fragmentsAmount;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.pointId);
        buffer.writeShort(this.fragmentsAmount);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final ImageTransferingClientBuffer.EnumImageTransfer operation = ImageTransferingClientBuffer.EnumImageTransfer.values()[buffer.readByte()];
        final long pointId = buffer.readLong();
        final int fragmentsAmount = buffer.readShort();
        OxygenHelperClient.addRoutineTask(()->ImageTransferingClientBuffer.create(operation, pointId, fragmentsAmount));
    }
}
