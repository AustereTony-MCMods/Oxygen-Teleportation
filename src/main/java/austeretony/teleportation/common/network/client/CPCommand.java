package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.client.TeleportationManagerClient;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPCommand extends ProxyPacket {

    private EnumCommand command;

    public CPCommand() {}

    public CPCommand(EnumCommand command) {
        this.command = command;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.command.ordinal());
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.command = EnumCommand.values()[buffer.readByte()];
        switch (this.command) {
        case OPEN_MENU:
            TeleportationManagerClient.instance().openMenuDelegated();
            break;
        }
    }

    public enum EnumCommand {

        OPEN_MENU
    }
}
