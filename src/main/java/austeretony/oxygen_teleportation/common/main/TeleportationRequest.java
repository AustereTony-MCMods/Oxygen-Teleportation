package austeretony.oxygen_teleportation.common.main;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.notification.AbstractNotification;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.notification.EnumNotifications;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
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
    public EnumNotifications getType() {
        return EnumNotifications.REQUEST;
    }

    @Override
    public String getDescription() {
        return "teleportation.jump.request";
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
    public int getExpireTime() {
        return TeleportationConfig.JUMP_REQUEST_EXPIRE_TIME.getIntValue();
    }

    @Override
    public void accepted(EntityPlayer player) {
        if (OxygenHelperServer.isOnline(this.senderUUID)) {
            EntityPlayerMP senderPlayerMP = CommonReference.playerByUUID(this.senderUUID);
            OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.JUMP_REQUEST_ACCEPTED_TARGET.ordinal());
            OxygenHelperServer.sendMessage(senderPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.JUMP_REQUEST_ACCEPTED_SENDER.ordinal());
            TeleportationManagerServer.instance().getPlayersManager().move(senderPlayerMP, this.senderUUID, CommonReference.uuid(player));
        } else
            OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.JUMP_REQUEST_VISITOR_OFFLINE.ordinal());
        OxygenHelperServer.setRequesting(this.senderUUID, false);
    }

    @Override
    public void rejected(EntityPlayer player) {
        if (OxygenHelperServer.isOnline(this.senderUUID))
            OxygenHelperServer.sendMessage(CommonReference.playerByUUID(this.senderUUID), TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.JUMP_REQUEST_REJECTED_SENDER.ordinal());
        OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.JUMP_REQUEST_REJECTED_TARGET.ordinal());
        OxygenHelperServer.setRequesting(this.senderUUID, false);
    }

    @Override
    public void expired() {
        OxygenHelperServer.setRequesting(this.senderUUID, false);
    }
}
