package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.common.util.ImageTransferingServerBuffer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPStartImageUpload extends ProxyPacket {

    private ImageTransferingServerBuffer.EnumImageTransfer operation;

    private int partsAmount;

    private long pointId;

    public SPStartImageUpload() {}

    public SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer operation, long pointId, int partsAmount) {
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
        this.operation = ImageTransferingServerBuffer.EnumImageTransfer.values()[buffer.readByte()];
        this.pointId = buffer.readLong();
        if (!ImageTransferingServerBuffer.exist(this.pointId))
            ImageTransferingServerBuffer.create(
                    this.operation, 
                    CommonReference.uuid(getEntityPlayerMP(netHandler)), 
                    this.pointId, 
                    buffer.readShort());
    }
}
