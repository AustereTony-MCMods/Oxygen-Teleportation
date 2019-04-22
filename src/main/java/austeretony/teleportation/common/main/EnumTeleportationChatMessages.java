package austeretony.teleportation.common.main;

import austeretony.oxygen.common.core.api.ClientReference;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum EnumTeleportationChatMessages {

    CAMP_CREATED,
    CAMP_REMOVED,
    SET_FAVORITE,
    CAMP_LOCKED,
    CAMP_UNLOCKED,
    LOCATION_CREATED,
    LOCATION_LOCKED,
    LOCATION_UNLOCKED,
    LOCATION_REMOVED,
    PREPARE_FOR_TELEPORTATION,
    MOVED_TO_CAMP,
    MOVED_TO_LOCATION,
    MOVED_TO_PLAYER,
    JUMP_PROFILE_CHANGED,
    JUMP_REQUEST_ACCEPTED_TARGET,
    JUMP_REQUEST_REJECTED_TARGET,
    JUMP_REQUEST_ACCEPTED_SENDER,
    JUMP_REQUEST_REJECTED_SENDER,
    JUMP_REQUEST_TARGET_OFFLINE,
    JUMP_REQUEST_VISITOR_OFFLINE,
    TELEPORTATION_ABORTED,
    CROSS_DIM_TELEPORTSTION_DISABLED,
    INVITATION_REQUEST_ACCEPTED_OWNER,
    INVITATION_REQUEST_REJECTED_OWNER,
    INVITATION_REQUEST_ACCEPTED,
    INVITATION_REQUEST_REJECTED,
    UNINVITED,
    CAMP_LEFT;

    @SideOnly(Side.CLIENT)
    public void show(String... args) {
        switch (this) {
        case CAMP_CREATED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.campCreated", args[0]));
            break;
        case CAMP_REMOVED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.campRemoved", args[0]));
            break;
        case SET_FAVORITE:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.setFavorite", args[0]));
            break;
        case CAMP_LOCKED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.campLocked", args[0]));
            break;
        case CAMP_UNLOCKED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.campUnlocked", args[0]));
            break;
        case LOCATION_CREATED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.locationCreated", args[0]));
            break;
        case LOCATION_LOCKED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.locationLocked", args[0]));
            break;
        case LOCATION_UNLOCKED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.locationUnlocked", args[0]));
            break;
        case LOCATION_REMOVED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.locationRemoved", args[0]));
            break;
        case PREPARE_FOR_TELEPORTATION:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.prepareForTeleportation", args[0]));
            break; 
        case MOVED_TO_CAMP:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.movedToCamp", args[0]));
            break; 
        case MOVED_TO_LOCATION:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.movedToLocation", args[0]));
            break; 
        case MOVED_TO_PLAYER:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.movedToPlayer", args[0]));
            break;
        case JUMP_PROFILE_CHANGED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.jumpProfileChanged", args[0]));
            break;
        case JUMP_REQUEST_ACCEPTED_TARGET:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.jumpRequestAccepted"));
            break;
        case JUMP_REQUEST_REJECTED_TARGET:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.jumpRequestRejected"));
            break;
        case JUMP_REQUEST_ACCEPTED_SENDER:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.jumpRequestAcceptedVisitor"));
            break;
        case JUMP_REQUEST_REJECTED_SENDER:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.jumpRequestRejectedVisitor"));
            break;
        case JUMP_REQUEST_TARGET_OFFLINE:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.jumpRequestTargetOffline"));
            break;
        case JUMP_REQUEST_VISITOR_OFFLINE:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.jumpRequestVisitorOffline"));
            break;
        case TELEPORTATION_ABORTED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.teleportationAborted"));
            break;
        case CROSS_DIM_TELEPORTSTION_DISABLED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.crossDimTeleportationDisabled"));
            break;
        case INVITATION_REQUEST_ACCEPTED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.invitationRequestAccepted", args[0], args[1]));
            break;
        case INVITATION_REQUEST_REJECTED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.invitationRequestRejected", args[0], args[1]));
            break;
        case INVITATION_REQUEST_ACCEPTED_OWNER:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.invitationRequestAcceptedOwner", args[0], args[1]));
            break;
        case INVITATION_REQUEST_REJECTED_OWNER:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.invitationRequestRejectedOwner", args[0], args[1]));
            break;
        case UNINVITED:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.playerUninvited"));
            break;
        case CAMP_LEFT:
            ClientReference.showMessage(new TextComponentTranslation("teleportation.message.campLeft", args[0]));
            break;
        }
    }
}
