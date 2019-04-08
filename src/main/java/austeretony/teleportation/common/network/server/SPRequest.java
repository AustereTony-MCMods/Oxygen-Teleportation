package austeretony.teleportation.common.network.server;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.network.client.CPCommand;
import austeretony.teleportation.common.network.client.CPSyncCooldown;
import austeretony.teleportation.common.network.client.CPSyncValidWorldPointIds;
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
        if (TeleportationManagerServer.instance().profileExist(playerUUID))//for sure
            if (!TeleportationManagerServer.instance().getPlayerProfile(playerUUID).isSyncing() && !TeleportationProcess.exist(playerUUID)) {
                PlayerProfile playerProfile = TeleportationManagerServer.instance().getPlayerProfile(playerUUID);
                playerProfile.setSyncing(true);
                TeleportationMain.network().sendTo(new CPSyncCooldown(), playerMP);
                long[] camps = new long[playerProfile.getCampsAmount() + playerProfile.getOtherCampsAmount()];
                int index = 0;
                for (long id : playerProfile.getCampIds())
                    camps[index++] = id;
                for (long id : playerProfile.getOtherCampIds())
                    camps[index++] = id;
                long[] locations = new long[TeleportationManagerServer.instance().getWorldProfile().getLocationsAmount()];
                index = 0;
                for (long id : TeleportationManagerServer.instance().getWorldProfile().getLocationIds())
                    locations[index++] = id;
                if (camps.length > 0 || locations.length > 0)
                    TeleportationMain.network().sendTo(new CPSyncValidWorldPointIds(camps, locations), playerMP); 
                if (camps.length == 0 && locations.length == 0) {
                    OxygenHelperServer.syncPlayersData(playerMP, TeleportationMain.JUMP_PROFILE_DATA_ID);
                    TeleportationMain.network().sendTo(new CPCommand(CPCommand.EnumCommand.OPEN_MENU), playerMP);
                    playerProfile.setSyncing(false);
                }
            }
    }

    public enum EnumRequest {

        OPEN_MENU
    }
}
