package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.util.ImageTransferingServerBuffer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPStartImageUpload extends ProxyPacket {

    private int operation, partsAmount;

    private long pointId;

    public SPStartImageUpload() {}

    public SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer operation, long pointId, int partsAmount) {
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
        if (!ImageTransferingServerBuffer.exist(this.pointId))
            ImageTransferingServerBuffer.create(
                    ImageTransferingServerBuffer.EnumImageTransfer.values()[this.operation], 
                    OxygenHelperServer.uuid(getEntityPlayerMP(netHandler)), 
                    this.pointId, 
                    buffer.readInt());
    }
}
