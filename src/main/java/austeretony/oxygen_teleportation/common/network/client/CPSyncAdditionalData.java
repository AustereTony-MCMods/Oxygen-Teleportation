package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPSyncAdditionalData extends Packet {

    private long campTime, locationTime, jumpTime, favoriteCampId, invitationsId;

    public CPSyncAdditionalData() {}

    public CPSyncAdditionalData(long campTime, long locationTime, long jumpTime, long favoriteCampId, long invitationsId) {
        this.campTime = campTime;
        this.locationTime = locationTime;
        this.jumpTime = jumpTime;
        this.favoriteCampId = favoriteCampId;
        this.invitationsId = invitationsId;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeLong(this.campTime);
        buffer.writeLong(this.locationTime);
        buffer.writeLong(this.jumpTime);
        buffer.writeLong(this.favoriteCampId);
        buffer.writeLong(this.invitationsId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final long 
        campTime = buffer.readLong(),
        locationTime = buffer.readLong(), 
        jumpTime = buffer.readLong(),
        favoriteCampId = buffer.readLong(),
        invitationsId = buffer.readLong();   
        OxygenHelperClient.addRoutineTask(()->TeleportationManagerClient.instance().getPlayerDataManager().additionalDataReceived(campTime, locationTime, jumpTime, favoriteCampId, invitationsId));     
    }
}
