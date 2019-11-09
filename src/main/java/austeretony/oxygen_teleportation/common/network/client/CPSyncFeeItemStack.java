package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPSyncFeeItemStack extends Packet {

    public CPSyncFeeItemStack() {}

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        TeleportationManagerServer.instance().getFeeStackWrapper().write(buffer);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final ItemStackWrapper stackWrapper = ItemStackWrapper.read(buffer);
        OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().setFeeStack(stackWrapper));
    }
}
