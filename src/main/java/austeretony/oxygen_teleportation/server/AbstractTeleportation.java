package austeretony.oxygen_teleportation.server;

import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.process.AbstractTemporaryProcess;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.InventoryProviderServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class AbstractTeleportation extends AbstractTemporaryProcess {

    protected final EntityPlayerMP playerMP;

    protected final double prevX, prevZ;

    protected final long fee;

    protected final boolean processOnMove;

    protected int delaySeconds;

    public static final long TELEPORTATION_PROCESS_ID = 1000L;

    public AbstractTeleportation(EntityPlayerMP playerMP, int delaySeconds, long fee) {
        this.playerMP = playerMP;
        this.prevX = playerMP.posX;
        this.prevZ = playerMP.posZ;
        this.delaySeconds = delaySeconds < 1 ? 1 : delaySeconds;
        if (this.delaySeconds > 1)
            OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.PREPARE_FOR_TELEPORTATION.ordinal());
        this.fee = fee;
        this.processOnMove = PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(playerMP), EnumTeleportationPrivilege.PROCESS_TELEPORTATION_ON_MOVE.id(), 
                TeleportationConfig.PROCESS_TELEPORTATION_ON_MOVE.asBoolean());
    }

    @Override
    public long getId() {
        return TELEPORTATION_PROCESS_ID;
    }

    @Override
    public int getExpireTimeSeconds() {
        return this.delaySeconds;
    }

    @Override
    public void process() {}

    @Override
    public boolean isExpired() {
        if (this.update()) {
            if (System.currentTimeMillis() >= this.getExpirationTimeStamp()) {
                this.expired();
                return true;
            }
        } else
            return true;
        return false;
    }

    public boolean update() {
        if (!this.processOnMove) {
            if (this.playerMP.posX != this.prevX || this.playerMP.posZ != this.prevZ) {
                OxygenHelperServer.sendStatusMessage(this.playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.TELEPORTATION_ABORTED.ordinal());
                return false;
            }
        }
        return true;
    }

    @Override
    public void expired() {
        if (this.fee > 0L) {
            if (TeleportationConfig.FEE_MODE.asInt() == 1) {
                if (InventoryProviderServer.getPlayerInventory().getEqualItemAmount(this.playerMP, TeleportationManagerServer.instance().getFeeStackWrapper()) < this.fee)
                    return;

                InventoryProviderServer.getPlayerInventory().removeItem(this.playerMP, TeleportationManagerServer.instance().getFeeStackWrapper(), (int) this.fee);
                SoundEventHelperServer.playSoundClient(this.playerMP, OxygenSoundEffects.INVENTORY_OPERATION.getId());
            } else {
                UUID playerUUID = CommonReference.getPersistentUUID(this.playerMP);
                if (!CurrencyHelperServer.enoughCurrency(playerUUID, this.fee, OxygenMain.COMMON_CURRENCY_INDEX))
                    return;

                CurrencyHelperServer.removeCurrency(playerUUID, this.fee, OxygenMain.COMMON_CURRENCY_INDEX);

                SoundEventHelperServer.playSoundClient(this.playerMP, OxygenSoundEffects.RINGING_COINS.getId());
            }
        }
        this.move();
    }

    public abstract void move();
}
