package austeretony.oxygen_teleportation.server;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPSyncAdditionalData;
import net.minecraft.entity.player.EntityPlayerMP;

public class CampsSyncHandlerServer implements DataSyncHandlerServer<WorldPoint> {

    @Override
    public int getDataId() {
        return TeleportationMain.CAMPS_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {   
        this.syncAdditionalData(playerUUID);
        return true;
    }

    private void syncAdditionalData(UUID playerUUID) {
        EntityPlayerMP playerMP = CommonReference.playerByUUID(playerUUID);
        TeleportationPlayerData playerData = TeleportationManagerServer.instance().getPlayersDataContainer().getPlayerData(playerUUID);
        long invitationsId = TeleportationManagerServer.instance().getSharedCampsContainer().haveInvitedPlayers(playerUUID) ?
                TeleportationManagerServer.instance().getSharedCampsContainer().getInvitationsContainer(playerUUID).getId() : 0L;
                OxygenMain.network().sendTo(new CPSyncAdditionalData(
                        playerData.getCooldownData().getLastCampTime(),
                        playerData.getCooldownData().getLastLocationTime(),
                        playerData.getCooldownData().getLastJumpTime(),
                        playerData.getFavoriteCampId(),
                        invitationsId), playerMP);
    }

    @Override
    public Set<Long> getIds(UUID playerUUID) {
        TeleportationPlayerData playerData = TeleportationManagerServer.instance().getPlayersDataContainer().getPlayerData(playerUUID);
        Set<Long> camps = new HashSet<>(playerData.getCampsAmount() + TeleportationManagerServer.instance().getSharedCampsContainer().getInvitationsAmount(playerUUID));
        for (long id : playerData.getCampIds())
            camps.add(id);
        if (TeleportationManagerServer.instance().getSharedCampsContainer().haveInvitations(playerUUID))
            for (long id : TeleportationManagerServer.instance().getSharedCampsContainer().getInvitations(playerUUID))
                camps.add(id);
        return camps;
    }

    @Override
    public WorldPoint getEntry(UUID playerUUID, long entryId) {
        TeleportationManagerServer.instance().getImagesLoader().loadAndSendCampPreviewImageAsync(CommonReference.playerByUUID(playerUUID), entryId);
        if (TeleportationManagerServer.instance().getSharedCampsContainer().haveInvitation(playerUUID, entryId))
            return TeleportationManagerServer.instance().getSharedCampsContainer().getCamp(entryId);
        else
            return TeleportationManagerServer.instance().getPlayersDataContainer().getPlayerData(playerUUID).getCamp(entryId);
    }
}
