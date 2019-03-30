package austeretony.teleportation.common.network.client;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.menu.camps.CampsLoaderClient;
import austeretony.teleportation.common.menu.camps.CampsLoaderServer;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.menu.locations.LocationsLoaderClient;
import austeretony.teleportation.common.menu.locations.LocationsManagerClient;
import austeretony.teleportation.common.menu.locations.LocationsManagerServer;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public class CPSyncPoints extends ProxyPacket {

    private WorldPoint.EnumWorldPoints type;

    private long[] points;

    public CPSyncPoints() {}

    public CPSyncPoints(WorldPoint.EnumWorldPoints type, long[] pointIds) {
        this.type = type;
        this.points = pointIds;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = ((NetHandlerPlayServer) netHandler).player;
        buffer.writeByte(this.type.ordinal());
        buffer.writeInt(this.points.length);
        switch (this.type) {
        case CAMP:
            UUID ownerUUID;
            for (long id : this.points) {
                if (CampsManagerServer.instance().getPlayerProfile(OxygenHelperServer.uuid(playerMP)).getOtherCampIds().contains(id)) {
                    ownerUUID = CampsManagerServer.instance().getPlayerProfile(OxygenHelperServer.uuid(playerMP)).getOtherCampOwner(id);
                    if (CampsManagerServer.instance().isProfileExist(ownerUUID))
                        CampsManagerServer.instance().getPlayerProfile(ownerUUID).getCamp(id).write(buffer);
                    else {
                        CampsLoaderServer.loadPlayerData(ownerUUID);//IO operation
                        CampsManagerServer.instance().getPlayerProfile(ownerUUID).getCamp(id).write(buffer);
                    }
                } else
                    CampsManagerServer.instance().getPlayerProfile(OxygenHelperServer.uuid(playerMP)).getCamp(id).write(buffer);
            }
            break;
        case LOCATION:
            for (long id : this.points)
                LocationsManagerServer.instance().getWorldProfile().getLocation(id).write(buffer);
            break;
        }
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.type = WorldPoint.EnumWorldPoints.values()[buffer.readByte()];
        int amount = buffer.readInt();
        switch (this.type) {
        case CAMP:
            for (int i = 0; i < amount; i++)
                CampsManagerClient.instance().getPlayerProfile().addCamp(WorldPoint.read(buffer));
            CampsLoaderClient.savePlayerDataDelegated();
            break;
        case LOCATION:
            for (int i = 0; i < amount; i++)
                LocationsManagerClient.instance().getWorldProfile().addLocation(WorldPoint.read(buffer));
            LocationsLoaderClient.saveLocationsDataDelegated();
            break;
        }
    }
}
