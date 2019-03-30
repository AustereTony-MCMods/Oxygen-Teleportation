package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.util.PacketBufferUtils;
import austeretony.teleportation.common.menu.players.PlayersManagerClient;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSendJumpRequest extends ProxyPacket {

    private String visitorUsername;

    public CPSendJumpRequest() {}

    public CPSendJumpRequest(String visitorUsername) {
        this.visitorUsername = visitorUsername;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        PacketBufferUtils.writeString(this.visitorUsername, buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        PlayersManagerClient.instance().getJumpRequest().start(PacketBufferUtils.readString(buffer));
    }
}
