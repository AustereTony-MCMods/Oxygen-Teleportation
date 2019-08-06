package austeretony.oxygen_teleportation.common.network.server;

import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPSyncInvitedPlayers;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPTeleportationRequest extends ProxyPacket {

    private EnumRequest request;

    public SPTeleportationRequest() {}

    public SPTeleportationRequest(EnumRequest request) {
        this.request = request;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.request.ordinal());
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        this.request = EnumRequest.values()[buffer.readByte()];
        switch (this.request) {
        case SYNC_INVITED_PLAYERS:
            TeleportationMain.network().sendTo(new CPSyncInvitedPlayers(CommonReference.getPersistentUUID(playerMP)), playerMP);
            break;
        }
    }

    public enum EnumRequest {

        SYNC_INVITED_PLAYERS
    }
}
