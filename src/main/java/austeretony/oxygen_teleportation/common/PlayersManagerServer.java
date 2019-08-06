package austeretony.oxygen_teleportation.common;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.main.EnumOxygenChatMessage;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationChatMessage;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData.EnumJumpProfile;
import austeretony.oxygen_teleportation.common.main.TeleportationProcess;
import austeretony.oxygen_teleportation.common.main.TeleportationRequest;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayersManagerServer {

    private final TeleportationManagerServer manager;

    public PlayersManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public void changeJumpProfile(EntityPlayerMP playerMP, EnumJumpProfile profile) {
        if (TeleportationConfig.ENABLE_PLAYERS.getBooleanValue()) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            TeleportationPlayerData playerData = this.manager.getPlayerData(playerUUID);
            playerData.setJumpProfile(profile);
            TeleportationLoaderServer.savePersistentDataDelegated(playerData);
            this.manager.updateJumpProfile(playerUUID);
            OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.JUMP_PROFILE_CHANGED.ordinal(), profile.toString().toLowerCase());
        }
    }

    public void moveToPlayer(EntityPlayerMP visitorPlayerMP, int targetIndex) {
        if (TeleportationConfig.ENABLE_PLAYERS.getBooleanValue()) {
            UUID 
            visitorUUID = CommonReference.getPersistentUUID(visitorPlayerMP),
            targetUUID;
            if (!this.teleporting(visitorUUID) 
                    && this.readyMoveToPlayer(visitorUUID)
                    && OxygenHelperServer.isOnline(targetIndex)) { 
                targetUUID = OxygenHelperServer.getSharedPlayerData(targetIndex).getPlayerUUID();
                if (!visitorUUID.equals(targetUUID)) {
                    EnumJumpProfile targetJumpProfile = this.manager.getPlayerData(targetUUID).getJumpProfile();
                    switch (targetJumpProfile) {
                    case DISABLED:
                        if (PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
                            this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        break;
                    case FREE:
                        this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        break;
                    case REQUEST:
                        if (PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false))
                            this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        else {
                            EntityPlayerMP targetPlayerMP = CommonReference.playerByUUID(targetUUID);
                            OxygenHelperServer.sendRequest(visitorPlayerMP, targetPlayerMP, 
                                    new TeleportationRequest(TeleportationMain.TELEPORTATION_REQUEST_ID, visitorUUID, CommonReference.getName(visitorPlayerMP)), true);
                        }
                        break;
                    }  
                } else
                    OxygenHelperServer.sendMessage(visitorPlayerMP, OxygenMain.OXYGEN_MOD_INDEX, EnumOxygenChatMessage.REQUEST_RESET.ordinal());
            } else
                OxygenHelperServer.sendMessage(visitorPlayerMP, OxygenMain.OXYGEN_MOD_INDEX, EnumOxygenChatMessage.REQUEST_RESET.ordinal());
        }
    }

    public void move(EntityPlayerMP visitorPlayerMP, UUID visitorUUID, UUID targetUUID) {
        EntityPlayerMP targetPlayerMP = CommonReference.playerByUUID(targetUUID);
        if (!PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                && visitorPlayerMP.dimension != targetPlayerMP.dimension) {
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
            return;
        }
        int delay = PrivilegeProviderServer.getPrivilegeValue(visitorUUID, EnumTeleportationPrivilege.PLAYER_TELEPORTATION_DELAY.toString(), TeleportationConfig.PLAYERS_TELEPORT_DELAY.getIntValue());
        if (delay < 1)
            delay = 1;
        if (delay > 1)
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.PREPARE_FOR_TELEPORTATION.ordinal(), String.valueOf(delay));
        TeleportationProcess.create(visitorPlayerMP, targetPlayerMP, delay); 
    }  

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToPlayer(UUID playerUUID) {
        return System.currentTimeMillis() - this.manager.getPlayerData(playerUUID).getCooldownInfo().getLastJumpTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}
