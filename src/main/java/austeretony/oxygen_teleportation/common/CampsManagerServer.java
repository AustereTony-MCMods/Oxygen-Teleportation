package austeretony.oxygen_teleportation.common;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.main.EnumOxygenChatMessage;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.CampInvitationRequest;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationChatMessage;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.main.TeleportationProcess;
import austeretony.oxygen_teleportation.common.main.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;

public class CampsManagerServer {

    private final TeleportationManagerServer manager;

    public CampsManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public void moveToCamp(EntityPlayerMP playerMP, long pointId) {
        if (TeleportationConfig.ENABLE_CAMPS.getBooleanValue()) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            if (this.campExist(playerUUID, pointId)) 
                this.move(this.getCamp(playerUUID, pointId), playerUUID, playerMP);
            else if (this.haveInvitation(playerUUID, pointId))
                this.move(this.manager.getSharedCampsManager().getCamp(pointId), playerUUID, playerMP);
        }
    }

    public void moveToFavoriteCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (TeleportationConfig.ENABLE_CAMPS.getBooleanValue() 
                && PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.ENABLE_FAVORITE_CAMP.toString(), TeleportationConfig.ENABLE_FAVORITE_CAMP.getBooleanValue())) {
            if (this.campExist(playerUUID, pointId)) 
                this.move(this.getCamp(playerUUID, pointId), playerUUID, playerMP);
            else if (this.haveInvitation(playerUUID, pointId))
                this.move(this.manager.getSharedCampsManager().getCamp(pointId), playerUUID, playerMP);
        }
    }

    private void move(WorldPoint worldPoint, UUID playerUUID, EntityPlayerMP playerMP) {
        if (this.campAvailable(worldPoint, playerUUID) 
                && !this.teleporting(playerUUID) 
                && this.readyMoveToCamp(playerUUID)) {
            if (!PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                    && playerMP.dimension != worldPoint.getDimensionId()) {
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
                return;
            }
            int delay = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.CAMP_TELEPORTATION_DELAY.toString(), TeleportationConfig.CAMPS_TELEPORT_DELAY.getIntValue());
            if (delay < 1)
                delay = 1;
            if (delay > 1)
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.PREPARE_FOR_TELEPORTATION.ordinal(), String.valueOf(delay));
            TeleportationProcess.create(TeleportationProcess.EnumTeleportation.CAMP, playerMP, worldPoint.getId(), delay);    
        }
    }

    public void createCamp(EntityPlayerMP playerMP, long pointId, String name, String description) {
        if (TeleportationConfig.ENABLE_CAMPS.getBooleanValue()) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            TeleportationPlayerData playerData = this.manager.getPlayerData(playerUUID);
            if (playerData.getCampsAmount() 
                    < PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue())) {
                WorldPoint worldPoint = new WorldPoint(
                        System.currentTimeMillis(),
                        playerUUID,
                        CommonReference.getName(playerMP), 
                        name, 
                        description,
                        playerMP.dimension,
                        (float) playerMP.posX, 
                        (float) playerMP.posY, 
                        (float) playerMP.posZ,
                        playerMP.rotationYawHead, 
                        playerMP.rotationPitch);
                worldPoint.setId(pointId);
                playerData.addCamp(worldPoint);
                TeleportationLoaderServer.savePersistentDataDelegated(playerData);
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.CAMP_CREATED.ordinal(), worldPoint.getName());
            }
        }
    }

    public void removeCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.campExist(playerUUID, pointId)
                && !OxygenHelperServer.isRequesting(playerUUID)) { 
            WorldPoint worldPoint = this.getCamp(playerUUID, pointId);
            if (this.isOwner(playerUUID, worldPoint)) {
                TeleportationPlayerData playerData = this.manager.getPlayerData(playerUUID);
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.CAMP_REMOVED.ordinal(), worldPoint.getName());
                playerData.removeCamp(pointId);
                if (pointId == playerData.getFavoriteCampId())
                    playerData.setFavoriteCampId(0L);
                this.manager.getImagesLoader().removeCampPreviewImageDelegated(playerUUID, pointId);   

                if (this.manager.getSharedCampsManager().haveInvitedPlayers(playerUUID, pointId)) {
                    this.manager.getSharedCampsManager().removeCamp(playerUUID, pointId);
                    TeleportationLoaderServer.savePersistentDataDelegated(this.manager.getSharedCampsManager());
                }

                TeleportationLoaderServer.savePersistentDataDelegated(playerData);
            }
        }
    }

    public void setFavoriteCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        TeleportationPlayerData playerData = this.manager.getPlayerData(playerUUID);
        if (TeleportationConfig.ENABLE_FAVORITE_CAMP.getBooleanValue() 
                && (this.campExist(playerUUID, pointId) || this.manager.getSharedCampsManager().haveInvitation(playerUUID, pointId))
                && pointId != playerData.getFavoriteCampId()) {
            playerData.setFavoriteCampId(pointId);
            TeleportationLoaderServer.savePersistentDataDelegated(playerData);
        }
    }

    public void lockCamp(EntityPlayerMP playerMP, long oldPointId, boolean flag) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.campExist(playerUUID, oldPointId)
                && !OxygenHelperServer.isRequesting(playerUUID)) { 
            WorldPoint worldPoint = this.getCamp(playerUUID, oldPointId);
            if (this.isOwner(playerUUID, worldPoint)) {
                TeleportationPlayerData playerData = this.manager.getPlayerData(playerUUID);
                worldPoint.setLocked(flag);
                worldPoint.setId(oldPointId + 1L);
                playerData.addCamp(worldPoint);
                if (playerData.getFavoriteCampId() == oldPointId)
                    playerData.setFavoriteCampId(worldPoint.getId());
                playerData.removeCamp(oldPointId);
                this.manager.getImagesLoader().renameCampPreviewImageDelegated(playerUUID, oldPointId, worldPoint.getId());

                if (this.manager.getSharedCampsManager().haveInvitedPlayers(playerUUID, oldPointId)) {
                    this.manager.getSharedCampsManager().replaceCamp(playerUUID, oldPointId, worldPoint);
                    TeleportationLoaderServer.savePersistentDataDelegated(this.manager.getSharedCampsManager());
                }

                TeleportationLoaderServer.savePersistentDataDelegated(playerData);
                if (flag)
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.CAMP_LOCKED.ordinal(), worldPoint.getName());
                else
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.CAMP_UNLOCKED.ordinal(), worldPoint.getName());
            }
        }
    }

    public void editCamp(EntityPlayerMP playerMP, long oldPointId, String name, String description, boolean updateName, 
            boolean updateDescription, boolean updateImage, boolean updatePosition) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.campExist(playerUUID, oldPointId)
                && !OxygenHelperServer.isRequesting(playerUUID)) { 
            WorldPoint worldPoint = this.getCamp(playerUUID, oldPointId);
            if (this.isOwner(playerUUID, worldPoint)) {
                TeleportationPlayerData playerData = this.manager.getPlayerData(playerUUID);
                long newPointId = oldPointId + 1L;
                boolean edited = false;
                if (updateName)
                    worldPoint.setName(name);
                if (updateDescription)
                    worldPoint.setDescription(description);
                if (updateImage)
                    this.manager.getImagesLoader().removeCampPreviewImage(playerUUID, oldPointId);
                if (updatePosition)
                    worldPoint.setPosition(playerMP.rotationYaw, playerMP.rotationPitch, (float) playerMP.posX, (float) playerMP.posY, (float) playerMP.posZ, playerMP.dimension);
                edited = updateName || updateDescription || updateImage || updatePosition;
                if (edited) {
                    worldPoint.setId(newPointId);
                    playerData.addCamp(worldPoint);
                    if (playerData.getFavoriteCampId() == oldPointId)
                        playerData.setFavoriteCampId(newPointId);
                    playerData.removeCamp(oldPointId);
                    if (!updateImage)
                        this.manager.getImagesLoader().renameCampPreviewImageDelegated(playerUUID, oldPointId, newPointId);

                    if (this.manager.getSharedCampsManager().haveInvitedPlayers(playerUUID, oldPointId)) {
                        this.manager.getSharedCampsManager().replaceCamp(playerUUID, oldPointId, worldPoint);
                        TeleportationLoaderServer.savePersistentDataDelegated(this.manager.getSharedCampsManager());
                    }

                    TeleportationLoaderServer.savePersistentDataDelegated(playerData);
                }
            }
        }
    }

    public void invitePlayer(EntityPlayerMP playerMP, long pointId, UUID playerUUID) {
        UUID ownerUUID = CommonReference.getPersistentUUID(playerMP);
        if (TeleportationConfig.ENABLE_CAMP_INVITATIONS.getBooleanValue() 
                && this.campExist(ownerUUID, pointId)) { 
            WorldPoint worldPoint = this.getCamp(ownerUUID, pointId);
            if (this.isOwner(ownerUUID, worldPoint) 
                    && OxygenHelperServer.isOnline(playerUUID)
                    && !this.manager.getSharedCampsManager().haveInvitation(playerUUID, pointId)
                    && !playerUUID.equals(ownerUUID)) {
                if (this.manager.getSharedCampsManager().getInvitedPlayersAmountForCamp(playerUUID, pointId) < TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.getIntValue()) {
                    EntityPlayerMP invitedPlayerMP = CommonReference.playerByUUID(playerUUID);
                    OxygenHelperServer.sendRequest(playerMP, invitedPlayerMP, 
                            new CampInvitationRequest(
                                    TeleportationMain.INVITATION_TO_CAMP_ID,
                                    ownerUUID, 
                                    CommonReference.getName(playerMP), 
                                    pointId, 
                                    worldPoint.getName()), true);
                } else
                    OxygenHelperServer.sendMessage(playerMP, OxygenMain.OXYGEN_MOD_INDEX, EnumOxygenChatMessage.REQUEST_RESET.ordinal());
            }
        }
    }

    public void uninvitePlayer(EntityPlayerMP playerMP, long pointId, UUID playerUUID) {
        UUID ownerUUID = CommonReference.getPersistentUUID(playerMP);
        if (TeleportationConfig.ENABLE_CAMP_INVITATIONS.getBooleanValue() 
                && this.campExist(ownerUUID, pointId)) {
            WorldPoint worldPoint = this.getCamp(ownerUUID, pointId);
            if (this.isOwner(ownerUUID, worldPoint)
                    && this.manager.getSharedCampsManager().haveInvitation(playerUUID, pointId)) {
                this.manager.getSharedCampsManager().uninvite(ownerUUID, pointId, playerUUID);
                TeleportationLoaderServer.savePersistentDataDelegated(this.manager.getSharedCampsManager());

                OxygenHelperServer.removeObservedPlayer(ownerUUID, playerUUID, true);

                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.UNINVITED.ordinal());
            }
        }
    }

    public void leaveCamp(EntityPlayerMP playerMP, long pointId) {
        UUID 
        playerUUID = CommonReference.getPersistentUUID(playerMP),
        ownerUUID;
        if (this.manager.getSharedCampsManager().haveInvitation(playerUUID, pointId)) {
            ownerUUID = this.manager.getSharedCampsManager().getCampOwner(pointId);            
            this.manager.getSharedCampsManager().uninvite(ownerUUID, pointId, playerUUID);
            TeleportationLoaderServer.savePersistentDataDelegated(this.manager.getSharedCampsManager());

            OxygenHelperServer.removeObservedPlayer(ownerUUID, playerUUID, true);

            TeleportationPlayerData playerData =  this.manager.getPlayerData(playerUUID);
            if (playerData.getFavoriteCampId() == pointId)
                playerData.setFavoriteCampId(0L);
            TeleportationLoaderServer.savePersistentDataDelegated(playerData);                
            OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.CAMP_LEFT.ordinal(), this.manager.getSharedCampsManager().getCamp(pointId).getName());
        }
    }

    private boolean campExist(UUID playerUUID, long pointId) {
        return this.manager.getPlayerData(playerUUID).campExist(pointId);
    }

    private boolean haveInvitation(UUID playerUUID, long pointId) {
        return this.manager.getSharedCampsManager().haveInvitation(playerUUID, pointId) 
                && this.manager.getSharedCampsManager().campExist(pointId);
    }

    private WorldPoint getCamp(UUID playerUUID, long pointId) {
        return this.manager.getPlayerData(playerUUID).getCamp(pointId);
    }

    private boolean campAvailable(WorldPoint worldPoint, UUID playerUUID) {       
        return !worldPoint.isLocked() || worldPoint.isOwner(playerUUID);
    }

    private boolean isOwner(UUID playerUUID, WorldPoint worldPoint) {
        return worldPoint.isOwner(playerUUID);
    }

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToCamp(UUID playerUUID) {
        return System.currentTimeMillis() - this.manager.getPlayerData(playerUUID).getCooldownInfo().getLastCampTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.CAMPS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}
