package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPSyncCooldown extends Packet {

    private long campTime, locationTime, jumpTime;

    public CPSyncCooldown() {}

    public CPSyncCooldown(long campTime, long locationTime, long jumpTime) {
        this.campTime = campTime;
        this.locationTime = locationTime;
        this.jumpTime = jumpTime;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeLong(this.campTime);
        buffer.writeLong(this.locationTime);
        buffer.writeLong(this.jumpTime);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final long 
        campTime = buffer.readLong(),
        locationTime = buffer.readLong(), 
        jumpTime = buffer.readLong();   
        OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getPlayerDataManager().updateCooldown(campTime, locationTime, jumpTime));
    }
}
