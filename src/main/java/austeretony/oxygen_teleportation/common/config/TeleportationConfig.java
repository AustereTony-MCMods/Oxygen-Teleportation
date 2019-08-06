package austeretony.oxygen_teleportation.common.config;

import java.util.List;

import austeretony.oxygen.common.api.config.AbstractConfigHolder;
import austeretony.oxygen.common.api.config.ConfigValue;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class TeleportationConfig extends AbstractConfigHolder {

    public static final ConfigValue
    IMAGE_WIDTH = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "image_width"),
    IMAGE_HEIGHT = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "image_height"),
    PROCESS_TELEPORTATION_ON_MOVE = new ConfigValue(ConfigValue.EnumValueType.BOOLEAN, "main", "process_teleportation_on_move"),
    ENABLE_CROSS_DIM_TELEPORTATION = new ConfigValue(ConfigValue.EnumValueType.BOOLEAN, "main", "enable_cross_dim_teleportation"),

    ENABLE_CAMPS = new ConfigValue(ConfigValue.EnumValueType.BOOLEAN, "camps", "enable_camps"),
    CAMPS_MAX_AMOUNT = new ConfigValue(ConfigValue.EnumValueType.INT, "camps", "max_amount"),
    CAMPS_TELEPORT_DELAY = new ConfigValue(ConfigValue.EnumValueType.INT, "camps", "teleport_delay_seconds"),
    CAMPS_TELEPORT_COOLDOWN = new ConfigValue(ConfigValue.EnumValueType.INT, "camps", "cooldown_seconds"),
    ENABLE_FAVORITE_CAMP = new ConfigValue(ConfigValue.EnumValueType.BOOLEAN, "camps", "enable_favorite_camp"),
    ENABLE_CAMP_INVITATIONS = new ConfigValue(ConfigValue.EnumValueType.BOOLEAN, "camps", "enable_camp_invitations"),
    MAX_INVITED_PLAYERS_PER_CAMP = new ConfigValue(ConfigValue.EnumValueType.INT, "camps", "max_invited_players_per_camp"),
    INVITATION_REQUEST_EXPIRE_TIME = new ConfigValue(ConfigValue.EnumValueType.INT, "camps", "invitation_request_expire_time_seconds"),

    ENABLE_LOCATIONS = new ConfigValue(ConfigValue.EnumValueType.BOOLEAN, "locations", "enable_locations"),
    LOCATIONS_MAX_AMOUNT = new ConfigValue(ConfigValue.EnumValueType.INT, "locations", "max_amount"),
    LOCATIONS_TELEPORT_DELAY = new ConfigValue(ConfigValue.EnumValueType.INT, "locations", "teleport_delay_seconds"),
    LOCATIONS_TELEPORT_COOLDOWN = new ConfigValue(ConfigValue.EnumValueType.INT, "locations", "cooldown_seconds"),
    ALLOW_LOCATIONS_CREATION_FOR_ALL = new ConfigValue(ConfigValue.EnumValueType.BOOLEAN, "locations", "allow_locations_creation_for_all"),

    ENABLE_PLAYERS = new ConfigValue(ConfigValue.EnumValueType.BOOLEAN, "players", "enable_players"),
    PLAYERS_TELEPORT_DELAY = new ConfigValue(ConfigValue.EnumValueType.INT, "players", "teleport_delay_seconds"),
    PLAYERS_TELEPORT_COOLDOWN = new ConfigValue(ConfigValue.EnumValueType.INT, "players", "cooldown_seconds"),
    DEFAULT_JUMP_PROFILE = new ConfigValue(ConfigValue.EnumValueType.INT, "players", "default_jump_profile"),
    JUMP_REQUEST_EXPIRE_TIME = new ConfigValue(ConfigValue.EnumValueType.INT, "players", "jump_request_expire_seconds");

    @Override
    public String getModId() {
        return TeleportationMain.MODID;
    }

    @Override
    public String getVersion() {
        return TeleportationMain.VERSION_CUSTOM;
    }

    @Override
    public String getExternalPath() {
        return CommonReference.getGameFolder() + "/config/oxygen/teleportation.json";
    }

    @Override
    public String getInternalPath() {
        return "assets/oxygen_teleportation/teleportation.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(IMAGE_WIDTH);
        values.add(IMAGE_HEIGHT);
        values.add(PROCESS_TELEPORTATION_ON_MOVE);
        values.add(ENABLE_CROSS_DIM_TELEPORTATION);

        values.add(ENABLE_CAMPS);
        values.add(CAMPS_MAX_AMOUNT);
        values.add(CAMPS_TELEPORT_DELAY);
        values.add(CAMPS_TELEPORT_COOLDOWN);
        values.add(ENABLE_FAVORITE_CAMP);
        values.add(ENABLE_CAMP_INVITATIONS);
        values.add(MAX_INVITED_PLAYERS_PER_CAMP);
        values.add(INVITATION_REQUEST_EXPIRE_TIME);

        values.add(ENABLE_LOCATIONS);
        values.add(LOCATIONS_MAX_AMOUNT);
        values.add(LOCATIONS_TELEPORT_DELAY);
        values.add(LOCATIONS_TELEPORT_COOLDOWN);
        values.add(ALLOW_LOCATIONS_CREATION_FOR_ALL);

        values.add(ENABLE_PLAYERS);
        values.add(PLAYERS_TELEPORT_DELAY);
        values.add(PLAYERS_TELEPORT_COOLDOWN);
        values.add(DEFAULT_JUMP_PROFILE);
        values.add(JUMP_REQUEST_EXPIRE_TIME);
    }

    @Override
    public boolean sync() {
        return true;
    }
}
