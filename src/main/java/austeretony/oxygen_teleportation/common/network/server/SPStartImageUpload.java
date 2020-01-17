package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.util.ImageTransferingServerBuffer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPStartImageUpload extends Packet {

    private int ordinal, fragmentsAmount;

    private long pointId;

    public SPStartImageUpload() {}

    public SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer operation, long pointId, int fragmentsAmount) {
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
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), TeleportationMain.IMAGE_UPLOAD_REQUEST_ID)) {
            final int ordinal = buffer.readByte();
            if (ordinal >= 0 && ordinal < ImageTransferingServerBuffer.EnumImageTransfer.values().length) {
                final ImageTransferingServerBuffer.EnumImageTransfer operation = ImageTransferingServerBuffer.EnumImageTransfer.values()[ordinal];
                final long pointId = buffer.readLong();
                final int fragmentsAmount = buffer.readShort();
                OxygenHelperServer.addRoutineTask(()->ImageTransferingServerBuffer.create(operation, CommonReference.getPersistentUUID(playerMP), pointId, fragmentsAmount));
            }
        }
    }
}
