package austeretony.teleportation.common;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumChatMessages;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.players.TeleportationRequest;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayersManagerServer {

    private final TeleportationManagerServer manager;

    private final Set<UUID> 
    requestedPlayers = new HashSet<UUID>(),//one incoming teleportation request per player
    requestingPlayers = new HashSet<UUID>();//for preventing teleportation requests spam, only one request until reply or expire

    public PlayersManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public Set<UUID> getRequestedPlayers() {
        return this.requestedPlayers;
    }

    public boolean playerRequested(UUID playerUUID) {
        return this.requestedPlayers.contains(playerUUID);
    }

    public void setRequested(UUID playerUUID) {
        this.requestedPlayers.add(playerUUID);
    }

    public void resetRequest(UUID playerUUID) {
        this.requestedPlayers.remove(playerUUID);
    }

    public Set<UUID> getRequestingPlayers() {
        return this.requestingPlayers;
    }

    public boolean playerRequesting(UUID playerUUID) {
        return this.requestingPlayers.contains(playerUUID);
    }

    public void setRequesting(UUID playerUUID) {
        this.requestingPlayers.add(playerUUID);
    }

    public void resetRequesting(UUID playerUUID) {
        this.requestingPlayers.remove(playerUUID);
    }

    public void changeJumpProfile(EntityPlayerMP playerMP, PlayerProfile.EnumJumpProfile profile) {
        if (TeleportationConfig.ENABLE_PLAYERS.getBooleanValue()) {
            UUID playerUUID = CommonReference.uuid(playerMP);
            this.manager.getPlayerProfile(playerUUID).setJumpProfile(profile);
            this.manager.getCampsLoader().savePlayerData(playerUUID);
            this.manager.updateAdditionalPlayerData(playerUUID);
            OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_PROFILE_CHANGED.ordinal(), profile.toString().toLowerCase());
        }
    }

    public void moveToPlayer(EntityPlayerMP visitorPlayerMP, UUID targetUUID) {
        if (TeleportationConfig.ENABLE_PLAYERS.getBooleanValue()) {
            UUID visitorUUID = CommonReference.uuid(visitorPlayerMP);
            if (!this.teleporting(visitorUUID) && this.readyMoveToPlayer(visitorUUID)) {
                if (OxygenHelperServer.isOnline(targetUUID)) {
                    PlayerProfile.EnumJumpProfile targetJumpProfile = this.manager.getPlayerProfile(targetUUID).getJumpProfile();
                    switch (targetJumpProfile) {
                    case DISABLED:
                        if (PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
                            this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        break;
                    case FREE:
                        this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        break;
                    case REQUEST:
                        if (PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
                            this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        else if (!this.playerRequesting(visitorUUID) && !this.playerRequested(targetUUID) && !visitorUUID.equals(targetUUID)) {
                            this.setRequesting(visitorUUID);
                            this.setRequested(targetUUID);
                            EntityPlayerMP targetPlayerMP = CommonReference.playerByUUID(targetUUID);
                            OxygenHelperServer.addNotification(targetPlayerMP, 
                                    new TeleportationRequest(
                                            0,//teleportation request index
                                            targetUUID, 
                                            visitorUUID, 
                                            CommonReference.username(visitorPlayerMP)));
                            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_SENT.ordinal(), CommonReference.username(targetPlayerMP));
                        } else
                            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_RESET.ordinal());
                        break;
                    }  
                } else
                    OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_TARGET_OFFLINE.ordinal());
            }
        }
    }

    public void move(EntityPlayerMP visitorPlayerMP, UUID visitorUUID, UUID targetUUID) {
        EntityPlayerMP targetPlayerMP = CommonReference.playerByUUID(targetUUID);
        if (!PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumPrivileges.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                && visitorPlayerMP.dimension != targetPlayerMP.dimension) {
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
            return;
        }
        int delay = PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumPrivileges.PLAYER_TELEPORTATION_DELAY.toString(), TeleportationConfig.PLAYERS_TELEPORT_DELAY.getIntValue());
        if (delay < 1)
            delay = 1;
        if (delay > 1)
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.PREPARE_FOR_TELEPORTATION.ordinal(), String.valueOf(delay));
        TeleportationProcess.create(visitorPlayerMP, targetPlayerMP, delay); 
    }  

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToPlayer(UUID playerUUID) {
        return System.currentTimeMillis() - this.manager.getPlayerProfile(playerUUID).getCooldownInfo().getLastJumpTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.PLAYER_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}
