package austeretony.teleportation.common;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.main.EnumOxygenChatMessages;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumTeleportationChatMessages;
import austeretony.teleportation.common.main.EnumTeleportationPrivileges;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationPlayerData;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.main.TeleportationRequest;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayersManagerServer {

    private final TeleportationManagerServer manager;

    public PlayersManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public void changeJumpProfile(EntityPlayerMP playerMP, TeleportationPlayerData.EnumJumpProfile profile) {
        if (TeleportationConfig.ENABLE_PLAYERS.getBooleanValue()) {
            UUID playerUUID = CommonReference.uuid(playerMP);
            this.manager.getPlayerProfile(playerUUID).setJumpProfile(profile);
            this.manager.getCampsLoader().savePlayerData(playerUUID);
            this.manager.updateSharedPlayerData(playerUUID);
            OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.JUMP_PROFILE_CHANGED.ordinal(), profile.toString().toLowerCase());
        }
    }

    public void moveToPlayer(EntityPlayerMP visitorPlayerMP, UUID targetUUID) {
        if (TeleportationConfig.ENABLE_PLAYERS.getBooleanValue()) {
            UUID visitorUUID = CommonReference.uuid(visitorPlayerMP);
            if (!this.teleporting(visitorUUID) && this.readyMoveToPlayer(visitorUUID)) {
                if (OxygenHelperServer.isOnline(targetUUID) 
                        && !visitorUUID.equals(targetUUID)) {
                    TeleportationPlayerData.EnumJumpProfile targetJumpProfile = this.manager.getPlayerProfile(targetUUID).getJumpProfile();
                    switch (targetJumpProfile) {
                    case DISABLED:
                        if (PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumTeleportationPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
                            this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        break;
                    case FREE:
                        this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        break;
                    case REQUEST:
                        if (PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumTeleportationPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
                            this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        else {
                            EntityPlayerMP targetPlayerMP = CommonReference.playerByUUID(targetUUID);
                            OxygenHelperServer.sendRequest(visitorPlayerMP, targetPlayerMP, 
                                    new TeleportationRequest(TeleportationMain.TELEPORTATION_REQUEST_ID, visitorUUID, CommonReference.username(visitorPlayerMP)), true);
                        }
                        break;
                    }  
                } else
                    OxygenHelperServer.sendMessage(visitorPlayerMP, OxygenMain.OXYGEN_MOD_INDEX, EnumOxygenChatMessages.REQUEST_RESET.ordinal());
            } else
                OxygenHelperServer.sendMessage(visitorPlayerMP, OxygenMain.OXYGEN_MOD_INDEX, EnumOxygenChatMessages.REQUEST_RESET.ordinal());
        }
    }

    public void move(EntityPlayerMP visitorPlayerMP, UUID visitorUUID, UUID targetUUID) {
        EntityPlayerMP targetPlayerMP = CommonReference.playerByUUID(targetUUID);
        if (!PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumTeleportationPrivileges.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                && visitorPlayerMP.dimension != targetPlayerMP.dimension) {
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
            return;
        }
        int delay = PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumTeleportationPrivileges.PLAYER_TELEPORTATION_DELAY.toString(), TeleportationConfig.PLAYERS_TELEPORT_DELAY.getIntValue());
        if (delay < 1)
            delay = 1;
        if (delay > 1)
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.PREPARE_FOR_TELEPORTATION.ordinal(), String.valueOf(delay));
        TeleportationProcess.create(visitorPlayerMP, targetPlayerMP, delay); 
    }  

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToPlayer(UUID playerUUID) {
        return System.currentTimeMillis() - this.manager.getPlayerProfile(playerUUID).getCooldownInfo().getLastJumpTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivileges.PLAYER_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}
