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
        if (firstIds != null)
            for (long id : firstIds)
                TeleportationManagerServer.instance().getPlayerData(playerUUID).getCamp(id).write(buffer);
        if (secondIds != null)
            for (long id : secondIds)
                TeleportationManagerServer.instance().getWorldData().getLocation(id).write(buffer);
    }
}
