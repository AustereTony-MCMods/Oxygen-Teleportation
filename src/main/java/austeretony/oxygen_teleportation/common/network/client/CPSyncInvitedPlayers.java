package austeretony.oxygen_teleportation.common.network.client;

import java.util.Map;
import java.util.UUID;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.util.PacketBufferUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.SharedCampsManagerServer.InvitationsContainerServer;
import austeretony.oxygen_teleportation.common.SharedCampsManagerServer.PlayersContainer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncInvitedPlayers extends ProxyPacket {

    private InvitationsContainerServer invitations;

    public CPSyncInvitedPlayers() {}

    public CPSyncInvitedPlayers(InvitationsContainerServer invitations) {
        this.invitations = invitations;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeLong(this.invitations.getId());
        buffer.writeShort(this.invitations.access.size());
        for (Map.Entry<Long, PlayersContainer> entry : this.invitations.access.entrySet()) {
            buffer.writeShort(entry.getValue().players.size());
            for (UUID playerUUID : entry.getValue().players)    
                PacketBufferUtils.writeUUID(playerUUID, buffer);
            buffer.writeLong(entry.getKey());
        }
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().getSharedCampsManager().reset();
        TeleportationManagerClient.instance().getSharedCampsManager().getInvitationsContainer().setId(buffer.readLong());
        int 
        amountOuter = buffer.readShort(),
        amountInner,
        i = 0,
        j;
        PlayersContainer players;
        for (; i < amountOuter; i ++) {
            players = new PlayersContainer();
            amountInner = buffer.readShort();
            for (j = 0; j < amountInner; j++)
                players.players.add(PacketBufferUtils.readUUID(buffer));
            TeleportationManagerClient.instance().getSharedCampsManager().getInvitationsContainer().invitedPlayers.put(buffer.readLong(), players);
        }
        OxygenHelperClient.savePlayerDataDelegated(TeleportationManagerClient.instance().getSharedCampsManager());
    }
}
