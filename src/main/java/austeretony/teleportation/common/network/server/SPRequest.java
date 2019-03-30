package austeretony.teleportation.common.network.server;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.menu.locations.LocationsManagerServer;
import austeretony.teleportation.common.network.client.CPCommand;
import austeretony.teleportation.common.network.client.CPSyncCooldown;
import austeretony.teleportation.common.network.client.CPSyncValidPointIds;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPRequest extends ProxyPacket {

    private int request;

    public SPRequest() {}

    public SPRequest(EnumRequest request) {
        this.request = request.ordinal();
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.request);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        EnumRequest request = EnumRequest.values()[buffer.readByte()];
        switch (request) {
        case OPEN_MENU:
            this.processOpenMenuRequest(playerMP);
            break;
        }
    }

    private void processOpenMenuRequest(EntityPlayerMP playerMP) {
        UUID playerUUID = OxygenHelperServer.uuid(playerMP);
        if (!CampsManagerServer.instance().getPlayerProfile(playerUUID).isSyncing() && !TeleportationProcess.exist(playerUUID)) {
            CampsManagerServer.instance().getPlayerProfile(playerUUID).setSyncing(true);
            TeleportationMain.network().sendTo(new CPSyncCooldown(), playerMP);
            long[] camps = new long[CampsManagerServer.instance().getPlayerProfile(playerUUID).getCampsAmount() 
                                    + CampsManagerServer.instance().getPlayerProfile(playerUUID).getOtherCampsAmount()];
            int index = 0;
            for (long id : CampsManagerServer.instance().getPlayerProfile(playerUUID).getCampIds())
                camps[index++] = id;
            for (long id : CampsManagerServer.instance().getPlayerProfile(playerUUID).getOtherCampIds())
                camps[index++] = id;
            long[] locations = new long[LocationsManagerServer.instance().getWorldProfile().getLocationsAmount()];
            index = 0;
            for (long id : LocationsManagerServer.instance().getWorldProfile().getLocationIds())
                locations[index++] = id;
            if (camps.length > 0 || locations.length > 0)
                TeleportationMain.network().sendTo(new CPSyncValidPointIds(camps, locations), playerMP); 
            if (camps.length == 0 && locations.length == 0) {
                OxygenHelperServer.syncPlayersData(playerMP, TeleportationMain.JUMP_PROFILE_DATA_ID);
                TeleportationMain.network().sendTo(new CPCommand(CPCommand.EnumCommand.OPEN_MENU), playerMP);
                CampsManagerServer.instance().getPlayerProfile(playerUUID).setSyncing(false);
            }
        }
    }

    public enum EnumRequest {

        OPEN_MENU
    }
}
