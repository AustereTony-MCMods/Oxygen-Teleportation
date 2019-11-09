package austeretony.oxygen_teleportation.server;

import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.currency.CurrencyHelperServer;
import austeretony.oxygen_core.common.inventory.InventoryHelper;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.server.OxygenPlayerData;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_core.server.api.WatcherHelperServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class AbstractTeleportation {

    protected final EntityPlayerMP playerMP;

    protected final double prevX, prevZ;

    protected final long expireTime;

    protected final long fee;

    protected final boolean processOnMove;

    public AbstractTeleportation(EntityPlayerMP playerMP, int delaySeconds, long fee) {
        this.playerMP = playerMP;
        this.prevX = playerMP.posX;
        this.prevZ = playerMP.posZ;
        delaySeconds = delaySeconds < 1 ? 1 : delaySeconds;
        if (delaySeconds > 1)
            OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.PREPARE_FOR_TELEPORTATION.ordinal());
        this.expireTime = System.currentTimeMillis() + delaySeconds * 1000L;
        this.fee = fee;
        this.processOnMove = PrivilegeProviderServer.getValue(CommonReference.getPersistentUUID(playerMP), EnumTeleportationPrivilege.PROCESS_TELEPORTATION_ON_MOVE.toString(), 
                TeleportationConfig.PROCESS_TELEPORTATION_ON_MOVE.getBooleanValue());
    }

    public boolean process() {
        if (this.update()) {
            if (System.currentTimeMillis() >= this.expireTime) {
                if (this.fee > 0L) {
                    if (TeleportationConfig.FEE_MODE.getIntValue() == 1) {
                        if (InventoryHelper.getEqualStackAmount(this.playerMP, TeleportationManagerServer.instance().getFeeStackWrapper()) < this.fee)
                            return true;
                        CommonReference.delegateToServerThread(
                                ()->InventoryHelper.removeEqualStack(this.playerMP, TeleportationManagerServer.instance().getFeeStackWrapper(), (int) this.fee));
                        SoundEventHelperServer.playSoundClient(this.playerMP, OxygenSoundEffects.INVENTORY.id);
                    } else {
                        UUID playerUUID = CommonReference.getPersistentUUID(this.playerMP);
                        if (!CurrencyHelperServer.enoughCurrency(playerUUID, this.fee))
                            return true;
                        CurrencyHelperServer.removeCurrency(playerUUID, this.fee);
                        CurrencyHelperServer.save(playerUUID);
                        
                        WatcherHelperServer.setValue(playerUUID, OxygenPlayerData.CURRENCY_COINS_WATCHER_ID, CurrencyHelperServer.getCurrency(playerUUID));
                        SoundEventHelperServer.playSoundClient(this.playerMP, OxygenSoundEffects.SELL.id);
                    }
                }
                this.move();
                return true;
            }
        } else {
            return true;
        }
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

    public abstract void move();
}
