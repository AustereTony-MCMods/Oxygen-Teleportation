package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPCommand extends ProxyPacket {

    private int request;

    public CPCommand() {}

    public CPCommand(EnumCommand request) {
        this.request = request.ordinal();
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeBoolean(CommonReference.isOpped(getEntityPlayerMP(netHandler)));
        buffer.writeByte(this.request);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        CampsManagerClient.instance().setOpped(buffer.readBoolean());
        EnumCommand request = EnumCommand.values()[buffer.readByte()];
        switch (request) {
        case OPEN_MENU:
            CampsManagerClient.instance().openMenuDelegated();
            break;
        }
    }

    public enum EnumCommand {

        OPEN_MENU
    }
}
