package austeretony.oxygen_teleportation.client;

import java.util.UUID;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData.EnumJumpProfile;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.server.SPChangeJumpProfile;
import austeretony.oxygen_teleportation.common.network.server.SPChangePointLockState;
import austeretony.oxygen_teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPEditWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPLeaveCampPoint;
import austeretony.oxygen_teleportation.common.network.server.SPManageInvitation;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToFavoriteCamp;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToPlayer;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToPoint;
import austeretony.oxygen_teleportation.common.network.server.SPRemoveWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPRequestInvitationsSync;
import austeretony.oxygen_teleportation.common.network.server.SPSetFavoriteCamp;

public class PlayerDataManagerClient {

    private final TeleportationManagerClient manager;

    protected PlayerDataManagerClient(TeleportationManagerClient manager) {
        this.manager = manager;
    }

    public void updateCooldown(int campCooldownLeftSeconds, int locationCooldownLeftSeconds, int jumpCooldownLeftSeconds) {
        this.manager.getPlayerData().getCooldownData().updateCooldown(campCooldownLeftSeconds, locationCooldownLeftSeconds, jumpCooldownLeftSeconds);
    }

    public void additionalDataReceived(int campCooldownLeftSeconds, int locationCooldownLeftSeconds, int jumpCooldownLeftSeconds, long favoriteCampId, long invitationsId) {
        this.updateCooldown(campCooldownLeftSeconds, locationCooldownLeftSeconds, jumpCooldownLeftSeconds);
        this.manager.getTeleportationMenuManager().cooldownSynchronized();
        this.manager.getPlayerData().setFavoriteCampId(favoriteCampId);
        if (invitationsId != 0L &&
                this.manager.getSharedCampsContainer().getInvitationsContainer().getId() != invitationsId)
            OxygenMain.network().sendToServer(new SPRequestInvitationsSync());
    }

    public static EnumJumpProfile getPlayerJumpProfile(UUID playerUUID) {
        return EnumJumpProfile.values()[OxygenHelperClient.getPlayerSharedData(playerUUID).getByte(TeleportationMain.JUMP_PROFILE_SHARED_DATA_ID)];
    }

    public void changeJumpProfileSynced(EnumJumpProfile profile) {
        OxygenMain.network().sendToServer(new SPChangeJumpProfile(profile));
    }

    public void moveToPlayerSynced(int index) {
        OxygenMain.network().sendToServer(new SPMoveToPlayer(index));
    }

    public void moveToCampSynced(long id) {        
        OxygenMain.network().sendToServer(new SPMoveToPoint(EnumWorldPoint.CAMP, id));
    }

    public void moveToFavoriteCampSynced() {        
        OxygenMain.network().sendToServer(new SPMoveToFavoriteCamp(this.manager.getPlayerData().getFavoriteCampId()));
    }   

    public void createCampPointSynced(String name, String description) {
        OxygenMain.network().sendToServer(new SPCreateWorldPoint(EnumWorldPoint.CAMP, name, description));
    }

    public void removeCampPointSynced(long pointId) {
        OxygenMain.network().sendToServer(new SPRemoveWorldPoint(EnumWorldPoint.CAMP, pointId));
    }

    public void setFavoriteCampSynced(long pointId) {        
        OxygenMain.network().sendToServer(new SPSetFavoriteCamp(pointId));
    }

    public void lockCampSynced(long pointId, boolean flag) {    
        OxygenMain.network().sendToServer(new SPChangePointLockState(EnumWorldPoint.CAMP, pointId, flag));
    }

    public void editCampPointSynced(long pointId, String name, String description, boolean updatePosition, boolean updateImage) {
        OxygenMain.network().sendToServer(new SPEditWorldPoint(EnumWorldPoint.CAMP, pointId, name, description, updatePosition, updateImage));
    }

    public void invitePlayerSynced(long pointId, UUID playerUUID) {
        OxygenMain.network().sendToServer(new SPManageInvitation(SPManageInvitation.EnumOperation.INVITE, pointId, playerUUID));
    }

    public void uninvitePlayerSynced(long pointId, UUID playerUUID) {
        OxygenMain.network().sendToServer(new SPManageInvitation(SPManageInvitation.EnumOperation.UNINVITE, pointId, playerUUID));
    }

    public void leaveCampPointSynced(long pointId) {
        OxygenMain.network().sendToServer(new SPLeaveCampPoint(pointId));
    }

    public void campCreated(WorldPoint worldPoint) {
        this.manager.getPlayerData().addCamp(worldPoint);
        this.manager.getPlayerData().setChanged(true);
        this.manager.getImagesManager().cacheLatestImage(worldPoint.getId());
        this.manager.getImagesLoader().saveLatestCampPreviewImageAsync(worldPoint.getId());
        this.manager.getImagesManager().uploadCampPreviewToServerAsync(worldPoint.getId());

        this.manager.getTeleportationMenuManager().campCreated(worldPoint);
    }

    public void campEdited(long oldPointId, WorldPoint worldPoint, boolean updateImage) {
        this.manager.getPlayerData().addCamp(worldPoint);
        if (this.manager.getPlayerData().getFavoriteCampId() == oldPointId)
            this.manager.getPlayerData().setFavoriteCampId(worldPoint.getId());
        this.manager.getPlayerData().removeCamp(oldPointId);
        this.manager.getPlayerData().setChanged(true);
        if (updateImage) {
            this.manager.getImagesManager().cacheLatestImage(worldPoint.getId());
            this.manager.getImagesManager().removeCachedImage(oldPointId);
            this.manager.getImagesManager().uploadCampPreviewToServerAsync(worldPoint.getId());
            this.manager.getImagesLoader().saveLatestCampPreviewImageAsync(worldPoint.getId());
        } else {
            this.manager.getImagesLoader().renameCampPreviewImageAsync(oldPointId, worldPoint.getId());
            this.manager.getImagesManager().replaceCachedImage(oldPointId, worldPoint.getId());
        }

        if (this.manager.getSharedCampsContainer().invitedPlayersExist(oldPointId))
            this.manager.getSharedCampsContainer().getInvitationsContainer().replace(oldPointId, worldPoint.getId());

        this.manager.getTeleportationMenuManager().campEdited(oldPointId, worldPoint, updateImage);
    }

    public void campRemoved(long pointId) {
        this.manager.getPlayerData().removeCamp(pointId);
        if (pointId == this.manager.getPlayerData().getFavoriteCampId())
            this.manager.getPlayerData().setFavoriteCampId(0L);
        this.manager.getPlayerData().setChanged(true);
        this.manager.getImagesManager().removeCachedImage(pointId);

        this.manager.getTeleportationMenuManager().campRemoved(pointId);
    }

    public void favoriteCampSet(long pointId) {
        this.manager.getPlayerData().setFavoriteCampId(pointId);
        this.manager.getPlayerData().setChanged(true);

        this.manager.getTeleportationMenuManager().favoriteCampSet(pointId);
    }

    public void playerUninvited(long pointId, UUID playerUUID) {
        this.manager.getSharedCampsContainer().uninvite(pointId, playerUUID);
        this.manager.getSharedCampsContainer().setChanged(true);

        this.manager.getTeleportationMenuManager().playerUninvited(pointId, playerUUID);
    }
}
