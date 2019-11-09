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

public class CampInvitationRequest extends AbstractNotification {

    public final int index;

    public final String ownerUsername, campName;

    public final UUID ownerUUID;

    public final long pointId;

    public CampInvitationRequest(int index, UUID ownerUUID, String ownerUsername, long pointId, String campName) {
        this.index = index;
        this.ownerUUID = ownerUUID;
        this.ownerUsername = ownerUsername;
        this.pointId = pointId;
        this.campName = campName;
    }

    @Override
    public EnumNotification getType() {
        return EnumNotification.REQUEST;
    }

    @Override
    public String getDescription() {
        return "oxygen_teleportation.invitation.request";
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
    public int getExpireTimeSeconds() {
        return TeleportationConfig.INVITATION_REQUEST_EXPIRE_TIME_SECONDS.getIntValue();
    }

    @Override
    public void process() {}

    @Override
    public void accepted(EntityPlayer player) {
        UUID targetUUID = CommonReference.getPersistentUUID(player);
        TeleportationManagerServer.instance().getSharedCampsContainer().invite(this.ownerUUID, this.pointId, targetUUID);
        TeleportationManagerServer.instance().getSharedCampsContainer().setChanged(true);

        OxygenHelperServer.addObservedPlayer(this.ownerUUID, targetUUID);

        OxygenHelperServer.sendStatusMessage((EntityPlayerMP) player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.INVITATION_REQUEST_ACCEPTED.ordinal());
        if (OxygenHelperServer.isPlayerOnline(this.ownerUUID))
            OxygenHelperServer.sendStatusMessage(CommonReference.playerByUUID(this.ownerUUID), TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.INVITATION_REQUEST_ACCEPTED_OWNER.ordinal());
    }

    @Override
    public void rejected(EntityPlayer player) {
        OxygenHelperServer.sendStatusMessage((EntityPlayerMP) player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.INVITATION_REQUEST_REJECTED.ordinal());
        if (OxygenHelperServer.isPlayerOnline(this.ownerUUID))
            OxygenHelperServer.sendStatusMessage(CommonReference.playerByUUID(this.ownerUUID), TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.INVITATION_REQUEST_REJECTED_OWNER.ordinal());
    }

    @Override
    public void expired() {}
}
