package austeretony.oxygen_teleportation.server;

import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
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
                PrivilegesProviderServer.getAsInt(CommonReference.getPersistentUUID(playerMP), EnumTeleportationPrivilege.PLAYER_TELEPORTATION_DELAY_SECONDS.id(), TeleportationConfig.PLAYER_TELEPORTATION_DELAY_SECONDS.asInt()), 
                PrivilegesProviderServer.getAsLong(CommonReference.getPersistentUUID(playerMP), EnumTeleportationPrivilege.PLAYER_TELEPORTATION_FEE.id(), TeleportationConfig.TELEPORTATION_TO_PLAYER_FEE.asLong()));
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
        if (PrivilegesProviderServer.getAsInt(CommonReference.getPersistentUUID(this.playerMP), EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.id(), 
                TeleportationConfig.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.asInt()) > 0) {
            TeleportationPlayerData playerData = TeleportationManagerServer.instance().getPlayersDataContainer().getPlayerData(CommonReference.getPersistentUUID(this.playerMP));
            playerData.getCooldownData().jumped();
            playerData.setChanged(true);

            UUID playerUUID = CommonReference.getPersistentUUID(this.playerMP);
            int 
            campCooldownSeconds = PrivilegesProviderServer.getAsInt(playerUUID, EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN_SECONDS.id(), TeleportationConfig.CAMP_TELEPORTATION_COOLDOWN_SECONDS.asInt()),
            campCooldownLeftSeconds = (int) MathUtils.clamp((playerData.getCooldownData().getNextCampTime() - System.currentTimeMillis()) / 1000, 0L, campCooldownSeconds),

            locationCooldownSeconds = PrivilegesProviderServer.getAsInt(playerUUID, EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.id(), TeleportationConfig.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.asInt()),
            locationCooldownLeftSeconds = (int) MathUtils.clamp((playerData.getCooldownData().getNextLocationTime() - System.currentTimeMillis()) / 1000, 0L, locationCooldownSeconds),

            jumpCooldownSeconds = PrivilegesProviderServer.getAsInt(playerUUID, EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.id(), TeleportationConfig.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.asInt()),
            jumpCooldownLeftSeconds = (int) MathUtils.clamp((playerData.getCooldownData().getNextJumpTime() - System.currentTimeMillis()) / 1000, 0L, jumpCooldownSeconds);

            OxygenMain.network().sendTo(new CPSyncCooldown(campCooldownLeftSeconds, locationCooldownLeftSeconds, jumpCooldownLeftSeconds), this.playerMP);
        }
    }
}
