package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.camps.SharedCamps;
import austeretony.teleportation.common.main.PlayerProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncInvitedPlayers extends ProxyPacket {

    public CPSyncInvitedPlayers() {}

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        PlayerProfile playerProfile = TeleportationManagerServer.instance().getPlayerProfile(CommonReference.uuid(playerMP));
        buffer.writeShort(playerProfile.getSharedCamps().size());
        for (SharedCamps sharedCamps : playerProfile.getSharedCamps().values())
            sharedCamps.write(buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().getPlayerProfile().getSharedCamps().clear();
        TeleportationManagerClient.instance().getPlayerProfile().getInvitedPlayers().clear();
        int amount = buffer.readShort();
        SharedCamps sharedCamps;
        for (int i = 0; i < amount; i++) {
            sharedCamps = SharedCamps.read(buffer);
            for (long id : sharedCamps.getCamps())
                TeleportationManagerClient.instance().getPlayerProfile().inviteToCamp(id, sharedCamps.playerUUID, sharedCamps.username);
        }
    }
}
