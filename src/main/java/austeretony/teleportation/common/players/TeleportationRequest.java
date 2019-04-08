package austeretony.teleportation.common.players;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.notification.AbstractNotification;
import austeretony.oxygen.common.notification.EnumNotifications;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumChatMessages;
import austeretony.teleportation.common.main.TeleportationMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportationRequest extends AbstractNotification {

    public final int index;

    public final String visitorUsername;

    public final UUID targetUUID, visitorUUID;

    public TeleportationRequest(int index, UUID targetUUID, UUID visitorUUID, String visitorUsername) {
        this.index = index;
        this.targetUUID = targetUUID;
        this.visitorUUID = visitorUUID;
        this.visitorUsername = visitorUsername;
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
        return new String[] {this.visitorUsername};
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
        if (OxygenHelperServer.isOnline(this.visitorUUID)) {
            EntityPlayerMP visitorPlayerMP = CommonReference.playerByUUID(this.visitorUUID);
            OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_ACCEPTED.ordinal(), CommonReference.username(visitorPlayerMP));
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_ACCEPTED_VISITOR.ordinal(), CommonReference.username(player));
            TeleportationManagerServer.instance().getPlayersManager().move(visitorPlayerMP, this.visitorUUID, CommonReference.uuid(player));
        } else
            OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_VISITOR_OFFLINE.ordinal());
        TeleportationManagerServer.instance().getPlayersManager().resetRequest(this.targetUUID);
        TeleportationManagerServer.instance().getPlayersManager().resetRequesting(this.visitorUUID);
    }

    @Override
    public void rejected(EntityPlayer player) {
        if (OxygenHelperServer.isOnline(this.visitorUUID)) {
            EntityPlayerMP visitorPlayerMP = CommonReference.playerByUUID(this.visitorUUID);
            OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_REJECTED.ordinal(), CommonReference.username(visitorPlayerMP));
            OxygenHelperServer.sendMessage(visitorPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_REJECTED_VISITOR.ordinal(), CommonReference.username(player));
        } else
            OxygenHelperServer.sendMessage(player, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.JUMP_REQUEST_VISITOR_OFFLINE.ordinal());
        TeleportationManagerServer.instance().getPlayersManager().resetRequest(this.targetUUID);
        TeleportationManagerServer.instance().getPlayersManager().resetRequesting(this.visitorUUID);
    }

    @Override
    public void expired() {
        TeleportationManagerServer.instance().getPlayersManager().resetRequest(this.targetUUID);
        TeleportationManagerServer.instance().getPlayersManager().resetRequesting(this.visitorUUID);
    }
}
