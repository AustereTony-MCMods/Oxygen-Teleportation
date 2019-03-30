package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncCooldown extends ProxyPacket {

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        CampsManagerServer.instance().getPlayerProfile(OxygenHelperServer.uuid(getEntityPlayerMP(netHandler))).getCooldownInfo().write(buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        CampsManagerClient.instance().getPlayerProfile().getCooldownInfo().read(buffer);
    }
}
