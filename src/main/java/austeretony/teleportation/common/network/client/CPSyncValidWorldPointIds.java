package austeretony.teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.network.server.SPAbsentPoints;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncValidWorldPointIds extends ProxyPacket {

    private long[] camps, locations;

    public CPSyncValidWorldPointIds() {}

    public CPSyncValidWorldPointIds(long[] camps, long[] locations) {
        this.camps = camps;
        this.locations = locations;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeLong(TeleportationManagerServer.instance().getPlayerProfile(CommonReference.uuid(getEntityPlayerMP(netHandler))).getFavoriteCampId());
        buffer.writeShort(this.camps.length);
        for (long id : this.camps) 
            buffer.writeLong(id);
        buffer.writeShort(this.locations.length);
        for (long id : this.locations) 
            buffer.writeLong(id);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().getPlayerProfile().setFavoriteCampId(buffer.readLong());
        int 
        indexNS = buffer.readShort(),
        indexVC = 0,
        nscSize = 0, 
        nslSize = 0;
        long[] needSyncCamps = new long[indexNS];
        if (indexNS > 0) {
            this.camps = new long[indexNS];
            for (; indexVC < indexNS; indexVC++)
                this.camps[indexVC] = buffer.readLong();    
            WorldPoint[] existingValidCamps = new WorldPoint[indexNS];
            indexNS = indexVC = 0;
            for (long id : this.camps) {
                if (!TeleportationManagerClient.instance().getPlayerProfile().campExist(id))
                    needSyncCamps[indexNS++] = id;
                else               
                    existingValidCamps[indexVC++] = TeleportationManagerClient.instance().getPlayerProfile().getCamp(id);
            }
            nscSize = indexNS;
            TeleportationManagerClient.instance().getPlayerProfile().getCamps().clear();
            for (WorldPoint worldPoint : existingValidCamps) {
                if (worldPoint == null) break;
                TeleportationManagerClient.instance().getPlayerProfile().addCamp(worldPoint);
            }
        }
        indexNS = buffer.readShort();
        long[] needSyncLocations = new long[indexNS];
        if (indexNS > 0) {
            indexVC = 0;
            this.locations = new long[indexNS];
            for (; indexVC < indexNS; indexVC++)
                this.locations[indexVC] = buffer.readLong(); 
            WorldPoint[] existingValidLocations = new WorldPoint[indexNS];
            indexNS = indexVC = 0;
            for (long id : this.locations) {
                if (!TeleportationManagerClient.instance().getWorldProfile().locationExist(id))
                    needSyncLocations[indexNS++] = id;
                else               
                    existingValidLocations[indexVC++] = TeleportationManagerClient.instance().getWorldProfile().getLocation(id);
            }
            nslSize = indexNS;
            TeleportationManagerClient.instance().getWorldProfile().getLocations().clear();
            for (WorldPoint worldPoint : existingValidLocations) {
                if (worldPoint == null) break;
                TeleportationManagerClient.instance().getWorldProfile().addLocation(worldPoint);
            }
        }
        TeleportationMain.network().sendToServer(new SPAbsentPoints(nscSize, needSyncCamps, nslSize, needSyncLocations));
    }
}
