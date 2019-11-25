package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPSyncAdditionalData extends Packet {

    private long favoriteCampId, invitationsId;

    private int campCooldownLeftSeconds, locationCooldownLeftSeconds, jumpCooldownLeftSeconds;

    public CPSyncAdditionalData() {}

    public CPSyncAdditionalData(int campCooldownLeftSeconds, int locationCooldownLeftSeconds, int jumpCooldownLeftSeconds, long favoriteCampId, long invitationsId) {
        this.campCooldownLeftSeconds = campCooldownLeftSeconds;
        this.locationCooldownLeftSeconds = locationCooldownLeftSeconds;
        this.jumpCooldownLeftSeconds = jumpCooldownLeftSeconds;
        this.favoriteCampId = favoriteCampId;
        this.invitationsId = invitationsId;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeShort(this.campCooldownLeftSeconds);
        buffer.writeShort(this.locationCooldownLeftSeconds);
        buffer.writeShort(this.jumpCooldownLeftSeconds);
        buffer.writeLong(this.favoriteCampId);
        buffer.writeLong(this.invitationsId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final int 
        campCooldownLeftSeconds = buffer.readShort(),
        locationCooldownLeftSeconds = buffer.readShort(), 
        jumpCooldownLeftSeconds = buffer.readShort();   
        final long 
        favoriteCampId = buffer.readLong(),
        invitationsId = buffer.readLong();   
        OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getPlayerDataManager().additionalDataReceived(
                campCooldownLeftSeconds, locationCooldownLeftSeconds, jumpCooldownLeftSeconds, favoriteCampId, invitationsId));     
    }
}
