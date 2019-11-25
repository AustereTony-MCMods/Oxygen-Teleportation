package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPSyncCooldown extends Packet {

    private int campCooldownLeftSeconds, locationCooldownLeftSeconds, jumpCooldownLeftSeconds;

    public CPSyncCooldown() {}

    public CPSyncCooldown(int campCooldownLeftSeconds, int locationCooldownLeftSeconds, int jumpCooldownLeftSeconds) {
        this.campCooldownLeftSeconds = campCooldownLeftSeconds;
        this.locationCooldownLeftSeconds = locationCooldownLeftSeconds;
        this.jumpCooldownLeftSeconds = jumpCooldownLeftSeconds;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeShort(this.campCooldownLeftSeconds);
        buffer.writeShort(this.locationCooldownLeftSeconds);
        buffer.writeShort(this.jumpCooldownLeftSeconds);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final int 
        campCooldownLeftSeconds = buffer.readShort(),
        locationCooldownLeftSeconds = buffer.readShort(), 
        jumpCooldownLeftSeconds = buffer.readShort();   
        OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getPlayerDataManager().updateCooldown(campCooldownLeftSeconds, locationCooldownLeftSeconds, jumpCooldownLeftSeconds));
    }
}
