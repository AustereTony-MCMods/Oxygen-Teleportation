package austeretony.teleportation.common.main;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.menu.camps.CampsLoaderServer;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.menu.locations.LocationsManagerServer;
import austeretony.teleportation.common.network.client.CPSyncCooldown;
import austeretony.teleportation.common.world.SimpleTeleporter;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportationProcess {

    public final EnumTeleportations type;

    public final EntityPlayerMP player;

    private long pointId;

    private EntityPlayerMP target;

    private double prevX, prevY, prevZ;

    private boolean processOnMove;

    private int counter;

    private TeleportationProcess(EnumTeleportations type, EntityPlayerMP player, int delay) {
        this.type = type;
        this.player = player;
        this.prevX = player.posX;
        this.prevY = player.posY;
        this.prevZ = player.posZ;
        this.processOnMove = PrivilegeProviderServer.getPrivilegeValue(OxygenHelperServer.uuid(player), EnumPrivileges.PROCESS_TELEPORTATION_ON_MOVE.toString(), 
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
        CampsManagerServer.instance().getTeleportations().put(OxygenHelperServer.uuid(player), new TeleportationProcess(type, player, pointId, delay));
    }

    public static void create(EntityPlayerMP player, EntityPlayerMP target, int delay) {
        CampsManagerServer.instance().getTeleportations().put(OxygenHelperServer.uuid(player), new TeleportationProcess(EnumTeleportations.JUMP, player, target, delay));
    }

    public static boolean exist(UUID playerUUID) {
        return CampsManagerServer.instance().getTeleportations().containsKey(playerUUID);
    }

    public static TeleportationProcess get(UUID playerUUID) {
        return CampsManagerServer.instance().getTeleportations().get(playerUUID);
    }

    public boolean expired() {
        if (this.counter > 0) {
            this.counter--;
            if (!this.processOnMove) {
                if (this.player.posX != this.prevX || this.player.posZ != this.prevZ) {
                    OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.TELEPORTATION_ABORTED.ordinal());
                    return true;
                }
            }
            if (this.counter == 0) {
                UUID playerUUID = OxygenHelperServer.uuid(this.player);
                if (!OxygenHelperServer.isOnline(playerUUID))
                    return true;
                WorldPoint point;
                switch (this.type) {
                case CAMP:
                    point = CampsManagerServer.instance().getPlayerProfile(playerUUID).getCamp(this.pointId);
                    this.move(point.getDimensionId(), point.getXPos(), point.getYPos(), point.getZPos(), point.getYaw(), point.getPitch());
                    if (PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.CAMP_TELEPORTATION_COOLDOWN.toString(), 
                            TeleportationConfig.CAMPS_TELEPORT_COOLDOWN.getIntValue()) > 0) {
                        CampsManagerServer.instance().getPlayerProfile(playerUUID).getCooldownInfo().movedToCamp();
                        TeleportationMain.network().sendTo(new CPSyncCooldown(), this.player);
                    }
                    OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.MOVED_TO_CAMP.ordinal(), point.getName());
                    break;
                case LOCATION:
                    point = LocationsManagerServer.instance().getWorldProfile().getLocation(this.pointId);
                    this.move(point.getDimensionId(), point.getXPos(), point.getYPos(), point.getZPos(), point.getYaw(), point.getPitch());
                    if (PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.LOCATION_TELEPORTATION_COOLDOWN.toString(), 
                            TeleportationConfig.LOCATIONS_TELEPORT_COOLDOWN.getIntValue()) > 0) {
                        CampsManagerServer.instance().getPlayerProfile(playerUUID).getCooldownInfo().movedToLocation();
                        TeleportationMain.network().sendTo(new CPSyncCooldown(), this.player);
                    }
                    OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.MOVED_TO_LOCATION.ordinal(), point.getName());
                    break;
                case JUMP:
                    if (!OxygenHelperServer.isOnline(OxygenHelperServer.uuid(this.target)))
                        return true;
                    this.move(this.target.dimension, (float) this.target.posX, (float) this.target.posY, (float) this.target.posZ, this.target.rotationYawHead, this.target.rotationPitch);
                    if (PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.PLAYER_TELEPORTATION_COOLDOWN.toString(), 
                            TeleportationConfig.PLAYERS_TELEPORT_COOLDOWN.getIntValue()) > 0) {
                        CampsManagerServer.instance().getPlayerProfile(playerUUID).getCooldownInfo().jumped();
                        TeleportationMain.network().sendTo(new CPSyncCooldown(), this.player);
                    }
                    OxygenHelperServer.sendMessage(this.player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.MOVED_TO_PLAYER.ordinal(), OxygenHelperServer.username(this.target));
                    break;
                }        
                CampsLoaderServer.savePlayerData(playerUUID);
                return true;
            }
        }
        return false;
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
