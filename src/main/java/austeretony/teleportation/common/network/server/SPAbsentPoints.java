package austeretony.teleportation.common.network.server;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.menu.camps.CampsLoaderServer;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.menu.locations.LocationsManagerServer;
import austeretony.teleportation.common.network.client.CPCommand;
import austeretony.teleportation.common.network.client.CPSyncPoints;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPAbsentPoints extends ProxyPacket {

    private long[] absentCamps, absentLocations;

    private int campsSize, locationsSize;

    public SPAbsentPoints() {}

    public SPAbsentPoints(int campsSize, long[] absentCamps, int locationsSize, long[] absentLocations) {
        this.campsSize = campsSize;
        this.absentCamps = absentCamps;
        this.locationsSize = locationsSize;
        this.absentLocations = absentLocations;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeInt(this.campsSize);
        for (long id : this.absentCamps) {
            if (id == 0) break;
            buffer.writeLong(id);
        }
        buffer.writeInt(this.locationsSize);
        for (long id : this.absentLocations) {
            if (id == 0) break;
            buffer.writeLong(id);
        }
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        this.absentCamps = new long[buffer.readInt()];
        int i = 0;
        for (; i < this.absentCamps.length; i++)
            this.absentCamps[i] = buffer.readLong(); 
        if (this.absentCamps.length > 0) {
            TeleportationMain.network().sendTo(new CPSyncPoints(WorldPoint.EnumWorldPoints.CAMP, this.absentCamps), playerMP);
            CampsLoaderServer.loadAndSendCampPreviewImagesDelegated(playerMP, this.absentCamps);
        }
        this.absentLocations = new long[buffer.readInt()];
        i = 0;
        for (; i < this.absentLocations.length; i++)
            this.absentLocations[i] = buffer.readLong(); 
        if (this.absentLocations.length > 0) {
            TeleportationMain.network().sendTo(new CPSyncPoints(WorldPoint.EnumWorldPoints.LOCATION, this.absentLocations), playerMP);
            LocationsManagerServer.instance().downloadLocationPreviewsToClientDelegated(playerMP, this.absentLocations);
        }
        OxygenHelperServer.syncPlayersData(playerMP, TeleportationMain.JUMP_PROFILE_DATA_ID);
        TeleportationMain.network().sendTo(new CPCommand(CPCommand.EnumCommand.OPEN_MENU), playerMP);
        CampsManagerServer.instance().getPlayerProfile(OxygenHelperServer.uuid(playerMP)).setSyncing(false);
    }
}
