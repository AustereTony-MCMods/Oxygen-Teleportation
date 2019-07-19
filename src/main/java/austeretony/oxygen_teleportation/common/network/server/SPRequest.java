package austeretony.oxygen_teleportation.common.network.server;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.main.TeleportationProcess;
import austeretony.oxygen_teleportation.common.network.client.CPSyncCooldown;
import austeretony.oxygen_teleportation.common.network.client.CPSyncValidWorldPointsIds;
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
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (TeleportationManagerServer.instance().dataExist(playerUUID)) {//for sure
            if (!OxygenHelperServer.isSyncing(playerUUID) 
                    && !TeleportationProcess.exist(playerUUID)) {
                TeleportationPlayerData playerData = TeleportationManagerServer.instance().getPlayerData(playerUUID);
                OxygenHelperServer.setSyncing(playerUUID, true);
                TeleportationMain.network().sendTo(new CPSyncCooldown(playerData.getCooldownInfo()), playerMP);
                long[] camps = new long[playerData.getCampsAmount() + TeleportationManagerServer.instance().getSharedCampsManager().getInvitationsAmount(playerUUID)];
                int index = 0;
                for (long id : playerData.getCampIds())
                    camps[index++] = id;
                if (TeleportationManagerServer.instance().getSharedCampsManager().haveInvitations(playerUUID))
                    for (long id : TeleportationManagerServer.instance().getSharedCampsManager().getInvitations(playerUUID))
                        camps[index++] = id;
                long[] locations = new long[TeleportationManagerServer.instance().getWorldData().getLocationsAmount()];
                index = 0;
                for (long id : TeleportationManagerServer.instance().getWorldData().getLocationIds())
                    locations[index++] = id;
                long invitationsId = TeleportationManagerServer.instance().getSharedCampsManager().haveInvitedPlayers(playerUUID) ? TeleportationManagerServer.instance().getSharedCampsManager().getInvitationsContainer(playerUUID).getId() : 0L;
                TeleportationMain.network().sendTo(new CPSyncValidWorldPointsIds(camps, locations, invitationsId), playerMP); 
                OxygenHelperServer.syncSharedPlayersData(playerMP, OxygenHelperServer.getSharedDataIdentifiersForScreen(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID));
            }
        }
    }

    public enum EnumRequest {

        OPEN_MENU
    }
}
