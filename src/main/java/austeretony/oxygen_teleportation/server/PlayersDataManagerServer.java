package austeretony.oxygen_teleportation.server;

import java.util.UUID;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.EnumOxygenStatusMessage;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.TimeHelperServer;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData.EnumJumpProfile;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPFavoriteCampUpdated;
import austeretony.oxygen_teleportation.common.network.client.CPPlayerUninvited;
import austeretony.oxygen_teleportation.common.network.client.CPSyncFeeItemStack;
import austeretony.oxygen_teleportation.common.network.client.CPSyncInvitedPlayers;
import austeretony.oxygen_teleportation.common.network.client.CPWorldPointCreated;
import austeretony.oxygen_teleportation.common.network.client.CPWorldPointEdited;
import austeretony.oxygen_teleportation.common.network.client.CPWorldPointRemoved;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayersDataManagerServer {

    private final TeleportationManagerServer manager;

    protected PlayersDataManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public boolean isPlayerTeleporting(UUID playerUUID) {
        return OxygenHelperServer.getOxygenPlayerData(playerUUID).getTemporaryProcess(AbstractTeleportation.TELEPORTATION_PROCESS_ID) != null;
    }

    public void addTeleportation(AbstractTeleportation teleportation) {
        OxygenHelperServer.getOxygenPlayerData(CommonReference.getPersistentUUID(teleportation.playerMP)).addTemporaryProcess(teleportation);
    }

    public void playerLoaded(EntityPlayerMP playerMP) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP); 
        TeleportationPlayerData playerData = this.manager.getPlayersDataContainer().getPlayerData(playerUUID);
        if (playerData == null) {
            playerData = this.manager.getPlayersDataContainer().createPlayerData(playerUUID);
            OxygenHelperServer.loadPersistentData(playerData);  
        }
        if (TeleportationConfig.FEE_MODE.asInt() == 1)
            OxygenMain.network().sendTo(new CPSyncFeeItemStack(), playerMP);
        OxygenHelperServer.getPlayerSharedData(playerUUID).setByte(TeleportationMain.JUMP_PROFILE_SHARED_DATA_ID, playerData.getJumpProfile().ordinal());
    }

    public void changeJumpProfile(EntityPlayerMP playerMP, EnumJumpProfile profile) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_PLAYER_TELEPORTATION_USAGE.id(), TeleportationConfig.ENABLE_PLAYER_TELEPORTATION.asBoolean())) {
            TeleportationPlayerData playerData = this.manager.getPlayersDataContainer().getPlayerData(playerUUID);
            playerData.setJumpProfile(profile);
            playerData.setChanged(true);     

            OxygenHelperServer.getPlayerSharedData(playerUUID).setByte(TeleportationMain.JUMP_PROFILE_SHARED_DATA_ID, profile.ordinal());

            OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.JUMP_PROFILE_CHANGED.ordinal());
        }
    }

    public void moveToPlayer(EntityPlayerMP visitorPlayerMP, int targetIndex) {
        UUID 
        visitorUUID = CommonReference.getPersistentUUID(visitorPlayerMP),
        targetUUID;
        if (PrivilegesProviderServer.getAsBoolean(visitorUUID, EnumTeleportationPrivilege.ALLOW_PLAYER_TELEPORTATION_USAGE.id(), TeleportationConfig.ENABLE_PLAYER_TELEPORTATION.asBoolean())
                && !this.isPlayerTeleporting(visitorUUID) 
                && this.readyMoveToPlayer(visitorUUID)
                && OxygenHelperServer.isPlayerOnline(targetIndex)) { 
            targetUUID = OxygenHelperServer.getPlayerSharedData(targetIndex).getPlayerUUID();
            if (!visitorUUID.equals(targetUUID)) {
                EnumJumpProfile targetJumpProfile = this.manager.getPlayersDataContainer().getPlayerData(targetUUID).getJumpProfile();
                switch (targetJumpProfile) {
                case DISABLED:
                    if (PrivilegesProviderServer.getAsBoolean(visitorUUID, EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.id(), false))
                        this.move(visitorPlayerMP, visitorUUID, targetUUID);
                    break;
                case FREE:
                    this.move(visitorPlayerMP, visitorUUID, targetUUID);
                    break;
                case REQUEST:
                    if (PrivilegesProviderServer.getAsBoolean(visitorUUID, EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.id(), false))
                        this.move(visitorPlayerMP, visitorUUID, targetUUID);
                    else {
                        EntityPlayerMP targetPlayerMP = CommonReference.playerByUUID(targetUUID);
                        OxygenHelperServer.sendRequest(visitorPlayerMP, targetPlayerMP, 
                                new TeleportationRequest(TeleportationMain.TELEPORTATION_REQUEST_ID, visitorUUID, CommonReference.getName(visitorPlayerMP)));
                    }
                    break;
                }  
            } else
                OxygenHelperServer.sendStatusMessage(visitorPlayerMP, OxygenMain.OXYGEN_CORE_MOD_INDEX, EnumOxygenStatusMessage.REQUEST_RESET.ordinal());
        } else
            OxygenHelperServer.sendStatusMessage(visitorPlayerMP, OxygenMain.OXYGEN_CORE_MOD_INDEX, EnumOxygenStatusMessage.REQUEST_RESET.ordinal());
    }

    public void move(EntityPlayerMP visitorPlayerMP, UUID visitorUUID, UUID targetUUID) {
        EntityPlayerMP targetPlayerMP = CommonReference.playerByUUID(targetUUID);
        if (!PrivilegesProviderServer.getAsBoolean(visitorUUID, EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.id(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.asBoolean())
                && visitorPlayerMP.dimension != targetPlayerMP.dimension) {
            OxygenHelperServer.sendStatusMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
            return;
        }
        this.addTeleportation(new PlayerTeleportation(visitorPlayerMP, targetPlayerMP)); 
    }  

    private boolean readyMoveToPlayer(UUID playerUUID) {
        return System.currentTimeMillis() >= this.manager.getPlayersDataContainer().getPlayerData(playerUUID).getCooldownData().getNextJumpTime();
    }

    public void moveToCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean())) {
            WorldPoint camp = this.getCamp(playerUUID, pointId);
            if (camp != null)
                this.move(camp, playerUUID, playerMP);
            else if (this.haveInvitation(playerUUID, pointId))
                this.move(this.manager.getSharedCampsContainer().getCamp(pointId), playerUUID, playerMP);
        }
    }

    public void moveToFavoriteCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean())
                && PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ENABLE_FAVORITE_CAMP.id(), TeleportationConfig.ENABLE_FAVORITE_CAMP.asBoolean())) {
            WorldPoint camp = this.getCamp(playerUUID, pointId);
            if (camp != null)
                this.move(camp, playerUUID, playerMP);
            else if (this.haveInvitation(playerUUID, pointId))
                this.move(this.manager.getSharedCampsContainer().getCamp(pointId), playerUUID, playerMP);
        }
    }

    private void move(WorldPoint worldPoint, UUID playerUUID, EntityPlayerMP playerMP) {
        if (this.campAvailable(worldPoint, playerUUID) 
                && !this.isPlayerTeleporting(playerUUID) 
                && this.readyMoveToCamp(playerUUID)) {
            if (!PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.id(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.asBoolean())
                    && playerMP.dimension != worldPoint.getDimensionId()) {
                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
                return;
            }
            this.addTeleportation(new CampTeleportation(playerMP, worldPoint));
        }
    }

    public void createCamp(EntityPlayerMP playerMP, String name, String description) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean())) {
            TeleportationPlayerData playerData = this.manager.getPlayersDataContainer().getPlayerData(playerUUID);
            if (playerData.getCampsAmount() 
                    < PrivilegesProviderServer.getAsInt(playerUUID, EnumTeleportationPrivilege.CAMPS_MAX_AMOUNT.id(), TeleportationConfig.CAMPS_MAX_AMOUNT.asInt())) {
                if (name.isEmpty())
                    name = String.format("Camp #%d", playerData.getCampsAmount() + 1);
                name = name.trim();
                if (name.length() > WorldPoint.MAX_NAME_LENGTH)
                    name = name.substring(0, WorldPoint.MAX_NAME_LENGTH);
                if (name.isEmpty())
                    name = String.format("Camp #%d", playerData.getCampsAmount() + 1);

                description = description.trim();
                if (description.length() > WorldPoint.MAX_DESCRIPTION_LENGTH)
                    description = description.substring(0, WorldPoint.MAX_NAME_LENGTH);

                WorldPoint camp = new WorldPoint(
                        playerData.createId(TimeHelperServer.getCurrentMillis()),
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
                playerData.addCamp(camp);
                playerData.setChanged(true);

                OxygenMain.network().sendTo(new CPWorldPointCreated(EnumWorldPoint.CAMP, camp), playerMP);

                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CAMP_CREATED.ordinal());
            }
        }
    }

    public void removeCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean())) { 
            WorldPoint camp = this.getCamp(playerUUID, pointId);
            if (camp != null
                    && camp.isOwner(playerUUID)) {
                TeleportationPlayerData playerData = this.manager.getPlayersDataContainer().getPlayerData(playerUUID);
                playerData.removeCamp(pointId);
                playerData.setChanged(true);

                this.manager.getImagesLoader().removeCampPreviewImageAsync(playerUUID, pointId);   

                if (this.manager.getSharedCampsContainer().haveInvitedPlayers(playerUUID, pointId)) {
                    this.manager.getSharedCampsContainer().removeCamp(playerUUID, pointId);
                    this.manager.getSharedCampsContainer().setChanged(true);
                }

                OxygenMain.network().sendTo(new CPWorldPointRemoved(EnumWorldPoint.CAMP, camp.getId()), playerMP);

                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CAMP_REMOVED.ordinal());
            }
        }
    }

    public void setFavoriteCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        TeleportationPlayerData playerData = this.manager.getPlayersDataContainer().getPlayerData(playerUUID);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean())
                && PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ENABLE_FAVORITE_CAMP.id(), TeleportationConfig.ENABLE_FAVORITE_CAMP.asBoolean()) 
                && (playerData.getCamp(pointId) != null || this.manager.getSharedCampsContainer().haveInvitation(playerUUID, pointId))
                && pointId != playerData.getFavoriteCampId()) {
            playerData.setFavoriteCampId(pointId);
            playerData.setChanged(true);

            OxygenMain.network().sendTo(new CPFavoriteCampUpdated(pointId), playerMP);

            OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.FAVORITE_CAMP_SET.ordinal());
        }
    }

    public void changeCampLockState(EntityPlayerMP playerMP, long pointId, boolean flag) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean())) { 
            WorldPoint camp = this.getCamp(playerUUID, pointId);
            if (camp != null
                    && camp.isOwner(playerUUID)) {
                TeleportationPlayerData playerData = this.manager.getPlayersDataContainer().getPlayerData(playerUUID);
                camp.setLocked(flag);
                camp.setId(playerData.createId(pointId));
                playerData.addCamp(camp);
                if (playerData.getFavoriteCampId() == pointId)
                    playerData.setFavoriteCampId(camp.getId());
                playerData.removeCamp(pointId);
                playerData.setChanged(true);

                this.manager.getImagesLoader().renameCampPreviewImageAsync(playerUUID, pointId, camp.getId());

                if (this.manager.getSharedCampsContainer().haveInvitedPlayers(playerUUID, pointId)) {
                    this.manager.getSharedCampsContainer().replaceCamp(playerUUID, pointId, camp);
                    this.manager.getSharedCampsContainer().setChanged(true);
                }

                OxygenMain.network().sendTo(new CPWorldPointEdited(EnumWorldPoint.CAMP, pointId, camp, false), playerMP);

                if (flag)
                    OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CAMP_LOCKED.ordinal());
                else
                    OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CAMP_UNLOCKED.ordinal());
            }
        }
    }

    public void editCamp(EntityPlayerMP playerMP, long pointId, String name, String description, boolean updatePosition, boolean updateImage) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean())) { 
            WorldPoint camp = this.getCamp(playerUUID, pointId);
            if (camp != null
                    && camp.isOwner(playerUUID)) {
                TeleportationPlayerData playerData = this.manager.getPlayersDataContainer().getPlayerData(playerUUID);
                long newPointId = playerData.createId(pointId);
                name = name.trim();
                if (name.length() > WorldPoint.MAX_NAME_LENGTH)
                    name = name.substring(0, WorldPoint.MAX_NAME_LENGTH);
                if (name.isEmpty())
                    name = "Camp";

                description = description.trim();
                if (description.length() > WorldPoint.MAX_DESCRIPTION_LENGTH)
                    description = description.substring(0, WorldPoint.MAX_NAME_LENGTH);

                camp.setName(name);
                camp.setDescription(description);
                if (updatePosition)
                    camp.setPosition((float) playerMP.posX, (float) playerMP.posY, (float) playerMP.posZ, playerMP.dimension, playerMP.rotationYawHead, playerMP.rotationPitch);
                if (updateImage)
                    this.manager.getImagesLoader().removeCampPreviewImage(playerUUID, pointId);
                else
                    this.manager.getImagesLoader().renameCampPreviewImageAsync(playerUUID, pointId, newPointId);
                camp.setId(newPointId);
                playerData.addCamp(camp);
                if (playerData.getFavoriteCampId() == pointId)
                    playerData.setFavoriteCampId(newPointId);
                playerData.removeCamp(pointId);
                playerData.setChanged(true);

                if (this.manager.getSharedCampsContainer().haveInvitedPlayers(playerUUID, pointId)) {
                    this.manager.getSharedCampsContainer().replaceCamp(playerUUID, pointId, camp);
                    this.manager.getSharedCampsContainer().setChanged(true);
                }

                OxygenMain.network().sendTo(new CPWorldPointEdited(EnumWorldPoint.CAMP, pointId, camp, updateImage), playerMP);

                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CAMP_EDITED.ordinal());
            }
        }
    }

    public void invitePlayer(EntityPlayerMP playerMP, long pointId, UUID playerUUID) {
        UUID ownerUUID = CommonReference.getPersistentUUID(playerMP);
        if (TeleportationConfig.ENABLE_CAMP_INVITATIONS.asBoolean()
                && PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean())) { 
            WorldPoint camp = this.getCamp(ownerUUID, pointId);
            if (camp != null
                    && camp.isOwner(ownerUUID) 
                    && OxygenHelperServer.isPlayerOnline(playerUUID)
                    && !this.manager.getSharedCampsContainer().haveInvitation(playerUUID, pointId)
                    && !playerUUID.equals(ownerUUID)) {
                if (this.manager.getSharedCampsContainer().getInvitedPlayersAmountForCamp(playerUUID, pointId) < TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.asInt()) {
                    EntityPlayerMP invitedPlayerMP = CommonReference.playerByUUID(playerUUID);
                    OxygenHelperServer.sendRequest(playerMP, invitedPlayerMP, 
                            new CampInvitationRequest(
                                    TeleportationMain.INVITATION_TO_CAMP_ID,
                                    ownerUUID, 
                                    CommonReference.getName(playerMP), 
                                    pointId, 
                                    camp.getName()));
                } else
                    OxygenHelperServer.sendStatusMessage(playerMP, OxygenMain.OXYGEN_CORE_MOD_INDEX, EnumOxygenStatusMessage.REQUEST_RESET.ordinal());
            }
        }
    }

    public void uninvitePlayer(EntityPlayerMP playerMP, long pointId, UUID playerUUID) {
        UUID ownerUUID = CommonReference.getPersistentUUID(playerMP);
        if (TeleportationConfig.ENABLE_CAMP_INVITATIONS.asBoolean() 
                && PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_CAMPS_USAGE.id(), TeleportationConfig.ENABLE_CAMPS.asBoolean())) {
            WorldPoint camp = this.getCamp(ownerUUID, pointId);
            if (camp != null
                    && camp.isOwner(ownerUUID)
                    && this.manager.getSharedCampsContainer().haveInvitation(playerUUID, pointId)) {
                this.manager.getSharedCampsContainer().uninvite(ownerUUID, pointId, playerUUID);
                this.manager.getSharedCampsContainer().setChanged(true);

                OxygenHelperServer.removeObservedPlayer(ownerUUID, playerUUID);

                OxygenMain.network().sendTo(new CPPlayerUninvited(pointId, playerUUID), playerMP);

                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.UNINVITED.ordinal());
            }
        }
    }

    public void leaveCamp(EntityPlayerMP playerMP, long pointId) {
        UUID 
        playerUUID = CommonReference.getPersistentUUID(playerMP),
        ownerUUID;
        if (this.manager.getSharedCampsContainer().haveInvitation(playerUUID, pointId)) {
            ownerUUID = this.manager.getSharedCampsContainer().getCampOwner(pointId);            
            this.manager.getSharedCampsContainer().uninvite(ownerUUID, pointId, playerUUID);
            this.manager.getSharedCampsContainer().setChanged(true);

            OxygenHelperServer.removeObservedPlayer(ownerUUID, playerUUID);

            TeleportationPlayerData playerData =  this.manager.getPlayersDataContainer().getPlayerData(playerUUID);
            if (playerData.getFavoriteCampId() == pointId)
                playerData.setFavoriteCampId(0L);
            playerData.setChanged(true);    

            OxygenMain.network().sendTo(new CPWorldPointRemoved(EnumWorldPoint.CAMP, pointId), playerMP);

            OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CAMP_LEFT.ordinal());
        }
    }

    public void syncInvitationsData(EntityPlayerMP playerMP) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.manager.getSharedCampsContainer().haveInvitedPlayers(playerUUID)) {
            ByteBuf buffer = null;
            try {
                buffer = Unpooled.buffer();
                this.manager.getSharedCampsContainer().getInvitationsContainer(playerUUID).write(buffer);
                byte[] compressed = new byte[buffer.writerIndex()];
                buffer.readBytes(compressed);
                OxygenMain.network().sendTo(new CPSyncInvitedPlayers(compressed), playerMP);
            } finally {
                if (buffer != null)
                    buffer.release();
            }
        }
    }

    private boolean haveInvitation(UUID playerUUID, long pointId) {
        return this.manager.getSharedCampsContainer().haveInvitation(playerUUID, pointId) 
                && this.manager.getSharedCampsContainer().isCampExist(pointId);
    }

    @Nullable
    private WorldPoint getCamp(UUID playerUUID, long pointId) {
        return this.manager.getPlayersDataContainer().getPlayerData(playerUUID).getCamp(pointId);
    }

    private boolean campAvailable(WorldPoint worldPoint, UUID playerUUID) {       
        return !worldPoint.isLocked() || worldPoint.isOwner(playerUUID);
    }

    private boolean readyMoveToCamp(UUID playerUUID) {
        return TimeHelperServer.getCurrentMillis() >= this.manager.getPlayersDataContainer().getPlayerData(playerUUID).getCooldownData().getNextCampTime();
    }
}
