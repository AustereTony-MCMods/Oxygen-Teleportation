package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.menu.players.PlayersManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPJumpRequestReply extends ProxyPacket {

    private EnumReply reply;

    public SPJumpRequestReply() {}

    public SPJumpRequestReply(EnumReply reply) {
        this.reply = reply;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.reply.ordinal());
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        PlayersManagerServer.instance().processJumpRequestReply(getEntityPlayerMP(netHandler), EnumReply.values()[buffer.readByte()]);
    }

    public enum EnumReply {

        ACCEPT,
        REJECT
    }
}
