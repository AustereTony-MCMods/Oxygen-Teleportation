package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.util.ImageTransferingServerBuffer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPUploadImagePart extends Packet {

    private int ordinal, index, fragmentsAmount;

    private long pointId;

    private byte[] fragment;

    public SPUploadImagePart() {}

    public SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer operation, long pointId, int index, byte[] fragment, int fragmentsAmount) {
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
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        final int ordinal = buffer.readByte();
        if (ordinal >= 0 && ordinal < ImageTransferingServerBuffer.EnumImageTransfer.values().length) {
            final ImageTransferingServerBuffer.EnumImageTransfer operation = ImageTransferingServerBuffer.EnumImageTransfer.values()[ordinal];
            final long pointId = buffer.readLong();
            final int 
            index = buffer.readShort(),
            fragmentsAmount = buffer.readShort();
            final byte[] fragment = new byte[buffer.readShort()];
            buffer.readBytes(fragment);
            OxygenHelperServer.addRoutineTask(()->ImageTransferingServerBuffer.processFragment(operation, CommonReference.getPersistentUUID(playerMP), pointId, fragmentsAmount, index, fragment));
        }
    }
}
