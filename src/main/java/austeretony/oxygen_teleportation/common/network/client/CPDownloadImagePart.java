package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.common.util.ImageTransferingClientBuffer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPDownloadImagePart extends Packet {

    private int ordinal, index, fragmentsAmount;

    private long pointId;

    private byte[] fragment;

    public CPDownloadImagePart() {}

    public CPDownloadImagePart(ImageTransferingClientBuffer.EnumImageTransfer operation, long pointId, int index, byte[] fragment, int fragmentsAmount) {
        this.ordinal = operation.ordinal();
        this.pointId = pointId;
        this.index = index;
        this.fragment = fragment;
        this.fragmentsAmount = fragmentsAmount;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal);
        buffer.writeLong(this.pointId);
        buffer.writeShort(this.index);
        buffer.writeShort(this.fragmentsAmount);
        buffer.writeShort(this.fragment.length);
        buffer.writeBytes(this.fragment);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final ImageTransferingClientBuffer.EnumImageTransfer operation = ImageTransferingClientBuffer.EnumImageTransfer.values()[buffer.readByte()];
        final long pointId = buffer.readLong();
        final int 
        index = buffer.readShort(),
        fragmentsAmount = buffer.readShort();
        byte[] fragment = new byte[buffer.readShort()];
        buffer.readBytes(fragment);
        OxygenHelperClient.addRoutineTask(()->ImageTransferingClientBuffer.processFragment(operation, pointId, fragmentsAmount, index, fragment));
    }
}
