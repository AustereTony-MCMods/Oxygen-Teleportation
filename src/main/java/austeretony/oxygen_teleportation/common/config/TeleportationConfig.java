package austeretony.oxygen_teleportation.common.config;

import java.util.List;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.config.AbstractConfigHolder;
import austeretony.oxygen_core.common.api.config.ConfigValueImpl;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class TeleportationConfig extends AbstractConfigHolder {

    public static final ConfigValue
    CAMPS_SAVE_DELAY_MINUTES = new ConfigValueImpl(EnumValueType.INT, "setup", "camps_save_delay_minutes"),
    LOCATIONS_SAVE_DELAY_MINUTES = new ConfigValueImpl(EnumValueType.INT, "setup", "locations_save_delay_minutes"),

    IMAGE_WIDTH = new ConfigValueImpl(EnumValueType.INT, "main", "image_width"),
    IMAGE_HEIGHT = new ConfigValueImpl(EnumValueType.INT, "main", "image_height"),
    PROCESS_TELEPORTATION_ON_MOVE = new ConfigValueImpl(EnumValueType.BOOLEAN, "main", "process_teleportation_on_move"),
    ENABLE_CROSS_DIM_TELEPORTATION = new ConfigValueImpl(EnumValueType.BOOLEAN, "main", "enable_cross_dim_teleportation"),

    ENABLE_CAMPS = new ConfigValueImpl(EnumValueType.BOOLEAN, "camps", "enable_camps"),
    CAMPS_MAX_AMOUNT = new ConfigValueImpl(EnumValueType.INT, "camps", "max_amount"),
    CAMP_TELEPORTATION_DELAY_SECONDS = new ConfigValueImpl(EnumValueType.INT, "camps", "teleport_delay_seconds"),
    CAMP_TELEPORTATION_COOLDOWN_SECONDS = new ConfigValueImpl(EnumValueType.INT, "camps", "cooldown_seconds"),
    ENABLE_FAVORITE_CAMP = new ConfigValueImpl(EnumValueType.BOOLEAN, "camps", "enable_favorite_camp"),
    ENABLE_CAMP_INVITATIONS = new ConfigValueImpl(EnumValueType.BOOLEAN, "camps", "enable_camp_invitations"),
    MAX_INVITED_PLAYERS_PER_CAMP = new ConfigValueImpl(EnumValueType.INT, "camps", "max_invited_players_per_camp"),
    INVITATION_REQUEST_EXPIRE_TIME_SECONDS = new ConfigValueImpl(EnumValueType.INT, "camps", "invitation_request_expire_time_seconds"),

    ENABLE_LOCATIONS = new ConfigValueImpl(EnumValueType.BOOLEAN, "locations", "enable_locations"),
    LOCATIONS_MAX_AMOUNT = new ConfigValueImpl(EnumValueType.INT, "locations", "max_amount"),
    LOCATION_TELEPORTATION_DELAY_SECONDS = new ConfigValueImpl(EnumValueType.INT, "locations", "teleport_delay_seconds"),
    LOCATION_TELEPORTATION_COOLDOWN_SECONDS = new ConfigValueImpl(EnumValueType.INT, "locations", "cooldown_seconds"),
    ALLOW_LOCATIONS_CREATION_FOR_ALL = new ConfigValueImpl(EnumValueType.BOOLEAN, "locations", "allow_locations_creation_for_all"),

    ENABLE_PLAYERS = new ConfigValueImpl(EnumValueType.BOOLEAN, "players", "enable_players"),
    PLAYER_TELEPORTATION_DELAY_SECONDS = new ConfigValueImpl(EnumValueType.INT, "players", "teleport_delay_seconds"),
    PLAYER_TELEPORTATION_COOLDOWN_SECONDS = new ConfigValueImpl(EnumValueType.INT, "players", "cooldown_seconds"),
    DEFAULT_JUMP_PROFILE = new ConfigValueImpl(EnumValueType.INT, "players", "default_jump_profile"),
    JUMP_REQUEST_EXPIRE_TIME = new ConfigValueImpl(EnumValueType.INT, "players", "jump_request_expire_seconds"),

    FEE_MODE = new ConfigValueImpl(EnumValueType.INT, "fees", "fee_mode"),
    CAMP_TELEPORTATION_FEE = new ConfigValueImpl(EnumValueType.LONG, "fees", "camp_teleportation_fee"),
    LOCATION_TELEPORTATION_FEE = new ConfigValueImpl(EnumValueType.LONG, "fees", "location_teleportation_fee"),
    JUMP_TO_PLAYER_FEE = new ConfigValueImpl(EnumValueType.LONG, "fees", "jump_to_player_fee");

    @Override
    public String getDomain() {
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
        values.add(CAMPS_SAVE_DELAY_MINUTES);
        values.add(LOCATIONS_SAVE_DELAY_MINUTES);

        values.add(IMAGE_WIDTH);
        values.add(IMAGE_HEIGHT);
        values.add(PROCESS_TELEPORTATION_ON_MOVE);
        values.add(ENABLE_CROSS_DIM_TELEPORTATION);

        values.add(ENABLE_CAMPS);
        values.add(CAMPS_MAX_AMOUNT);
        values.add(CAMP_TELEPORTATION_DELAY_SECONDS);
        values.add(CAMP_TELEPORTATION_COOLDOWN_SECONDS);
        values.add(ENABLE_FAVORITE_CAMP);
        values.add(ENABLE_CAMP_INVITATIONS);
        values.add(MAX_INVITED_PLAYERS_PER_CAMP);
        values.add(INVITATION_REQUEST_EXPIRE_TIME_SECONDS);

        values.add(ENABLE_LOCATIONS);
        values.add(LOCATIONS_MAX_AMOUNT);
        values.add(LOCATION_TELEPORTATION_DELAY_SECONDS);
        values.add(LOCATION_TELEPORTATION_COOLDOWN_SECONDS);
        values.add(ALLOW_LOCATIONS_CREATION_FOR_ALL);

        values.add(ENABLE_PLAYERS);
        values.add(PLAYER_TELEPORTATION_DELAY_SECONDS);
        values.add(PLAYER_TELEPORTATION_COOLDOWN_SECONDS);
        values.add(DEFAULT_JUMP_PROFILE);
        values.add(JUMP_REQUEST_EXPIRE_TIME);

        values.add(FEE_MODE);
        values.add(CAMP_TELEPORTATION_FEE);
        values.add(LOCATION_TELEPORTATION_FEE);
        values.add(JUMP_TO_PLAYER_FEE);
    }

    @Override
    public boolean sync() {
        return true;
    }
}
