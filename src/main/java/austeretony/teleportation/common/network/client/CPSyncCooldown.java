package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.TeleportationManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncCooldown extends ProxyPacket {

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerServer.instance().getPlayerProfile(CommonReference.uuid(getEntityPlayerMP(netHandler))).getCooldownInfo().write(buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().getPlayerData().getCooldownInfo().read(buffer);
    }
}
