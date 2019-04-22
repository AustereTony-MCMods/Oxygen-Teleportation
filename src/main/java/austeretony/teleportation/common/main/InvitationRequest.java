package austeretony.teleportation.common.main;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.notification.AbstractNotification;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.notification.EnumNotifications;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.config.TeleportationConfig;
import net.minecraft.entity.player.EntityPlayer;

public class InvitationRequest extends AbstractNotification {

    public final int index;

    public final String ownerUsername, campName;

    public final UUID targetUUID, ownerUUID;

    public final long pointId;

    public InvitationRequest(int index, UUID targetUUID, UUID ownerUUID, String ownerUsername, long pointId, String campName) {
        this.index = index;
        this.targetUUID = targetUUID;
        this.ownerUUID = ownerUUID;
        this.ownerUsername = ownerUsername;
        this.pointId = pointId;
        this.campName = campName;
    }

    @Override
    public EnumNotifications getType() {
        return EnumNotifications.REQUEST;
    }

    @Override
    public String getDescription() {
        return "teleportation.invitation.request";
    }

    @Override
    public String[] getArguments() {
        return new String[]{this.ownerUsername, this.campName};
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getExpireTime() {
        return TeleportationConfig.INVITATION_REQUEST_EXPIRE_TIME.getIntValue();
    }

    @Override
    public void accepted(EntityPlayer player) {
        TeleportationManagerServer.instance().getPlayerProfile(this.ownerUUID).inviteToCamp(this.pointId, this.targetUUID, CommonReference.username(player));
        TeleportationManagerServer.instance().getPlayerProfile(this.targetUUID).addOtherCamp(this.pointId, this.ownerUUID);
        TeleportationManagerServer.instance().getCampsLoader().savePlayerDataDelegated(this.ownerUUID);
        TeleportationManagerServer.instance().getCampsLoader().savePlayerDataDelegated(this.targetUUID);
        OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.INVITATION_REQUEST_ACCEPTED.ordinal(), this.ownerUsername, this.campName);
        if (OxygenHelperServer.isOnline(this.ownerUUID))
            OxygenHelperServer.sendMessage(CommonReference.playerByUUID(this.ownerUUID), TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.INVITATION_REQUEST_ACCEPTED_OWNER.ordinal(), this.campName, CommonReference.username(player));
        OxygenHelperServer.setRequesting(this.ownerUUID, false);
    }

    @Override
    public void rejected(EntityPlayer player) {
        OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.INVITATION_REQUEST_REJECTED.ordinal(), this.ownerUsername, this.campName);
        if (OxygenHelperServer.isOnline(this.ownerUUID))
            OxygenHelperServer.sendMessage(CommonReference.playerByUUID(this.ownerUUID), TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessages.INVITATION_REQUEST_REJECTED_OWNER.ordinal(), this.campName, CommonReference.username(player));
        OxygenHelperServer.setRequesting(this.ownerUUID, false);
    }

    @Override
    public void expired() {
        OxygenHelperServer.setRequesting(this.ownerUUID, false);
    }
}
