package austeretony.oxygen_teleportation.common.network.client;

import java.util.UUID;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncInvitedPlayers extends ProxyPacket {

    private UUID playerUUID;

    public CPSyncInvitedPlayers() {}

    public CPSyncInvitedPlayers(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerServer.instance().getSharedCampsManager().getInvitationsContainer(this.playerUUID).write(buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().getSharedCampsManager().reset();
        TeleportationManagerClient.instance().getSharedCampsManager().getInvitationsContainer().read(buffer);
        OxygenHelperClient.savePersistentDataDelegated(TeleportationManagerClient.instance().getSharedCampsManager());
    }
}