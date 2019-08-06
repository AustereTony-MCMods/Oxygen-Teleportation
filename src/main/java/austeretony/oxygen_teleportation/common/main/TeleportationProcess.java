package austeretony.oxygen_teleportation.common.main;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.process.AbstractTemporaryProcess;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen_teleportation.common.TeleportationLoaderServer;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.network.client.CPSyncCooldown;
import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportationProcess extends AbstractTemporaryProcess {

    public final EnumTeleportation type;

    public final EntityPlayerMP player;

    private long pointId;

    private EntityPlayerMP target;

    private final double prevX, prevY, prevZ;

    private final boolean processOnMove;

    private TeleportationProcess(EnumTeleportation type, EntityPlayerMP player, int delay) {
        super();
        this.type = type;
        this.player = player;
        this.prevX = player.posX;
        this.prevY = player.posY;
        this.prevZ = player.posZ;
        this.processOnMove = PrivilegeProviderServer.getPrivilegeValue(CommonReference.getPersistentUUID(player), EnumTeleportationPrivilege.PROCESS_TELEPORTATION_ON_MOVE.toString(), 
                TeleportationConfig.PROCESS_TELEPORTATION_ON_MOVE.getBooleanValue());
        this.counter = delay * 20;
    }

    private TeleportationProcess(EnumTeleportation type, EntityPlayerMP player, long pointId, int delay) {
        this(type, player, delay);
        this.pointId = pointId;
    }

    private TeleportationProcess(EnumTeleportation type, EntityPlayerMP player, EntityPlayerMP target, int delay) {
        this(type, player, delay);
        this.target = target;
    }

    public static void create(EnumTeleportation type, EntityPlayerMP player, long pointId, int delay) {
        OxygenHelperServer.addPlayerTemporaryProcess(player, new TeleportationProcess(type, player, pointId, delay));
    }

    public static void create(EntityPlayerMP player, EntityPlayerMP target, int delay) {
        OxygenHelperServer.addPlayerTemporaryProcess(player, new TeleportationProcess(EnumTeleportation.JUMP, player, target, delay));
    }

    public static boolean exist(UUID playerUUID) {
        return TeleportationManagerServer.instance().getTeleportations().contains(playerUUID);
    }

    @Override
    public void process() {}

    @Override
    public boolean isExpired() {
        if (this.counter > 0) {
            if (!this.processOnMove) {
                if (this.player.posX != this.prevX || this.player.posZ != this.prevZ) {
                    OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.TELEPORTATION_ABORTED.ordinal());
                    return true;
                }
            }
            this.counter--;
        }
        if (this.counter == 0) {
            if (!OxygenHelperServer.isOnline(CommonReference.getPersistentUUID(this.player)))
                return true;
            if (this.type == EnumTeleportation.JUMP && !OxygenHelperServer.isOnline(CommonReference.getPersistentUUID(this.target)))
                return true;
            this.expired();
            return true;
        }
        return false;
    }

    @Override
    public int getExpireTime() {
        return 0;//unused
    }

    @Override
    public void expired() {
        UUID playerUUID = CommonReference.getPersistentUUID(this.player);
        WorldPoint point;
        switch (this.type) {
        case CAMP:
            TeleportationPlayerData playerData = TeleportationManagerServer.instance().getPlayerData(playerUUID);
            if (TeleportationManagerServer.instance().getSharedCampsManager().haveInvitation(playerUUID, this.pointId))
                point = TeleportationManagerServer.instance().getSharedCampsManager().getCamp(this.pointId);
            else
                point = playerData.getCamp(this.pointId);
            CommonReference.teleportPlayer(this.player, point.getDimensionId(), point.getXPos(), point.getYPos(), point.getZPos(), point.getYaw(), point.getPitch());
            if (PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN.toString(), 
                    TeleportationConfig.CAMPS_TELEPORT_COOLDOWN.getIntValue()) > 0) {
                playerData.getCooldownInfo().movedToCamp();
                TeleportationMain.network().sendTo(new CPSyncCooldown(playerData.getCooldownInfo()), this.player);
            }
            OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.MOVED_TO_CAMP.ordinal(), point.getName());
            break;
        case LOCATION:
            point = TeleportationManagerServer.instance().getWorldData().getLocation(this.pointId);
            CommonReference.teleportPlayer(this.player, point.getDimensionId(), point.getXPos(), point.getYPos(), point.getZPos(), point.getYaw(), point.getPitch());
            if (PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN.toString(), 
                    TeleportationConfig.LOCATIONS_TELEPORT_COOLDOWN.getIntValue()) > 0) {
                TeleportationManagerServer.instance().getPlayerData(playerUUID).getCooldownInfo().movedToLocation();
                TeleportationMain.network().sendTo(new CPSyncCooldown(), this.player);
            }
            OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.MOVED_TO_LOCATION.ordinal(), point.getName());
            break;
        case JUMP:
            CommonReference.teleportPlayer(this.player, this.target.dimension, (float) this.target.posX, (float) this.target.posY, (float) this.target.posZ, this.target.rotationYawHead, this.target.rotationPitch);
            if (PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN.toString(), 
                    TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) > 0) {
                TeleportationManagerServer.instance().getPlayerData(playerUUID).getCooldownInfo().jumped();
                TeleportationMain.network().sendTo(new CPSyncCooldown(), this.player);
            }
            OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.MOVED_TO_PLAYER.ordinal(), CommonReference.getName(this.target));
            break;
        }        
        TeleportationLoaderServer.savePersistentDataDelegated(TeleportationManagerServer.instance().getPlayerData(playerUUID));
        TeleportationManagerServer.instance().getTeleportations().remove(playerUUID);
    }

    public enum EnumTeleportation {

        CAMP,
        LOCATION,
        JUMP
    }
}
