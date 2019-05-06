package austeretony.teleportation.client;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.core.api.ClientReference;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.common.SharedCampsManagerServer.PlayersContainer;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumTeleportationPrivileges;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.teleportation.common.network.server.SPEditWorldPoint;
import austeretony.teleportation.common.network.server.SPLeaveCampPoint;
import austeretony.teleportation.common.network.server.SPLockPoint;
import austeretony.teleportation.common.network.server.SPManageInvitation;
import austeretony.teleportation.common.network.server.SPMoveToPoint;
import austeretony.teleportation.common.network.server.SPRemoveWorldPoint;
import austeretony.teleportation.common.network.server.SPSetFavoriteCamp;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayer;

public class CampsManagerClient {

    private final TeleportationManagerClient manager;

    public CampsManagerClient(TeleportationManagerClient manager) {
        this.manager = manager;
    }

    public void downloadCampsDataSynced() {
        this.manager.getPlayerData().reset();
        this.manager.getSharedCampsManager().reset();
        this.manager.openMenuSynced();
    }

    public void moveToCampSynced(long id) {        
        if (id != 0L) {
            TeleportationMain.network().sendToServer(new SPMoveToPoint(WorldPoint.EnumWorldPoints.CAMP, id));
            this.manager.setTeleportationDelay(PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.CAMP_TELEPORTATION_DELAY.toString(), TeleportationConfig.CAMPS_TELEPORT_DELAY.getIntValue()));
        }
    }

    public void createCampPointSynced(String name, String description) {
        if (this.manager.getPlayerData().getOwnedCampsAmount() 
                < PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue())) {
            WorldPoint worldPoint = new WorldPoint(
                    OxygenHelperClient.getPlayerUUID(),
                    ClientReference.getClientPlayer().getName(), 
                    name, 
                    description,
                    ClientReference.getClientPlayer().dimension,
                    (float) ClientReference.getClientPlayer().posX, 
                    (float) ClientReference.getClientPlayer().posY, 
                    (float) ClientReference.getClientPlayer().posZ,
                    ClientReference.getClientPlayer().rotationYawHead, 
                    ClientReference.getClientPlayer().rotationPitch);
            worldPoint.createId();
            worldPoint.createDate();
            this.manager.getPlayerData().addCamp(worldPoint);
            TeleportationMain.network().sendToServer(new SPCreateWorldPoint(WorldPoint.EnumWorldPoints.CAMP, worldPoint));
            OxygenHelperClient.savePlayerDataDelegated(this.manager.getPlayerData());
            this.manager.getImagesManager().cacheLatestImage(worldPoint.getId());
            this.manager.getImagesLoader().saveLatestCampPreviewImageDelegated(worldPoint.getId());
            this.manager.getImagesManager().uploadCampPreviewToServerDelegated(worldPoint.getId());
        }
    }

    public void removeCampPointSynced(long pointId) {
        this.manager.getPlayerData().removeCamp(pointId);
        TeleportationMain.network().sendToServer(new SPRemoveWorldPoint(WorldPoint.EnumWorldPoints.CAMP, pointId));
        if (pointId == this.manager.getPlayerData().getFavoriteCampId())
            this.manager.getPlayerData().setFavoriteCampId(0L);
        OxygenHelperClient.savePlayerDataDelegated(this.manager.getPlayerData());
        this.manager.getImagesManager().getPreviewImages().remove(pointId);
    }

    public void setFavoriteCampSynced(long pointId) {        
        this.manager.getPlayerData().setFavoriteCampId(pointId);
        TeleportationMain.network().sendToServer(new SPSetFavoriteCamp(pointId));
        OxygenHelperClient.savePlayerDataDelegated(this.manager.getPlayerData());
    }

    public void lockCampSynced(WorldPoint worldPoint, boolean flag) {    
        long oldPointId = worldPoint.getId();
        worldPoint.setLocked(flag);
        worldPoint.setId(worldPoint.getId() + 1L);
        this.manager.getPlayerData().addCamp(worldPoint);
        if (this.manager.getPlayerData().getFavoriteCampId() == oldPointId)
            this.manager.getPlayerData().setFavoriteCampId(worldPoint.getId());
        this.manager.getPlayerData().removeCamp(oldPointId);
        OxygenHelperClient.savePlayerDataDelegated(this.manager.getPlayerData());
        TeleportationMain.network().sendToServer(new SPLockPoint(WorldPoint.EnumWorldPoints.CAMP, oldPointId, flag));
        this.manager.getImagesLoader().renameCampPreviewImageDelegated(oldPointId, worldPoint.getId());
        this.manager.getImagesManager().replaceCachedImage(oldPointId, worldPoint.getId());

        if (this.manager.getSharedCampsManager().invitedPlayersExist(oldPointId)) {//just for proper visualization
            PlayersContainer players = this.manager.getSharedCampsManager().getInvitationsContainer().invitedPlayers.remove(oldPointId);
            this.manager.getSharedCampsManager().getInvitationsContainer().invitedPlayers.put(worldPoint.getId(), players);
        }   
    }

    public void editCampPointSynced(WorldPoint worldPoint, String newName, String newDescription, boolean updateImage, boolean updatePosition) {
        long 
        oldPointId = worldPoint.getId(),
        newPointId =  oldPointId + 1L;
        boolean 
        edited = false,
        updateName = false,
        updateDescription = false;
        if (!newName.equals(worldPoint.getName())) {
            updateName = true;
            worldPoint.setName(newName);
        }
        if (!newDescription.equals(worldPoint.getDescription())) {
            updateDescription = true;
            worldPoint.setDescription(newDescription);
        }
        if (updateImage) {
            this.manager.getImagesManager().cacheLatestImage(newPointId);
            this.manager.getImagesManager().removeCachedImage(oldPointId);
            this.manager.getImagesManager().uploadCampPreviewToServerDelegated(newPointId);
            this.manager.getImagesLoader().saveLatestCampPreviewImageDelegated(newPointId);
        }
        if (updatePosition) {
            EntityPlayer player = ClientReference.getClientPlayer();
            worldPoint.setPosition(player.rotationYaw, player.rotationPitch, (float) player.posX, (float) player.posY, (float) player.posZ, player.dimension);

        }
        edited = updateName || updateDescription || updateImage || updatePosition;
        if (edited) {
            worldPoint.setId(newPointId);
            this.manager.getPlayerData().addCamp(worldPoint);
            if (this.manager.getPlayerData().getFavoriteCampId() == oldPointId)
                this.manager.getPlayerData().setFavoriteCampId(newPointId);
            this.manager.getPlayerData().removeCamp(oldPointId);
            OxygenHelperClient.savePlayerDataDelegated(this.manager.getPlayerData());
            TeleportationMain.network().sendToServer(new SPEditWorldPoint(WorldPoint.EnumWorldPoints.CAMP, oldPointId, newName, newDescription, 
                    updateName, updateDescription, updateImage, updatePosition));
            if (!updateImage) {
                this.manager.getImagesLoader().renameCampPreviewImageDelegated(oldPointId, newPointId);
                this.manager.getImagesManager().replaceCachedImage(oldPointId, newPointId);
            }

            if (this.manager.getSharedCampsManager().invitedPlayersExist(oldPointId)) {//just for proper visualization
                PlayersContainer players = this.manager.getSharedCampsManager().getInvitationsContainer().invitedPlayers.remove(oldPointId);
                this.manager.getSharedCampsManager().getInvitationsContainer().invitedPlayers.put(worldPoint.getId(), players);
            }   
        }
    }

    public void invitePlayerSynced(long pointId, UUID playerUUID) {
        TeleportationMain.network().sendToServer(new SPManageInvitation(SPManageInvitation.EnumOperation.INVITE, pointId, playerUUID));
    }

    public void uninvitePlayerSynced(long pointId, UUID playerUUID) {
        TeleportationMain.network().sendToServer(new SPManageInvitation(SPManageInvitation.EnumOperation.UNINVITE, pointId, playerUUID));
        OxygenHelperClient.savePlayerDataDelegated(this.manager.getPlayerData());
    }

    public void leaveCampPointSynced(long pointId) {
        this.manager.getPlayerData().removeCamp(pointId);
        TeleportationMain.network().sendToServer(new SPLeaveCampPoint(pointId));
        if (pointId == this.manager.getPlayerData().getFavoriteCampId())
            this.manager.getPlayerData().setFavoriteCampId(0L);
        OxygenHelperClient.savePlayerDataDelegated(this.manager.getPlayerData());
        this.manager.getImagesManager().getPreviewImages().remove(pointId);
    }
}
