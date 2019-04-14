package austeretony.teleportation.common.network.client;

import java.util.UUID;

import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.main.TeleportationPlayerData;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncWorldPoints extends ProxyPacket {

    private WorldPoint.EnumWorldPoints type;

    private long[] points;

    public CPSyncWorldPoints() {}

    public CPSyncWorldPoints(WorldPoint.EnumWorldPoints type, long[] pointIds) {
        this.type = type;
        this.points = pointIds;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        buffer.writeByte(this.type.ordinal());
        buffer.writeShort(this.points.length);
        switch (this.type) {
        case CAMP:
            UUID otherUUID;
            TeleportationPlayerData ownerProfile = TeleportationManagerServer.instance().getPlayerProfile(CommonReference.uuid(playerMP));
            for (long id : this.points) {
                if (ownerProfile.getOtherCampIds().contains(id)) {
                    otherUUID = ownerProfile.getOtherCampOwner(id);
                    if (TeleportationManagerServer.instance().profileExist(otherUUID))
                        TeleportationManagerServer.instance().getPlayerProfile(otherUUID).getCamp(id).write(buffer);
                    else {
                        TeleportationManagerServer.instance().getCampsLoader().loadPlayerData(otherUUID);//TODO IO operation... this is not good - need reliable solution
                        TeleportationManagerServer.instance().getPlayerProfile(otherUUID).getCamp(id).write(buffer);
                    }
                } else
                    ownerProfile.getCamp(id).write(buffer);
            }
            break;
        case LOCATION:
            for (long id : this.points)
                TeleportationManagerServer.instance().getWorldData().getLocation(id).write(buffer);
            break;
        }
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.type = WorldPoint.EnumWorldPoints.values()[buffer.readByte()];
        int 
        amount = buffer.readShort(),
        i;
        switch (this.type) {
        case CAMP:
            for (i = 0; i < amount; i++)
                TeleportationManagerClient.instance().getPlayerData().addCamp(WorldPoint.read(buffer));
            TeleportationManagerClient.instance().getCampsLoader().savePlayerDataDelegated();
            break;
        case LOCATION:
            for (i = 0; i < amount; i++)
                TeleportationManagerClient.instance().getWorldProfile().addLocation(WorldPoint.read(buffer));
            TeleportationManagerClient.instance().getLocationsLoader().saveLocationsDataDelegated();
            break;
        }
    }
}
