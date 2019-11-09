package austeretony.oxygen_teleportation.common.main;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.privilege.PrivilegeRegistry;

public enum EnumTeleportationPrivilege {

    PROCESS_TELEPORTATION_ON_MOVE("processTeleportationOnMove", EnumValueType.BOOLEAN),
    ENABLE_MOVE_TO_LOCKED_LOCATIONS("enableMoveToLockedLocations", EnumValueType.BOOLEAN),
    ENABLE_CROSS_DIM_TELEPORTATION("enableCrossDimTeleportation", EnumValueType.BOOLEAN),
    ENABLE_TELEPORTATION_TO_ANY_PLAYER("enableTeleportationToAnyPlayer", EnumValueType.BOOLEAN),

    CAMPS_MAX_AMOUNT("campsMaxAmount", EnumValueType.INT),
    CAMP_TELEPORTATION_DELAY_SECONDS("campTeleportDelay", EnumValueType.INT),
    CAMP_TELEPORTATION_COOLDOWN_SECONDS("campTeleportCooldown", EnumValueType.INT),
    ENABLE_FAVORITE_CAMP("enableFavoriteCamp", EnumValueType.BOOLEAN),

    LOCATIONS_MANAGEMENT("locationsManagement", EnumValueType.BOOLEAN),
    LOCATIONS_CREATION("locationsCreation", EnumValueType.BOOLEAN),
    LOCATION_TELEPORTATION_DELAY_SECONDS("locationTeleportDelay", EnumValueType.INT),
    LOCATION_TELEPORTATION_COOLDOWN_SECONDS("locationTeleportCooldown", EnumValueType.INT),

    PLAYER_TELEPORTATION_DELAY_SECONDS("playerTeleportDelay", EnumValueType.INT),
    PLAYER_TELEPORTATION_COOLDOWN_SECONDS("playerTeleportCooldown", EnumValueType.INT),

    CAMP_TELEPORTATION_FEE("campTeleportationFee", EnumValueType.LONG),
    LOCATION_TELEPORTATION_FEE("locationTeleportationFee", EnumValueType.LONG),
    JUMP_TO_PLAYER_FEE("jumpToPlayerFee", EnumValueType.LONG);

    private final String name;

    private final EnumValueType type;

    EnumTeleportationPrivilege(String name, EnumValueType type) {
        this.name = "teleportation:" + name;
        this.type = type;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static void register() {
        for (EnumTeleportationPrivilege privilege : EnumTeleportationPrivilege.values())
            PrivilegeRegistry.registerPrivilege(privilege.name, privilege.type);
    }
}
