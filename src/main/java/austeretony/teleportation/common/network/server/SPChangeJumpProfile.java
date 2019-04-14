package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.main.TeleportationPlayerData;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPChangeJumpProfile extends ProxyPacket {

    private TeleportationPlayerData.EnumJumpProfile profile;

    public SPChangeJumpProfile() {}

    public SPChangeJumpProfile(TeleportationPlayerData.EnumJumpProfile profile) {
        this.profile = profile;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.profile.ordinal());
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerServer.instance().getPlayersManager().changeJumpProfile(getEntityPlayerMP(netHandler), TeleportationPlayerData.EnumJumpProfile.values()[buffer.readByte()]);
    }
}
