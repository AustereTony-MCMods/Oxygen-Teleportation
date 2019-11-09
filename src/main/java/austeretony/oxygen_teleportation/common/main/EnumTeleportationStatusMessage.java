package austeretony.oxygen_teleportation.common.main;

import austeretony.oxygen_core.client.api.ClientReference;

public enum EnumTeleportationStatusMessage {

    CAMP_CREATED("campCreated"),
    CAMP_EDITED("campEdited"),
    CAMP_REMOVED("campRemoved"),
    CAMP_LOCKED("campLocked"),
    CAMP_UNLOCKED("campUnlocked"),
    FAVORITE_CAMP_SET("favoriteCampSet"),
    LOCATION_CREATED("locationCreated"),
    LOCATION_EDITED("locationEdited"),
    LOCATION_LOCKED("locationLocked"),
    LOCATION_UNLOCKED("locationUnlocked"),
    LOCATION_REMOVED("locationRemoved"),
    PREPARE_FOR_TELEPORTATION("prepareForTeleportation"),
    MOVED_TO_CAMP("movedToCamp"),
    MOVED_TO_LOCATION("movedToLocation"),
    MOVED_TO_PLAYER("movedToPlayer"),    
    JUMP_PROFILE_CHANGED("jumpProfileChanged"),
    JUMP_REQUEST_ACCEPTED_TARGET("jumpRequestAccepted"),
    JUMP_REQUEST_REJECTED_TARGET("jumpRequestRejected"),
    JUMP_REQUEST_ACCEPTED_SENDER("jumpRequestAcceptedVisitor"),
    JUMP_REQUEST_REJECTED_SENDER("jumpRequestRejectedVisitor"),
    JUMP_REQUEST_TARGET_OFFLINE("jumpRequestTargetOffline"),
    JUMP_REQUEST_VISITOR_OFFLINE("jumpRequestVisitorOffline"),
    TELEPORTATION_ABORTED("teleportationAborted"),
    CROSS_DIM_TELEPORTSTION_DISABLED("crossDimTeleportationDisabled"),
    INVITATION_REQUEST_ACCEPTED_OWNER("invitationRequestAcceptedOwner"),
    INVITATION_REQUEST_REJECTED_OWNER("invitationRequestRejectedOwner"),
    INVITATION_REQUEST_ACCEPTED("invitationRequestAccepted"),
    INVITATION_REQUEST_REJECTED("invitationRequestRejected"),
    UNINVITED("playerUninvited"),
    CAMP_LEFT("campLeft"),
    FEE_STACK_SPECIFIED("feeStackSpecified");

    private final String status;

    EnumTeleportationStatusMessage(String status) {
        this.status = "oxygen_teleportation.status." + status;
    }

    public String localizedName() {
        return ClientReference.localize(this.status);
    }
}
