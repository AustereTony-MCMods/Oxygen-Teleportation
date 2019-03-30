package austeretony.teleportation.common.menu.players;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumChatMessages;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.menu.camps.CampsLoaderServer;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.network.client.CPSendJumpRequest;
import austeretony.teleportation.common.network.server.SPJumpRequestReply;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PlayersManagerServer {

    private static PlayersManagerServer instance;

    private final Map<UUID, JumpRequestServer> jumpRequests = new ConcurrentHashMap<UUID, JumpRequestServer>();

    private PlayersManagerServer() {}

    public static void create() {
        instance = new PlayersManagerServer();
    }

    public static PlayersManagerServer instance() {
        return instance;
    }

    public static PlayerProfile.EnumJumpProfile getPlayerJumpProfile(UUID playerUUID) {
        return CampsManagerServer.instance().getPlayerProfile(playerUUID).getJumpProfile();
    }

    public Map<UUID, JumpRequestServer> getJumpRequests() {
        return this.jumpRequests;
    }

    //TODO changeJumpProfile()
    public void changeJumpProfile(EntityPlayerMP playerMP, PlayerProfile.EnumJumpProfile profile) {
        if (TeleportationConfig.ENABLE_PLAYERS.getBooleanValue()) {
            UUID playerUUID = OxygenHelperServer.uuid(playerMP);
            CampsManagerServer.instance().getPlayerProfile(playerUUID).setJumpProfile(profile);
            CampsLoaderServer.savePlayerData(playerUUID);
            CampsManagerServer.instance().updateAdditionalPlayerData(playerUUID);
            OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_PROFILE_CHANGED.ordinal(), profile.toString().toLowerCase());
        }
    }

    //TODO moveToPlayer()
    public void moveToPlayer(EntityPlayerMP visitorPlayerMP, UUID targetUUID) {
        if (TeleportationConfig.ENABLE_PLAYERS.getBooleanValue()) {
            UUID visitorUUID = OxygenHelperServer.uuid(visitorPlayerMP);
            if (!this.teleporting(visitorUUID) && this.readyMoveToPlayer(visitorUUID)) {
                if (OxygenHelperServer.isOnline(targetUUID)) {
                    PlayerProfile.EnumJumpProfile targetJumpProfile = this.getPlayerJumpProfile(targetUUID);
                    switch (targetJumpProfile) {
                    case DISABLED:
                        if (CommonReference.isOpped(visitorPlayerMP))
                            this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        break;
                    case FREE:
                        this.move(visitorPlayerMP, visitorUUID, targetUUID);
                        break;
                    case REQUEST:
                        this.requestJump(visitorPlayerMP, visitorUUID, targetUUID);
                        break;
                    }  
                } else
                    OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_TARGET_OFFLINE.ordinal());
            }
        }
    }

    private void move(EntityPlayerMP visitorPlayerMP, UUID visitorUUID, UUID targetUUID) {
        EntityPlayerMP targetPlayerMP = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(targetUUID);
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

    private void requestJump(EntityPlayerMP visitorPlayerMP, UUID visitorUUID, UUID targetUUID) {
        if (!JumpRequestServer.exist(targetUUID)) {
            EntityPlayerMP targetPlayerMP = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(targetUUID);
            JumpRequestServer.create(targetUUID, visitorUUID);
            TeleportationMain.network().sendTo(new CPSendJumpRequest(OxygenHelperServer.username(visitorPlayerMP)), targetPlayerMP);
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_SENT.ordinal(), OxygenHelperServer.username(targetPlayerMP));
        } else
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_RESET.ordinal());
    }

    public void processJumpRequests() {
        if (!this.getJumpRequests().isEmpty()) {
            Iterator<JumpRequestServer> iterator = this.getJumpRequests().values().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().expired())
                    iterator.remove();
            }
        }
    }

    //TODO processJumpRequestReply()
    public void processJumpRequestReply(EntityPlayerMP targetPlayerMP, SPJumpRequestReply.EnumReply reply) {
        UUID targetUUID = OxygenHelperServer.uuid(targetPlayerMP);
        if (JumpRequestServer.exist(targetUUID)) {
            UUID visitorUUID = JumpRequestServer.get(targetUUID).getVisitorUUID();
            if (OxygenHelperServer.isOnline(visitorUUID)) {
                EntityPlayerMP visitorPlayerMP = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(visitorUUID);
                switch (reply) {
                case ACCEPT:
                    OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_ACCEPTED_VISITOR.ordinal(), OxygenHelperServer.username(targetPlayerMP));
                    this.move(visitorPlayerMP, visitorUUID, targetUUID);
                    break;
                case REJECT:
                    OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_REJECTED_VISITOR.ordinal(), OxygenHelperServer.username(targetPlayerMP));
                    break;
                }
                JumpRequestServer.get(targetUUID).reset();
            } else
                OxygenHelperServer.sendMessage(targetPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_VISITOR_OFFLINE.ordinal());
        }
    }

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToPlayer(UUID playerUUID) {
        return System.currentTimeMillis() - CampsManagerServer.instance().getPlayerProfile(playerUUID).getCooldownInfo().getLastJumpTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.PLAYER_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}
