package austeretony.teleportation.common;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.camps.InvitationRequest;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumChatMessages;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;

public class CampsManagerServer {

    private final TeleportationManagerServer manager;

    private final Set<UUID> invitingPlayers = new HashSet<UUID>();//for preventing invitations spam, only one request until reply or expire

    public CampsManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public Set<UUID> getInvitingPlayers() {
        return this.invitingPlayers;
    }

    public boolean playerInviting(UUID playerUUID) {
        return this.invitingPlayers.contains(playerUUID);
    }

    public void setInviting(UUID playerUUID) {
        this.invitingPlayers.add(playerUUID);
    }

    public void resetInviting(UUID playerUUID) {
        this.invitingPlayers.remove(playerUUID);
    }

    public void moveToCamp(EntityPlayerMP playerMP, long pointId) {
        if (TeleportationConfig.ENABLE_CAMPS.getBooleanValue()) {
            UUID playerUUID = CommonReference.uuid(playerMP);
            if (this.campExistAndOwnedBy(playerUUID, pointId)) 
                this.move(this.getCamp(playerUUID, pointId), playerUUID, playerMP);
            else if (this.campExistAndSharedWith(playerUUID, pointId)) {
                UUID otherUUID = this.manager.getPlayerProfile(playerUUID).getOtherCampOwner(pointId);
                if (this.manager.profileExist(otherUUID))
                    this.move(this.getCamp(otherUUID, pointId), playerUUID, playerMP);
                else {
                    this.manager.getCampsLoader().loadPlayerData(otherUUID);//TODO IO operation... this is not good - need reliable solution
                    this.move(this.getCamp(otherUUID, pointId), playerUUID, playerMP);
                }
            }
        }
    }

    private void move(WorldPoint worldPoint, UUID playerUUID, EntityPlayerMP playerMP) {
        if (this.campAvailable(worldPoint, playerUUID) && !this.teleporting(playerUUID) && this.readyMoveToCamp(playerUUID)) {
            if (!PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                    && playerMP.dimension != worldPoint.getDimensionId()) {
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
                return;
            }
            int delay = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.CAMP_TELEPORTATION_DELAY.toString(), TeleportationConfig.CAMPS_TELEPORT_DELAY.getIntValue());
            if (delay < 1)
                delay = 1;
            if (delay > 1)
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.PREPARE_FOR_TELEPORTATION.ordinal(), String.valueOf(delay));
            TeleportationProcess.create(TeleportationProcess.EnumTeleportations.CAMP, playerMP, worldPoint.getId(), delay);    
        }
    }

    public void createCamp(EntityPlayerMP playerMP, long pointId, String name, String description) {
        if (TeleportationConfig.ENABLE_CAMPS.getBooleanValue()) {
            UUID playerUUID = CommonReference.uuid(playerMP);
            if (this.manager.getPlayerProfile(playerUUID).getCampsAmount() 
                    < PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue())) {
                WorldPoint worldPoint = new WorldPoint(
                        playerUUID,
                        CommonReference.username(playerMP), 
                        name, 
                        description,
                        playerMP.dimension,
                        (float) playerMP.posX, 
                        (float) playerMP.posY, 
                        (float) playerMP.posZ,
                        playerMP.rotationYawHead, 
                        playerMP.rotationPitch);
                worldPoint.setId(pointId);
                this.manager.getPlayerProfile(playerUUID).addCamp(worldPoint);
                this.manager.getCampsLoader().savePlayerDataDelegated(playerUUID);
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CAMP_CREATED.ordinal(), worldPoint.getName());
            }
        }
    }

    public void removeCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.uuid(playerMP);
        if (this.campExistAndOwnedBy(playerUUID, pointId)) { 
            WorldPoint worldPoint = this.getCamp(playerUUID, pointId);
            if (this.owner(playerUUID, worldPoint)) {
                PlayerProfile 
                playerProfile = this.manager.getPlayerProfile(playerUUID),
                invitedProfile;
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CAMP_REMOVED.ordinal(), worldPoint.getName());
                playerProfile.removeCamp(pointId);
                if (pointId == playerProfile.getFavoriteCampId())
                    playerProfile.setFavoriteCampId(0L);
                this.manager.getImagesLoader().removeCampPreviewImageDelegated(playerUUID, pointId);   

                if (playerProfile.haveInvitedPlayers(pointId))
                    for (UUID invitedUUID : playerProfile.getInvitedPlayers().get(pointId).getPlayers()) {
                        playerProfile.uninviteFromCamp(pointId, invitedUUID);
                        if (!this.manager.profileExist(invitedUUID)) {
                            this.manager.createPlayerProfile(invitedUUID);
                            this.manager.getCampsLoader().loadPlayerData(invitedUUID);//TODO IO operation... this is not good - need reliable solution
                        }
                        invitedProfile =  this.manager.getPlayerProfile(invitedUUID);
                        invitedProfile.removeOtherCamp(pointId);
                        if (invitedProfile.getFavoriteCampId() == pointId)
                            invitedProfile.setFavoriteCampId(0L);
                        this.manager.getCampsLoader().savePlayerDataDelegated(invitedUUID);
                    }

                this.manager.getCampsLoader().savePlayerDataDelegated(playerUUID);
            }
        }
    }

    public void setFavoriteCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.uuid(playerMP);
        if (TeleportationConfig.ENABLE_FAVORITE_CAMP.getBooleanValue() && this.campExistAndOwnedBy(playerUUID, pointId) && pointId != this.manager.getPlayerProfile(playerUUID).getFavoriteCampId()) {
            this.manager.getPlayerProfile(playerUUID).setFavoriteCampId(pointId);
            this.manager.getCampsLoader().savePlayerDataDelegated(playerUUID);
            OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.SET_FAVORITE.ordinal(), this.manager.getPlayerProfile(playerUUID).getCamp(pointId).getName());
        }
    }

    public void lockCamp(EntityPlayerMP playerMP, long oldPointId, boolean flag) {
        UUID playerUUID = CommonReference.uuid(playerMP);
        if (this.campExistAndOwnedBy(playerUUID, oldPointId)) { 
            WorldPoint worldPoint = this.getCamp(playerUUID, oldPointId);
            if (this.owner(playerUUID, worldPoint)) {
                PlayerProfile 
                playerProfile = this.manager.getPlayerProfile(playerUUID),
                invitedProfile;
                worldPoint.setLocked(flag);
                worldPoint.setId(oldPointId + 1L);
                playerProfile.addCamp(worldPoint);
                if (playerProfile.getFavoriteCampId() == oldPointId)
                    playerProfile.setFavoriteCampId(worldPoint.getId());
                playerProfile.removeCamp(oldPointId);
                this.manager.getImagesLoader().renameCampPreviewImageDelegated(playerUUID, oldPointId, worldPoint.getId());

                if (playerProfile.haveInvitedPlayers(oldPointId))
                    for (UUID invitedUUID : playerProfile.getInvitedPlayers().get(oldPointId).getPlayers()) {
                        playerProfile.inviteToCamp(worldPoint.getId(), invitedUUID, playerProfile.getSharedCamps().get(invitedUUID).username);
                        playerProfile.uninviteFromCamp(oldPointId, invitedUUID);
                        if (!this.manager.profileExist(invitedUUID)) {
                            this.manager.createPlayerProfile(invitedUUID);
                            this.manager.getCampsLoader().loadPlayerData(invitedUUID);//TODO IO operation... this is not good - need reliable solution
                        }
                        invitedProfile =  this.manager.getPlayerProfile(invitedUUID);
                        invitedProfile.removeOtherCamp(oldPointId);
                        invitedProfile.addOtherCamp(worldPoint.getId(), playerUUID);
                        if (invitedProfile.getFavoriteCampId() == oldPointId)
                            invitedProfile.setFavoriteCampId(worldPoint.getId());
                        this.manager.getCampsLoader().savePlayerDataDelegated(invitedUUID);
                    }

                this.manager.getCampsLoader().savePlayerDataDelegated(playerUUID);
                if (flag)
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CAMP_LOCKED.ordinal(), worldPoint.getName());
                else
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CAMP_UNLOCKED.ordinal(), worldPoint.getName());
            }
        }
    }

    public void editCamp(EntityPlayerMP playerMP, long oldPointId, String name, String description, boolean updateName, 
            boolean updateDescription, boolean updateImage, boolean updatePosition) {
        UUID playerUUID = CommonReference.uuid(playerMP);
        if (this.campExistAndOwnedBy(playerUUID, oldPointId)) { 
            WorldPoint worldPoint = this.getCamp(playerUUID, oldPointId);
            if (this.owner(playerUUID, worldPoint)) {
                PlayerProfile 
                playerProfile = this.manager.getPlayerProfile(playerUUID),
                invitedProfile;
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
                    playerProfile.addCamp(worldPoint);
                    if (playerProfile.getFavoriteCampId() == oldPointId)
                        playerProfile.setFavoriteCampId(newPointId);
                    playerProfile.removeCamp(oldPointId);
                    if (!updateImage)
                        this.manager.getImagesLoader().renameCampPreviewImageDelegated(playerUUID, oldPointId, newPointId);

                    if (playerProfile.haveInvitedPlayers(oldPointId))
                        for (UUID invitedUUID : playerProfile.getInvitedPlayers().get(oldPointId).getPlayers()) {
                            playerProfile.inviteToCamp(worldPoint.getId(), invitedUUID, playerProfile.getSharedCamps().get(invitedUUID).username);
                            playerProfile.uninviteFromCamp(oldPointId, invitedUUID);
                            if (!this.manager.profileExist(invitedUUID)) {
                                this.manager.createPlayerProfile(invitedUUID);
                                this.manager.getCampsLoader().loadPlayerData(invitedUUID);//TODO IO operation... this is not good - need reliable solution
                            }
                            invitedProfile =  this.manager.getPlayerProfile(invitedUUID);
                            invitedProfile.removeOtherCamp(oldPointId);
                            invitedProfile.addOtherCamp(worldPoint.getId(), playerUUID);
                            if (invitedProfile.getFavoriteCampId() == oldPointId)
                                invitedProfile.setFavoriteCampId(worldPoint.getId());
                            this.manager.getCampsLoader().savePlayerDataDelegated(invitedUUID);
                        }

                    this.manager.getCampsLoader().savePlayerDataDelegated(playerUUID);
                }
            }
        }
    }

    public void invitePlayer(EntityPlayerMP playerMP, long pointId, UUID playerUUID) {
        UUID ownerUUID = CommonReference.uuid(playerMP);
        if (this.campExistAndOwnedBy(ownerUUID, pointId)) { 
            WorldPoint worldPoint = this.getCamp(ownerUUID, pointId);
            if (this.owner(ownerUUID, worldPoint) && OxygenHelperServer.isOnline(playerUUID) && !playerUUID.equals(ownerUUID)) {
                if (this.manager.getPlayerProfile(playerUUID).getInvitedPlayersAmount(pointId) < TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.getIntValue()) {
                    if (!this.playerInviting(ownerUUID)) {                   
                        this.setInviting(ownerUUID);
                        EntityPlayerMP invitedPlayerMP = CommonReference.playerByUUID(playerUUID);
                        OxygenHelperServer.addNotification(invitedPlayerMP, 
                                new InvitationRequest(
                                        1,//invitation request index
                                        playerUUID, 
                                        ownerUUID, 
                                        CommonReference.username(playerMP), 
                                        pointId, 
                                        worldPoint.getName()));
                        OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.INVITATION_REQUEST_SENT.ordinal(), worldPoint.getName(), CommonReference.username(invitedPlayerMP));
                    } else
                        OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.INVITATION_REQUEST_RESET.ordinal());
                }
            }
        }
    }

    public void uninvitePlayer(EntityPlayerMP playerMP, long pointId, UUID playerUUID) {
        UUID ownerUUID = CommonReference.uuid(playerMP);
        if (this.campExistAndOwnedBy(ownerUUID, pointId)) {
            WorldPoint worldPoint = this.getCamp(ownerUUID, pointId);
            if (this.owner(ownerUUID, worldPoint)) {
                PlayerProfile 
                playerProfile = this.manager.getPlayerProfile(ownerUUID),
                invitedProfile =  this.manager.getPlayerProfile(playerUUID);
                playerProfile.uninviteFromCamp(pointId, playerUUID);
                if (!this.manager.profileExist(playerUUID)) {
                    this.manager.createPlayerProfile(playerUUID);
                    this.manager.getCampsLoader().loadPlayerData(playerUUID);//TODO IO operation... this is not good - need reliable solution
                }
                invitedProfile.removeOtherCamp(pointId);
                if (invitedProfile.getFavoriteCampId() == pointId)
                    invitedProfile.setFavoriteCampId(0L);
                this.manager.getCampsLoader().savePlayerDataDelegated(ownerUUID);
                this.manager.getCampsLoader().savePlayerDataDelegated(playerUUID);
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.UNINVITED.ordinal());
            }
        }
    }

    public void leaveCamp(EntityPlayerMP playerMP, long pointId) {
        UUID 
        playerUUID = CommonReference.uuid(playerMP),
        ownerUUID;
        if (this.campExistAndSharedWith(playerUUID, pointId)) {
            ownerUUID = this.manager.getPlayerProfile(playerUUID).getOtherCampOwner(pointId);
            WorldPoint worldPoint = this.getCamp(ownerUUID, pointId);
            if (this.owner(ownerUUID, worldPoint)) {
                PlayerProfile invitedProfile =  this.manager.getPlayerProfile(playerUUID);
                invitedProfile.removeOtherCamp(pointId);
                if (!this.manager.profileExist(ownerUUID)) {
                    this.manager.createPlayerProfile(ownerUUID);
                    this.manager.getCampsLoader().loadPlayerData(ownerUUID);//TODO IO operation... this is not good - need reliable solution
                }
                this.manager.getPlayerProfile(ownerUUID).uninviteFromCamp(pointId, playerUUID);
                if (invitedProfile.getFavoriteCampId() == pointId)
                    invitedProfile.setFavoriteCampId(0L);
                this.manager.getCampsLoader().savePlayerDataDelegated(ownerUUID);
                this.manager.getCampsLoader().savePlayerDataDelegated(playerUUID);
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CAMP_LEFT.ordinal(), worldPoint.getName());
            }
        }
    }

    private boolean campExistAndOwnedBy(UUID playerUUID, long pointId) {
        return this.manager.getPlayerProfile(playerUUID).campExist(pointId);
    }

    private boolean campExistAndSharedWith(UUID playerUUID, long pointId) {
        return this.manager.getPlayerProfile(playerUUID).isOtherCamp(pointId);
    }

    private WorldPoint getCamp(UUID playerUUID, long pointId) {
        return this.manager.getPlayerProfile(playerUUID).getCamp(pointId);
    }

    private boolean campAvailable(WorldPoint worldPoint, UUID playerUUID) {       
        return !worldPoint.isLocked() || worldPoint.isOwner(playerUUID);
    }

    private boolean owner(UUID playerUUID, WorldPoint worldPoint) {
        return worldPoint.isOwner(playerUUID);
    }

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToCamp(UUID playerUUID) {
        return System.currentTimeMillis() - this.manager.getPlayerProfile(playerUUID).getCooldownInfo().getLastCampTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.CAMP_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.CAMPS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}