package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.main.CooldownInfo;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.server.SPTeleportationRequest;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncAdditionalData extends ProxyPacket {

    private CooldownInfo cooldownInfo;

    private long favoriteCampId, invitationsId;

    public CPSyncAdditionalData() {}

    public CPSyncAdditionalData(CooldownInfo cooldownInfo, long favoriteCampId, long invitationsId) {
        this.cooldownInfo = cooldownInfo;
        this.favoriteCampId = favoriteCampId;
        this.invitationsId = invitationsId;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        this.cooldownInfo.write(buffer);
        buffer.writeLong(this.favoriteCampId);
        buffer.writeLong(this.invitationsId);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().getPlayerData().getCooldownInfo().read(buffer);
        TeleportationManagerClient.instance().getPlayerData().setFavoriteCampId(buffer.readLong());
        this.invitationsId = buffer.readLong();
        if (this.invitationsId != 0L &&
                TeleportationManagerClient.instance().getSharedCampsManager().getInvitationsContainer().getId() != this.invitationsId)
            TeleportationMain.network().sendToServer(new SPTeleportationRequest(SPTeleportationRequest.EnumRequest.SYNC_INVITED_PLAYERS));
    }
}
