package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen.common.itemstack.ItemStackWrapper;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncFeeItemStack extends ProxyPacket {

    public CPSyncFeeItemStack() {}

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerServer.instance().getFeeStackWrapper().write(buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().setFeeStack(ItemStackWrapper.read(buffer));
    }
}