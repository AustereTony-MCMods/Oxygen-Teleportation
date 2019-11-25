package austeretony.oxygen_teleportation.server;

import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPSyncCooldown;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerTeleportation extends AbstractTeleportation {

    private final EntityPlayerMP target;

    public PlayerTeleportation(EntityPlayerMP playerMP, EntityPlayerMP target) {
        super(
                playerMP, 
                PrivilegeProviderServer.getValue(CommonReference.getPersistentUUID(playerMP), EnumTeleportationPrivilege.PLAYER_TELEPORTATION_DELAY_SECONDS.toString(), TeleportationConfig.PLAYER_TELEPORTATION_DELAY_SECONDS.getIntValue()), 
                PrivilegeProviderServer.getValue(CommonReference.getPersistentUUID(playerMP), EnumTeleportationPrivilege.JUMP_TO_PLAYER_FEE.toString(), TeleportationConfig.JUMP_TO_PLAYER_FEE.getLongValue()));
        this.target = target;
    }

    public boolean update() {
        if (!this.processOnMove) {
            if (this.playerMP.posX != this.prevX || this.playerMP.posZ != this.prevZ) {
                OxygenHelperServer.sendStatusMessage(this.playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.TELEPORTATION_ABORTED.ordinal());
                return false;
            }
        }
        if (!OxygenHelperServer.isPlayerOnline(CommonReference.getPersistentUUID(this.target)))
            return false;
        return true;
    }

    @Override
    public void move() {       
        CommonReference.delegateToServerThread(()->CommonReference.teleportPlayer(this.playerMP, this.target.dimension, this.target.posX, this.target.posY, this.target.posZ, this.target.rotationYawHead, this.target.rotationPitch));
        this.setCooldown();
        OxygenHelperServer.sendStatusMessage(this.playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.MOVED_TO_PLAYER.ordinal());
    }

    private void setCooldown() {
        if (PrivilegeProviderServer.getValue(CommonReference.getPersistentUUID(this.playerMP), EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.toString(), 
                TeleportationConfig.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()) > 0) {
            TeleportationPlayerData playerData = TeleportationManagerServer.instance().getPlayersDataContainer().getPlayerData(CommonReference.getPersistentUUID(this.playerMP));
            playerData.getCooldownData().jumped();
            playerData.setChanged(true);

            UUID playerUUID = CommonReference.getPersistentUUID(this.playerMP);
            int 
            campCooldownSeconds = PrivilegeProviderServer.getValue(playerUUID, EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN_SECONDS.toString(), TeleportationConfig.CAMP_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()),
            campCooldownLeftSeconds = (int) MathUtils.clamp((playerData.getCooldownData().getNextCampTime() - System.currentTimeMillis()) / 1000, 0L, campCooldownSeconds),

            locationCooldownSeconds = PrivilegeProviderServer.getValue(playerUUID, EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.toString(), TeleportationConfig.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()),
            locationCooldownLeftSeconds = (int) MathUtils.clamp((playerData.getCooldownData().getNextLocationTime() - System.currentTimeMillis()) / 1000, 0L, locationCooldownSeconds),

            jumpCooldownSeconds = PrivilegeProviderServer.getValue(playerUUID, EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.toString(), TeleportationConfig.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()),
            jumpCooldownLeftSeconds = (int) MathUtils.clamp((playerData.getCooldownData().getNextJumpTime() - System.currentTimeMillis()) / 1000, 0L, jumpCooldownSeconds);

            OxygenMain.network().sendTo(new CPSyncCooldown(campCooldownLeftSeconds, locationCooldownLeftSeconds, jumpCooldownLeftSeconds), this.playerMP);
        }
    }
}
