package austeretony.oxygen_teleportation.common.main;

import austeretony.oxygen.client.core.api.ClientReference;
import net.minecraft.util.text.TextComponentTranslation;

public enum EnumTeleportationChatMessage {

    CAMP_CREATED,
    CAMP_REMOVED,
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
    CAMP_LEFT,
    FEE_STACK_SPECIFIED;

    public void show(String... args) {
        switch (this) {
        case CAMP_CREATED:      
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.campCreated", args[0]));
            break;
        case CAMP_REMOVED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.campRemoved", args[0]));
            break;
        case CAMP_LOCKED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.campLocked", args[0]));
            break;
        case CAMP_UNLOCKED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.campUnlocked", args[0]));
            break;
        case LOCATION_CREATED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.locationCreated", args[0]));
            break;
        case LOCATION_LOCKED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.locationLocked", args[0]));
            break;
        case LOCATION_UNLOCKED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.locationUnlocked", args[0]));
            break;
        case LOCATION_REMOVED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.locationRemoved", args[0]));
            break;
        case PREPARE_FOR_TELEPORTATION:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.prepareForTeleportation", args[0]));
            break; 
        case MOVED_TO_CAMP:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.movedToCamp", args[0]));
            break; 
        case MOVED_TO_LOCATION:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.movedToLocation", args[0]));
            break; 
        case MOVED_TO_PLAYER:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.movedToPlayer", args[0]));
            break;
        case JUMP_PROFILE_CHANGED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.jumpProfileChanged", args[0]));
            break;
        case JUMP_REQUEST_ACCEPTED_TARGET:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.jumpRequestAccepted"));
            break;
        case JUMP_REQUEST_REJECTED_TARGET:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.jumpRequestRejected"));
            break;
        case JUMP_REQUEST_ACCEPTED_SENDER:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.jumpRequestAcceptedVisitor"));
            break;
        case JUMP_REQUEST_REJECTED_SENDER:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.jumpRequestRejectedVisitor"));
            break;
        case JUMP_REQUEST_TARGET_OFFLINE:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.jumpRequestTargetOffline"));
            break;
        case JUMP_REQUEST_VISITOR_OFFLINE:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.jumpRequestVisitorOffline"));
            break;
        case TELEPORTATION_ABORTED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.teleportationAborted"));
            break;
        case CROSS_DIM_TELEPORTSTION_DISABLED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.crossDimTeleportationDisabled"));
            break;
        case INVITATION_REQUEST_ACCEPTED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.invitationRequestAccepted", args[0], args[1]));
            break;
        case INVITATION_REQUEST_REJECTED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.invitationRequestRejected", args[0], args[1]));
            break;
        case INVITATION_REQUEST_ACCEPTED_OWNER:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.invitationRequestAcceptedOwner", args[0], args[1]));
            break;
        case INVITATION_REQUEST_REJECTED_OWNER:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.invitationRequestRejectedOwner", args[0], args[1]));
            break;
        case UNINVITED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.playerUninvited"));
            break;
        case CAMP_LEFT:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.campLeft", args[0]));
            break;
        case FEE_STACK_SPECIFIED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_teleportation.message.feeStackSpecified"));
            break;
        }
    }
}
