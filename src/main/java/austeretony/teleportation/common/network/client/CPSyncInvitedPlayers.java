package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.main.SharedCamps;
import austeretony.teleportation.common.main.TeleportationPlayerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncInvitedPlayers extends ProxyPacket {

    public CPSyncInvitedPlayers() {}

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        TeleportationPlayerData playerProfile = TeleportationManagerServer.instance().getPlayerProfile(CommonReference.uuid(playerMP));
        buffer.writeShort(playerProfile.getSharedCampsAmount());
        for (SharedCamps sharedCamps : playerProfile.getSharedCamps())
            sharedCamps.write(buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().getPlayerData().clearSharedCamps();
        TeleportationManagerClient.instance().getPlayerData().clearInvitedPlayers();
        int amount = buffer.readShort();
        SharedCamps sharedCamps;
        for (int i = 0; i < amount; i++) {
            sharedCamps = SharedCamps.read(buffer);
            for (long id : sharedCamps.getCamps())
                TeleportationManagerClient.instance().getPlayerData().inviteToCamp(id, sharedCamps.playerUUID, sharedCamps.username);
        }
    }
}
