package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen.common.api.OxygenGUIHelper;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.server.SPSendAbsentPointsIds;
import austeretony.oxygen_teleportation.common.world.WorldPoint;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncValidWorldPointsIds extends ProxyPacket {

    private long[] camps, locations;

    private long invitationsId;

    public CPSyncValidWorldPointsIds() {}

    public CPSyncValidWorldPointsIds(long[] camps, long[] locations, long invitationsId) {
        this.camps = camps;
        this.locations = locations;
        this.invitationsId = invitationsId;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeLong(TeleportationManagerServer.instance().getPlayerData(CommonReference.getPersistentUUID(getEntityPlayerMP(netHandler))).getFavoriteCampId());
        buffer.writeLong(this.invitationsId);
        buffer.writeShort(this.camps.length);
        for (long id : this.camps) 
            buffer.writeLong(id);
        buffer.writeShort(this.locations.length);
        for (long id : this.locations) 
            buffer.writeLong(id);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().getPlayerData().setFavoriteCampId(buffer.readLong());
        boolean downloadInvitations = TeleportationManagerClient.instance().getSharedCampsManager().getInvitationsContainer().getId() !=  buffer.readLong();
        int 
        indexNeedSync = 0,
        indexValidPoint = 0,
        i = 0,
        nscSize = 0;
        long[] needSyncCamps = new long[buffer.readShort()];
        if (needSyncCamps.length > 0) {
            this.camps = new long[needSyncCamps.length];
            for (; i < needSyncCamps.length; i++)
                this.camps[i] = buffer.readLong();    
            WorldPoint[] existingValidCamps = new WorldPoint[needSyncCamps.length];
            for (long id : this.camps) {
                if (!TeleportationManagerClient.instance().getPlayerData().campExist(id))
                    needSyncCamps[indexNeedSync++] = id;
                else               
                    existingValidCamps[indexValidPoint++] = TeleportationManagerClient.instance().getPlayerData().getCamp(id);
            }
            nscSize = indexNeedSync;
            TeleportationManagerClient.instance().getPlayerData().getCamps().clear();
            TeleportationManagerClient.instance().getPlayerData().resetOwnedAmount();
            for (WorldPoint worldPoint : existingValidCamps) {
                if (worldPoint == null) break;
                TeleportationManagerClient.instance().getPlayerData().addCamp(worldPoint);
            }
        }
        long[] needSyncLocations = new long[buffer.readShort()];
        indexNeedSync = indexValidPoint = i = 0;
        if (needSyncLocations.length > 0) {
            this.locations = new long[needSyncLocations.length];
            for (; i < needSyncLocations.length; i++)
                this.locations[i] = buffer.readLong(); 
            WorldPoint[] existingValidLocations = new WorldPoint[needSyncLocations.length];
            for (long id : this.locations) {
                if (!TeleportationManagerClient.instance().getWorldData().locationExist(id))
                    needSyncLocations[indexNeedSync++] = id;
                else               
                    existingValidLocations[indexValidPoint++] = TeleportationManagerClient.instance().getWorldData().getLocation(id);
            }
            TeleportationManagerClient.instance().getWorldData().getLocations().clear();
            for (WorldPoint worldPoint : existingValidLocations) {
                if (worldPoint == null) break;
                TeleportationManagerClient.instance().getWorldData().addLocation(worldPoint);
            }
        }
        if (nscSize > 0 || indexNeedSync > 0)
            OxygenGUIHelper.needSync(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
        TeleportationMain.network().sendToServer(new SPSendAbsentPointsIds(nscSize, needSyncCamps, indexNeedSync, needSyncLocations, downloadInvitations));
    }
}
