package austeretony.oxygen_teleportation.common.main;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.privilege.PrivilegeRegistry;

public enum EnumTeleportationPrivilege {

    PROCESS_TELEPORTATION_ON_MOVE("teleportation:teleportOnMove", 100, EnumValueType.BOOLEAN),
    ENABLE_CROSS_DIM_TELEPORTATION("teleportation:crossDimTeleportation", 101, EnumValueType.BOOLEAN),
    ENABLE_TELEPORTATION_TO_LOCKED_LOCATIONS("teleportation:teleportToLockedLocations", 102, EnumValueType.BOOLEAN),
    ENABLE_TELEPORTATION_TO_ANY_PLAYER("teleportation:teleportToAnyPlayer", 103, EnumValueType.BOOLEAN),
    ALLOW_CAMPS_USAGE("teleportation:allowCampsUsage", 104, EnumValueType.BOOLEAN),
    ALLOW_LOCATIONS_USAGE("teleportation:allowLocationsUsage", 105, EnumValueType.BOOLEAN),
    ALLOW_PLAYER_TELEPORTATION_USAGE("teleportation:allowPlayerTeleportationUsage", 106, EnumValueType.BOOLEAN),

    CAMPS_MAX_AMOUNT("teleportation:campsMaxAmount", 110, EnumValueType.INT),
    CAMP_TELEPORTATION_DELAY_SECONDS("teleportation:campTeleportDelay", 111, EnumValueType.INT),
    CAMP_TELEPORTATION_COOLDOWN_SECONDS("teleportation:campTeleportCooldown", 112, EnumValueType.INT),
    ENABLE_FAVORITE_CAMP("teleportation:enableFavoriteCamp", 113, EnumValueType.BOOLEAN),

    LOCATIONS_MANAGEMENT("teleportation:locationsManagement", 120, EnumValueType.BOOLEAN),
    LOCATIONS_CREATION("teleportation:locationsCreation", 121, EnumValueType.BOOLEAN),
    LOCATION_TELEPORTATION_DELAY_SECONDS("teleportation:locationTeleportDelay", 122, EnumValueType.INT),
    LOCATION_TELEPORTATION_COOLDOWN_SECONDS("teleportation:locationTeleportCooldown", 123, EnumValueType.INT),

    PLAYER_TELEPORTATION_DELAY_SECONDS("teleportation:playerTeleportDelay", 130, EnumValueType.INT),
    PLAYER_TELEPORTATION_COOLDOWN_SECONDS("teleportation:playerTeleportCooldown", 131, EnumValueType.INT),

    CAMP_TELEPORTATION_FEE("teleportation:campTeleportationFee", 140, EnumValueType.LONG),
    LOCATION_TELEPORTATION_FEE("teleportation:locationTeleportationFee", 141, EnumValueType.LONG),
    PLAYER_TELEPORTATION_FEE("teleportation:playerTeleportationFee", 142, EnumValueType.LONG);

    private final String name;

    private final int id;

    private final EnumValueType type;

    EnumTeleportationPrivilege(String name, int id, EnumValueType type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public int id() {
        return id;
    }

    public static void register() {
        for (EnumTeleportationPrivilege privilege : values())
            PrivilegeRegistry.registerPrivilege(privilege.name, privilege.id, privilege.type);
    }
}
