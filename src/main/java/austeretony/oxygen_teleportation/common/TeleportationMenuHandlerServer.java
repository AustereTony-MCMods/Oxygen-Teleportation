package austeretony.oxygen_teleportation.common;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import austeretony.oxygen.common.api.network.OxygenNetwork;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.sync.gui.api.IComplexGUIHandlerServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.network.client.CPSyncAdditionalData;
import net.minecraft.network.PacketBuffer;

public class TeleportationMenuHandlerServer implements IComplexGUIHandlerServer {

    @Override
    public OxygenNetwork getNetwork() {
        return TeleportationMain.network();
    }

    @Override
    public Set<Long> getValidIdentifiersFirst(UUID playerUUID) {        
        TeleportationPlayerData playerData = TeleportationManagerServer.instance().getPlayerData(playerUUID);
        long invitationsId = TeleportationManagerServer.instance().getSharedCampsManager().haveInvitedPlayers(playerUUID) ? TeleportationManagerServer.instance().getSharedCampsManager().getInvitationsContainer(playerUUID).getId() : 0L;
        TeleportationMain.network().sendTo(new CPSyncAdditionalData(playerData.getCooldownInfo(), playerData.getFavoriteCampId(), invitationsId), CommonReference.playerByUUID(playerUUID));
        Set<Long> camps = new HashSet<Long>(playerData.getCampsAmount() + TeleportationManagerServer.instance().getSharedCampsManager().getInvitationsAmount(playerUUID));
        for (long id : playerData.getCampIds())
            camps.add(id);
        if (TeleportationManagerServer.instance().getSharedCampsManager().haveInvitations(playerUUID))
            for (long id : TeleportationManagerServer.instance().getSharedCampsManager().getInvitations(playerUUID))
                camps.add(id);
        return camps;
    }

    @Override
    public Set<Long> getValidIdentifiersSecond(UUID playerUUID) {
        return TeleportationManagerServer.instance().getWorldData().getLocationIds();
    }

    @Override
    public void writeEntries(UUID playerUUID, PacketBuffer buffer, long[] firstIds, long[] secondIds) {
        if (firstIds != null) {
            TeleportationPlayerData playerData = TeleportationManagerServer.instance().getPlayerData(playerUUID);
            for (long id : firstIds) {
                if (TeleportationManagerServer.instance().getSharedCampsManager().haveInvitation(playerUUID, id))
                    TeleportationManagerServer.instance().getSharedCampsManager().getCamp(id).write(buffer);
                else
                    playerData.getCamp(id).write(buffer);
            }
            TeleportationManagerServer.instance().getImagesLoader().loadAndSendCampPreviewImagesDelegated(CommonReference.playerByUUID(playerUUID), firstIds);
        }
        if (secondIds != null) {
            for (long id : secondIds)
                TeleportationManagerServer.instance().getWorldData().getLocation(id).write(buffer);
            TeleportationManagerServer.instance().getImagesManager().downloadLocationPreviewsToClientDelegated(CommonReference.playerByUUID(playerUUID), secondIds);
        }
    }
}
