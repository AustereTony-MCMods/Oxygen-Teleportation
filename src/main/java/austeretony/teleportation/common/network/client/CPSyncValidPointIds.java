package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.menu.locations.LocationsManagerClient;
import austeretony.teleportation.common.network.server.SPAbsentPoints;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncValidPointIds extends ProxyPacket {

    private long[] camps, locations;

    public CPSyncValidPointIds() {}

    public CPSyncValidPointIds(long[] camps, long[] locations) {
        this.camps = camps;
        this.locations = locations;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        buffer.writeLong(CampsManagerServer.instance().getPlayerProfile(OxygenHelperServer.uuid(playerMP)).getFavoriteCampId());
        buffer.writeInt(this.camps.length);
        for (long id : this.camps) 
            buffer.writeLong(id);
        buffer.writeInt(this.locations.length);
        for (long id : this.locations) 
            buffer.writeLong(id);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        CampsManagerClient.instance().setFavoriteCampSynced(buffer.readLong());
        int 
        indexNS = buffer.readInt(),
        indexVC = 0,
        nscSize, nslSize;
        this.camps = new long[indexNS];
        for (; indexVC < indexNS; indexVC++)
            this.camps[indexVC] = buffer.readLong();    
        long[] needSyncCamps = new long[indexNS];
        WorldPoint[] existingValidCamps = new WorldPoint[indexNS];
        indexNS = indexVC = 0;
        for (long id : this.camps) {
            if (!CampsManagerClient.instance().getPlayerProfile().campExist(id))
                needSyncCamps[indexNS++] = id;
            else               
                existingValidCamps[indexVC++] = CampsManagerClient.instance().getPlayerProfile().getCamp(id);
        }
        nscSize = indexNS;
        CampsManagerClient.instance().getPlayerProfile().getCamps().clear();
        for (WorldPoint worldPoint : existingValidCamps) {
            if (worldPoint == null) break;
            CampsManagerClient.instance().getPlayerProfile().addCamp(worldPoint);
        }
        indexNS = buffer.readInt();
        indexVC = 0;
        this.locations = new long[indexNS];
        for (; indexVC < indexNS; indexVC++)
            this.locations[indexVC] = buffer.readLong(); 
        long[] needSyncLocations = new long[indexNS];
        WorldPoint[] existingValidLocations = new WorldPoint[indexNS];
        indexNS = indexVC = 0;
        for (long id : this.locations) {
            if (!LocationsManagerClient.instance().getWorldProfile().locationExist(id))
                needSyncLocations[indexNS++] = id;
            else               
                existingValidLocations[indexVC++] = LocationsManagerClient.instance().getWorldProfile().getLocation(id);
        }
        nslSize = indexNS;
        LocationsManagerClient.instance().getWorldProfile().getLocations().clear();
        for (WorldPoint worldPoint : existingValidLocations) {
            if (worldPoint == null) break;
            LocationsManagerClient.instance().getWorldProfile().addLocation(worldPoint);
        }
        TeleportationMain.network().sendToServer(new SPAbsentPoints(nscSize, needSyncCamps, nslSize, needSyncLocations));
    }
}
