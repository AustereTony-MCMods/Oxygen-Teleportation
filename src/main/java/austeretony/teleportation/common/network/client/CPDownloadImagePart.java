package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.util.ImageTransferingClientBuffer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPDownloadImagePart extends ProxyPacket {

    private ImageTransferingClientBuffer.EnumImageTransfer operation;

    private int index, partsAmount;

    private long pointId;

    private byte[] part;

    public CPDownloadImagePart() {}

    public CPDownloadImagePart(ImageTransferingClientBuffer.EnumImageTransfer operation, long pointId, int index, byte[] part, int partsAmount) {
        this.operation = operation;
        this.pointId = pointId;
        this.index = index;
        this.part = part;
        this.partsAmount = partsAmount;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.operation.ordinal());
        buffer.writeLong(this.pointId);
        buffer.writeShort(this.index);
        buffer.writeShort(this.partsAmount);
        buffer.writeShort(this.part.length);
        buffer.writeBytes(this.part);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.operation = ImageTransferingClientBuffer.EnumImageTransfer.values()[buffer.readByte()];
        this.pointId = buffer.readLong();
        this.index = buffer.readShort();
        this.partsAmount = buffer.readShort();
        byte[] recieved = new byte[buffer.readShort()];
        buffer.readBytes(recieved);
        if (ImageTransferingClientBuffer.exist(this.pointId))
            ImageTransferingClientBuffer.get(this.pointId).addPart(this.index, recieved);
        else {
            ImageTransferingClientBuffer.create(
                    operation, 
                    this.pointId, 
                    this.partsAmount);
            ImageTransferingClientBuffer.get(this.pointId).addPart(this.index, recieved);
        }
    }
}
