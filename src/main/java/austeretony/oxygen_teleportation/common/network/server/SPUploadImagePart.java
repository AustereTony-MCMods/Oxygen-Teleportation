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

    private int[] fragment;

    public SPUploadImagePart() {}

    public SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer operation, long pointId, int index, int[] fragment, int fragmentsAmount) {
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
        for (int i : this.fragment)
            buffer.writeInt(i);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        //if (RequestsFilterHelper.getLock(CommonReference.getPersistentUUID(playerMP), TeleportationMain.IMAGE_UPLOAD_REQUEST_ID)) {
        final int ordinal = buffer.readByte();
        if (ordinal >= 0 && ordinal < ImageTransferingServerBuffer.EnumImageTransfer.values().length) {
            final ImageTransferingServerBuffer.EnumImageTransfer operation = ImageTransferingServerBuffer.EnumImageTransfer.values()[ordinal];
            final long pointId = buffer.readLong();
            final int 
            index = buffer.readShort(),
            fragmentsAmount = buffer.readShort();
            final int[] fragment = new int[buffer.readShort()];
            for (int i = 0; i < fragment.length; i++) 
                fragment[i] = buffer.readInt();
            OxygenHelperServer.addRoutineTask(()->ImageTransferingServerBuffer.processFragment(operation, CommonReference.getPersistentUUID(playerMP), pointId, fragmentsAmount, index, fragment));
        }
        //}
    }
}
