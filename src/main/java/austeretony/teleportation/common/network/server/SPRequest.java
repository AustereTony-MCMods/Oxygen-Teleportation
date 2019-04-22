package austeretony.teleportation.common.network.server;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationPlayerData;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.network.client.CPSyncCooldown;
import austeretony.teleportation.common.network.client.CPSyncValidWorldPointsIds;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPRequest extends ProxyPacket {

    private EnumRequest request;

    public SPRequest() {}

    public SPRequest(EnumRequest request) {
        this.request = request;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeByte(this.request.ordinal());
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        this.request = EnumRequest.values()[buffer.readByte()];
        switch (this.request) {
        case OPEN_MENU:
            this.processOpenMenuRequest(playerMP);
            break;
        }
    }

    private void processOpenMenuRequest(EntityPlayerMP playerMP) {
        UUID playerUUID = CommonReference.uuid(playerMP);
        if (TeleportationManagerServer.instance().profileExist(playerUUID)) {//for sure
            if (!OxygenHelperServer.isSyncing(playerUUID) && !TeleportationProcess.exist(playerUUID)) {
                TeleportationPlayerData playerProfile = TeleportationManagerServer.instance().getPlayerProfile(playerUUID);
                OxygenHelperServer.setSyncing(playerUUID, true);
                TeleportationMain.network().sendTo(new CPSyncCooldown(), playerMP);
                long[] camps = new long[playerProfile.getCampsAmount() + playerProfile.getOtherCampsAmount()];
                int index = 0;
                for (long id : playerProfile.getCampIds())
                    camps[index++] = id;
                for (long id : playerProfile.getOtherCampIds())
                    camps[index++] = id;
                long[] locations = new long[TeleportationManagerServer.instance().getWorldData().getLocationsAmount()];
                index = 0;
                for (long id : TeleportationManagerServer.instance().getWorldData().getLocationIds())
                    locations[index++] = id;
                TeleportationMain.network().sendTo(new CPSyncValidWorldPointsIds(camps, locations), playerMP); 
                OxygenHelperServer.syncSharedPlayersData(playerMP, OxygenHelperServer.getSharedDataIdentifiersForScreen(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID));
            }
        }
    }

    public enum EnumRequest {

        OPEN_MENU
    }
}
