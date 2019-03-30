package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.util.ImageTransferingClientBuffer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPStartImageDownload extends ProxyPacket {

    private int operation, partsAmount;

    private long pointId;

    public CPStartImageDownload() {}

    public CPStartImageDownload(ImageTransferingClientBuffer.EnumImageTransfer operation, long pointId, int partsAmount) {
        this.operation = operation.ordinal();
        this.pointId = pointId;
        this.partsAmount = partsAmount;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.operation);
        buffer.writeLong(this.pointId);
        buffer.writeInt(this.partsAmount);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.operation = buffer.readByte();
        this.pointId = buffer.readLong();
        if (!ImageTransferingClientBuffer.exist(this.pointId))
            ImageTransferingClientBuffer.create(
                    ImageTransferingClientBuffer.EnumImageTransfer.values()[this.operation], 
                    this.pointId, 
                    buffer.readInt());
    }
}
