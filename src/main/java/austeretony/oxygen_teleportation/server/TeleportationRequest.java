package austeretony.oxygen_teleportation.server;

import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.notification.AbstractNotification;
import austeretony.oxygen_core.common.notification.EnumNotification;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportationRequest extends AbstractNotification {

    public final int index;

    public final String senderUsername;

    public final UUID senderUUID;

    public TeleportationRequest(int index, UUID senderUUID, String senderUsername) {
        this.index = index;
        this.senderUUID = senderUUID;
        this.senderUsername = senderUsername;
    }

    @Override
    public EnumNotification getType() {
        return EnumNotification.REQUEST;
    }

    @Override
    public String getDescription() {
        return "oxygen_teleportation.jump.request";
    }

    @Override
    public String[] getArguments() {
        return new String[] {this.senderUsername};
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getExpireTimeSeconds() {
        return TeleportationConfig.JUMP_REQUEST_EXPIRE_TIME.getIntValue();
    }

    @Override
    public void process() {}

    @Override
    public void accepted(EntityPlayer player) {
        if (OxygenHelperServer.isPlayerOnline(this.senderUUID)) {
            EntityPlayerMP senderPlayerMP = CommonReference.playerByUUID(this.senderUUID);
            OxygenHelperServer.sendStatusMessage((EntityPlayerMP) player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.JUMP_REQUEST_ACCEPTED_TARGET.ordinal());
            OxygenHelperServer.sendStatusMessage(senderPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.JUMP_REQUEST_ACCEPTED_SENDER.ordinal());
            TeleportationManagerServer.instance().getPlayersDataManager().move(senderPlayerMP, this.senderUUID, CommonReference.getPersistentUUID(player));
        } else
            OxygenHelperServer.sendStatusMessage((EntityPlayerMP) player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.JUMP_REQUEST_VISITOR_OFFLINE.ordinal());
    }

    @Override
    public void rejected(EntityPlayer player) {
        if (OxygenHelperServer.isPlayerOnline(this.senderUUID))
            OxygenHelperServer.sendStatusMessage(CommonReference.playerByUUID(this.senderUUID), TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.JUMP_REQUEST_REJECTED_SENDER.ordinal());
        OxygenHelperServer.sendStatusMessage((EntityPlayerMP) player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.JUMP_REQUEST_REJECTED_TARGET.ordinal());
    }

    @Override
    public void expired() {}
}
