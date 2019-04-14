package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.util.ImageTransferingClientBuffer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPStartImageDownload extends ProxyPacket {

    private ImageTransferingClientBuffer.EnumImageTransfer operation;

    private int partsAmount;

    private long pointId;

    public CPStartImageDownload() {}

    public CPStartImageDownload(ImageTransferingClientBuffer.EnumImageTransfer operation, long pointId, int partsAmount) {
        this.operation = operation;
        this.pointId = pointId;
        this.partsAmount = partsAmount;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.operation.ordinal());
        buffer.writeLong(this.pointId);
        buffer.writeShort(this.partsAmount);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.operation = ImageTransferingClientBuffer.EnumImageTransfer.values()[buffer.readByte()];
        this.pointId = buffer.readLong();
        if (!ImageTransferingClientBuffer.exist(this.pointId))
            ImageTransferingClientBuffer.create(
                    this.operation, 
                    this.pointId, 
                    buffer.readShort());
    }
}
