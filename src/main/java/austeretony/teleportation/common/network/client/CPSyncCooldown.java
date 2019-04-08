package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.reference.CommonReference;
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
        TeleportationManagerClient.instance().getPlayerProfile().getCooldownInfo().read(buffer);
    }
}
