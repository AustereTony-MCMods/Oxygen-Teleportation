package austeretony.teleportation.common.main;

public enum EnumPrivileges {

    PROCESS_TELEPORTATION_ON_MOVE(":processTeleportationOnMove"),
    ENABLE_CROSS_DIM_TELEPORTATION(":enableCrossDimTeleportation"),
    CAMPS_MAX_AMOUNT(":campsMaxAmount"),
    CAMP_TELEPORTATION_DELAY(":campTeleportDelay"),
    CAMP_TELEPORTATION_COOLDOWN(":campTeleportCooldown"),
    LOCATIONS_CREATION(":locationsCreation"),
    LOCATION_TELEPORTATION_DELAY(":locationTeleportDelay"),
    LOCATION_TELEPORTATION_COOLDOWN(":locationTeleportCooldown"),
    PLAYER_TELEPORTATION_DELAY(":playerTeleportDelay"),
    PLAYER_TELEPORTATION_COOLDOWN(":playerTeleportCooldown");

    private final String name;

    EnumPrivileges(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return TeleportationMain.MODID + this.name;
    }
}
