package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.common.util.ImageTransferingServerBuffer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPUploadImagePart extends ProxyPacket {

    private ImageTransferingServerBuffer.EnumImageTransfer operation;

    private int index, partsAmount;

    private long pointId;

    private int[] part;

    public SPUploadImagePart() {}

    public SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer operation, long pointId, int index, int[] part, int partsAmount) {
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
        for (int i = 0; i < this.part.length; i++)
            buffer.writeInt(this.part[i]);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.operation = ImageTransferingServerBuffer.EnumImageTransfer.values()[buffer.readByte()];
        this.pointId = buffer.readLong();
        this.index = buffer.readShort();
        this.partsAmount = buffer.readShort();
        int[] recieved = new int[buffer.readShort()];
        for (int i = 0; i < recieved.length; i++) 
            recieved[i] = buffer.readInt();
        if (ImageTransferingServerBuffer.exist(this.pointId))
            ImageTransferingServerBuffer.get(this.pointId).addPart(this.index, recieved);
        else {
            ImageTransferingServerBuffer.create(
                    operation, 
                    CommonReference.getPersistentUUID(getEntityPlayerMP(netHandler)), 
                    this.pointId, 
                    this.partsAmount);
            ImageTransferingServerBuffer.get(this.pointId).addPart(this.index, recieved);
        }
    }
}
