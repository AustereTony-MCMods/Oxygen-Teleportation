package austeretony.oxygen_teleportation.client;

import java.util.UUID;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivileges;
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
        return TeleportationPlayerData.EnumJumpProfile.values()[OxygenHelperClient.getSharedPlayerData(playerUUID).getData(TeleportationMain.JUMP_PROFILE_DATA_ID).get(0)];
    }

    public void changeJumpProfileSynced(TeleportationPlayerData.EnumJumpProfile profile) {
        TeleportationMain.network().sendToServer(new SPChangeJumpProfile(profile));
    }

    public void moveToPlayerSynced(UUID targetUUID) {
        TeleportationMain.network().sendToServer(new SPMoveToPlayer(targetUUID));
        TeleportationManagerClient.instance().setTeleportationDelay(PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.PLAYER_TELEPORTATION_DELAY.toString(), TeleportationConfig.PLAYERS_TELEPORT_DELAY.getIntValue()));
    }
}
