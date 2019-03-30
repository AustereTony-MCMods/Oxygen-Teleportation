package austeretony.teleportation.common.menu.players;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumChatMessages;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.network.server.SPChangeJumpProfile;
import austeretony.teleportation.common.network.server.SPJumpRequestReply;
import austeretony.teleportation.common.network.server.SPMoveToPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayersManagerClient {

    private static PlayersManagerClient instance;

    private final JumpRequestClient jumpRequest = new JumpRequestClient();

    private PlayersManagerClient() {}

    public static void create() {
        instance = new PlayersManagerClient();
    }

    public static PlayersManagerClient instance() {
        return instance;
    }

    public static PlayerProfile.EnumJumpProfile getPlayerJumpProfile() {
        return PlayerProfile.EnumJumpProfile.values()[OxygenHelperClient.getPlayerData(OxygenHelperClient.getPlayerUUID()).getData(TeleportationMain.JUMP_PROFILE_DATA_ID).get(0)];
    } 

    public static PlayerProfile.EnumJumpProfile getPlayerJumpProfile(UUID playerUUID) {
        return PlayerProfile.EnumJumpProfile.values()[OxygenHelperClient.getPlayerData(playerUUID).getData(TeleportationMain.JUMP_PROFILE_DATA_ID).get(0)];
    }

    public JumpRequestClient getJumpRequest() {
        return this.jumpRequest;
    }

    //TODO changeJumpProfileSynced()
    public void changeJumpProfileSynced(PlayerProfile.EnumJumpProfile profile) {
        TeleportationMain.network().sendToServer(new SPChangeJumpProfile(profile));
    }

    //TODO moveToPlayerSynced()
    public void moveToPlayerSynced(UUID targetUUID) {
        if (OxygenHelperClient.getPlayerData(targetUUID) != null) {
            TeleportationMain.network().sendToServer(new SPMoveToPlayer(targetUUID));
            CampsManagerClient.instance().setTeleportationDelay(PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.PLAYER_TELEPORTATION_DELAY.toString(), TeleportationConfig.PLAYERS_TELEPORT_DELAY.getIntValue()));
        }
    }

    //TODO replyJumpRequestSynced()
    public void replyJumpRequestSynced(SPJumpRequestReply.EnumReply reply) {
        if (this.getJumpRequest().exist()) {
            TeleportationMain.network().sendToServer(new SPJumpRequestReply(reply));
            this.getJumpRequest().reset();
            if (reply == SPJumpRequestReply.EnumReply.ACCEPT)
                EnumChatMessages.JUMP_REQUEST_ACCEPTED.show(this.getJumpRequest().getVisitorUsername());
            else
                EnumChatMessages.JUMP_REQUEST_REJECTED.show(this.getJumpRequest().getVisitorUsername());
        }
    }
}
