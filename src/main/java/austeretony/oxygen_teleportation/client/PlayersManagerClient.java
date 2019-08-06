package austeretony.oxygen_teleportation.client;

import java.util.UUID;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.network.server.SPChangeJumpProfile;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToPlayer;

public class PlayersManagerClient {

    private final TeleportationManagerClient manager;

    public PlayersManagerClient(TeleportationManagerClient manager) {
        this.manager = manager; 
    }

    public static TeleportationPlayerData.EnumJumpProfile getPlayerJumpProfile(UUID playerUUID) {
        return TeleportationPlayerData.EnumJumpProfile.values()[OxygenHelperClient.getSharedPlayerData(playerUUID).getByte(TeleportationMain.JUMP_PROFILE_SHARED_DATA_ID)];
    }

    public void changeJumpProfileSynced(TeleportationPlayerData.EnumJumpProfile profile) {
        TeleportationMain.network().sendToServer(new SPChangeJumpProfile(profile));
    }

    public void moveToPlayerSynced(int index) {
        TeleportationMain.network().sendToServer(new SPMoveToPlayer(index));
        TeleportationManagerClient.instance().setTeleportationDelay(PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.PLAYER_TELEPORTATION_DELAY.toString(), TeleportationConfig.PLAYERS_TELEPORT_DELAY.getIntValue()));
    }
}
