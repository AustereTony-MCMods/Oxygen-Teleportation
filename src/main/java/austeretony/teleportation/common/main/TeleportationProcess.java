package austeretony.teleportation.common.main;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.process.AbstractTemporaryProcess;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.network.client.CPSyncCooldown;
import austeretony.teleportation.common.world.SimpleTeleporter;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportationProcess extends AbstractTemporaryProcess {

    public final EnumTeleportations type;

    public final EntityPlayerMP player;

    private long pointId;

    private EntityPlayerMP target;

    private final double prevX, prevY, prevZ;

    private final boolean processOnMove;

    private TeleportationProcess(EnumTeleportations type, EntityPlayerMP player, int delay) {
        super();
        this.type = type;
        this.player = player;
        this.prevX = player.posX;
        this.prevY = player.posY;
        this.prevZ = player.posZ;
        this.processOnMove = PrivilegeProviderServer.getPrivilegeValue(CommonReference.uuid(player), EnumPrivileges.PROCESS_TELEPORTATION_ON_MOVE.toString(), 
                TeleportationConfig.PROCESS_TELEPORTATION_ON_MOVE.getBooleanValue());
        this.counter = delay * 20;
    }

    private TeleportationProcess(EnumTeleportations type, EntityPlayerMP player, long pointId, int delay) {
        this(type, player, delay);
        this.pointId = pointId;
    }

    private TeleportationProcess(EnumTeleportations type, EntityPlayerMP player, EntityPlayerMP target, int delay) {
        this(type, player, delay);
        this.target = target;
    }

    public static void create(EnumTeleportations type, EntityPlayerMP player, long pointId, int delay) {
        OxygenHelperServer.addPlayerProcess(player, new TeleportationProcess(type, player, pointId, delay));
    }

    public static void create(EntityPlayerMP player, EntityPlayerMP target, int delay) {
        OxygenHelperServer.addPlayerProcess(player, new TeleportationProcess(EnumTeleportations.JUMP, player, target, delay));
    }

    public static boolean exist(UUID playerUUID) {
        return TeleportationManagerServer.instance().getTeleportations().contains(playerUUID);
    }

    @Override
    public boolean isExpired() {
        if (this.counter > 0) {
            if (!this.processOnMove) {
                if (this.player.posX != this.prevX || this.player.posZ != this.prevZ) {
                    OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.TELEPORTATION_ABORTED.ordinal());
                    return true;
                }
            }
            this.counter--;
        }
        if (this.counter == 0) {
            if (!OxygenHelperServer.isOnline(CommonReference.uuid(this.player)))
                return true;
            if (this.type == EnumTeleportations.JUMP && !OxygenHelperServer.isOnline(CommonReference.uuid(this.target)))
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
        UUID playerUUID = CommonReference.uuid(this.player);
        WorldPoint point;
        switch (this.type) {
        case CAMP:
            point = TeleportationManagerServer.instance().getPlayerProfile(playerUUID).getCamp(this.pointId);
            this.move(point.getDimensionId(), point.getXPos(), point.getYPos(), point.getZPos(), point.getYaw(), point.getPitch());
            if (PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.CAMP_TELEPORTATION_COOLDOWN.toString(), 
                    TeleportationConfig.CAMPS_TELEPORT_COOLDOWN.getIntValue()) > 0) {
                TeleportationManagerServer.instance().getPlayerProfile(playerUUID).getCooldownInfo().movedToCamp();
                TeleportationMain.network().sendTo(new CPSyncCooldown(), this.player);
            }
            OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.MOVED_TO_CAMP.ordinal(), point.getName());
            break;
        case LOCATION:
            point = TeleportationManagerServer.instance().getWorldData().getLocation(this.pointId);
            this.move(point.getDimensionId(), point.getXPos(), point.getYPos(), point.getZPos(), point.getYaw(), point.getPitch());
            if (PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.LOCATION_TELEPORTATION_COOLDOWN.toString(), 
                    TeleportationConfig.LOCATIONS_TELEPORT_COOLDOWN.getIntValue()) > 0) {
                TeleportationManagerServer.instance().getPlayerProfile(playerUUID).getCooldownInfo().movedToLocation();
                TeleportationMain.network().sendTo(new CPSyncCooldown(), this.player);
            }
            OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.MOVED_TO_LOCATION.ordinal(), point.getName());
            break;
        case JUMP:
            this.move(this.target.dimension, (float) this.target.posX, (float) this.target.posY, (float) this.target.posZ, this.target.rotationYawHead, this.target.rotationPitch);
            if (PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.PLAYER_TELEPORTATION_COOLDOWN.toString(), 
                    TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) > 0) {
                TeleportationManagerServer.instance().getPlayerProfile(playerUUID).getCooldownInfo().jumped();
                TeleportationMain.network().sendTo(new CPSyncCooldown(), this.player);
            }
            OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.MOVED_TO_PLAYER.ordinal(), CommonReference.username(this.target));
            break;
        }        
        TeleportationManagerServer.instance().getCampsLoader().savePlayerData(playerUUID);
        TeleportationManagerServer.instance().getTeleportations().remove(playerUUID);
    }

    private void move(int dimId, float x, float y, float z, float yaw, float pitch) {
        this.player.rotationYaw = this.player.rotationYawHead = yaw;
        this.player.rotationPitch = pitch;        
        if (this.player.dimension == dimId)                                                 
            this.player.setPositionAndUpdate(x, y, z);    
        else                                                
            SimpleTeleporter.transferToDimension(this.player, dimId, x, y, z);
    }

    public enum EnumTeleportations {

        CAMP,
        LOCATION,
        JUMP
    }
}
