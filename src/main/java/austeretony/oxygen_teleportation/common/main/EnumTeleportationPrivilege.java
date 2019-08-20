package austeretony.oxygen_teleportation.common.main;

import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;

public enum EnumTeleportationPrivilege {

    PROCESS_TELEPORTATION_ON_MOVE(":processTeleportationOnMove"),
    ENABLE_MOVE_TO_LOCKED_LOCATIONS(":enableMoveToLockedLocations"),
    ENABLE_CROSS_DIM_TELEPORTATION(":enableCrossDimTeleportation"),
    ENABLE_TELEPORTATION_TO_ANY_PLAYER(":enableTeleportationToAnyPlayer"),
    CAMPS_MAX_AMOUNT(":campsMaxAmount"),
    CAMP_TELEPORTATION_DELAY(":campTeleportDelay"),
    CAMP_TELEPORTATION_COOLDOWN(":campTeleportCooldown"),
    ENABLE_FAVORITE_CAMP(":enableFavoriteCamp"),
    LOCATIONS_MANAGEMENT(":locationsManagement"),
    LOCATIONS_CREATION(":locationsCreation"),
    LOCATION_TELEPORTATION_DELAY(":locationTeleportDelay"),
    LOCATION_TELEPORTATION_COOLDOWN(":locationTeleportCooldown"),
    PLAYER_TELEPORTATION_DELAY(":playerTeleportDelay"),
    PLAYER_TELEPORTATION_COOLDOWN(":playerTeleportCooldown"),
    CAMP_TELEPORTATION_FEE(":campTeleportationFee"),
    LOCATION_TELEPORTATION_FEE(":locationTeleportationFee"),
    JUMP_TO_PLAYER_FEE(":jumpToPlayerFee");

    private final String name;

    EnumTeleportationPrivilege(String name) {
        this.name = name;
        PrivilegeProviderServer.registerPrivilege(TeleportationMain.MODID + name, TeleportationMain.NAME);
    }

    @Override
    public String toString() {
        return TeleportationMain.MODID + this.name;
    }
}
