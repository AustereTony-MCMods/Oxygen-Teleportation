package austeretony.oxygen_teleportation.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPSyncCooldown;
import net.minecraft.entity.player.EntityPlayerMP;

public class CampTeleportation extends AbstractTeleportation {

    private final int dimension;

    private final float xPos, yPos, zPos, yaw, pitch;

    public CampTeleportation(EntityPlayerMP playerMP, WorldPoint camp) {
        super(
                playerMP, 
                PrivilegeProviderServer.getValue(CommonReference.getPersistentUUID(playerMP), EnumTeleportationPrivilege.CAMP_TELEPORTATION_DELAY_SECONDS.toString(), TeleportationConfig.CAMP_TELEPORTATION_DELAY_SECONDS.getIntValue()), 
                PrivilegeProviderServer.getValue(CommonReference.getPersistentUUID(playerMP), EnumTeleportationPrivilege.CAMP_TELEPORTATION_FEE.toString(), TeleportationConfig.CAMP_TELEPORTATION_FEE.getLongValue()));
        this.dimension = camp.getDimensionId();
        this.xPos = camp.getXPos();
        this.yPos = camp.getYPos();
        this.zPos = camp.getZPos();
        this.yaw = camp.getYaw();
        this.pitch = camp.getPitch();
    }

    @Override
    public void move() {       
        CommonReference.delegateToServerThread(()->CommonReference.teleportPlayer(this.playerMP, this.dimension, this.xPos, this.yPos, this.zPos, this.yaw, this.pitch));
        this.setCooldown();
        OxygenHelperServer.sendStatusMessage(this.playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.MOVED_TO_CAMP.ordinal());
    }

    private void setCooldown() {
        if (PrivilegeProviderServer.getValue(CommonReference.getPersistentUUID(this.playerMP), EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN_SECONDS.toString(), 
                TeleportationConfig.CAMP_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()) > 0) {
            TeleportationPlayerData playerData = TeleportationManagerServer.instance().getPlayersDataContainer().getPlayerData(CommonReference.getPersistentUUID(this.playerMP));
            playerData.getCooldownData().movedToCamp();
            playerData.setChanged(true);
            OxygenMain.network().sendTo(
                    new CPSyncCooldown(playerData.getCooldownData().getLastCampTime(), playerData.getCooldownData().getLastLocationTime(), playerData.getCooldownData().getLastJumpTime()), this.playerMP);
        }
    }
}
