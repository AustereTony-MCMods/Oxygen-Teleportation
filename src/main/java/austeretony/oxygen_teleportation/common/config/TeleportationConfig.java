package austeretony.oxygen_teleportation.common.config;

import java.util.List;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class TeleportationConfig extends AbstractConfig {

    public static final ConfigValue
    ENABLE_TELEPORTATION_MENU_KEY = ConfigValueUtils.getValue("client", "enable_teleportation_menu_key", true),
    ENABLE_FAVORITE_CAMP_KEY = ConfigValueUtils.getValue("client", "enable_favorite_camp_key", true),

    IMAGE_WIDTH = ConfigValueUtils.getValue("server", "image_width", 800, true),
    IMAGE_HEIGHT = ConfigValueUtils.getValue("server", "image_height", 450, true),
    PROCESS_TELEPORTATION_ON_MOVE = ConfigValueUtils.getValue("server", "process_teleportation_on_move", true),
    ENABLE_CROSS_DIM_TELEPORTATION = ConfigValueUtils.getValue("server", "enable_cross_dim_teleportation", true),

    ENABLE_CAMPS = ConfigValueUtils.getValue("server", "enable_camps", true, true),
    CAMPS_MAX_AMOUNT = ConfigValueUtils.getValue("server", "camps_max_amount", 10, true),
    CAMP_TELEPORTATION_DELAY_SECONDS = ConfigValueUtils.getValue("server", "camp_teleportation_delay_seconds", 0),
    CAMP_TELEPORTATION_COOLDOWN_SECONDS = ConfigValueUtils.getValue("server", "camp_cooldown_seconds", 0, true),
    ENABLE_FAVORITE_CAMP = ConfigValueUtils.getValue("server", "enable_favorite_camp", true, true),
    ENABLE_CAMP_INVITATIONS = ConfigValueUtils.getValue("server", "enable_camp_invitations", true, true),
    MAX_INVITED_PLAYERS_PER_CAMP = ConfigValueUtils.getValue("server", "max_invited_players_per_camp", 5, true),
    INVITATION_REQUEST_EXPIRE_TIME_SECONDS = ConfigValueUtils.getValue("server", "camp_invitation_request_expire_time_seconds", 20),

    ENABLE_LOCATIONS = ConfigValueUtils.getValue("server", "enable_locations", true, true),
    LOCATIONS_MAX_AMOUNT = ConfigValueUtils.getValue("server", "locations_max_amount", 50, true),
    LOCATION_TELEPORTATION_DELAY_SECONDS = ConfigValueUtils.getValue("server", "location_teleportation_delay_seconds", 0),
    LOCATION_TELEPORTATION_COOLDOWN_SECONDS = ConfigValueUtils.getValue("server", "location_cooldown_seconds", 0, true),
    ALLOW_LOCATIONS_CREATION_FOR_ALL = ConfigValueUtils.getValue("server", "allow_locations_creation_for_all", false, true),

    ENABLE_PLAYER_TELEPORTATION = ConfigValueUtils.getValue("server", "enable_player_teleportation", true, true),
    PLAYER_TELEPORTATION_DELAY_SECONDS = ConfigValueUtils.getValue("server", "player_teleportation_delay_seconds", 0),
    PLAYER_TELEPORTATION_COOLDOWN_SECONDS = ConfigValueUtils.getValue("server", "player_teleportation_cooldown_seconds", 0, true),
    DEFAULT_PLAYER_TELEPORTATION_PROFILE = ConfigValueUtils.getValue("server", "default_player_teleportation_profile", 1),
    PLAYER_TELEPORTATION_REQUEST_EXPIRE_TIME = ConfigValueUtils.getValue("server", "player_teleportation_request_expire_seconds", 20),

    FEE_MODE = ConfigValueUtils.getValue("server", "fee_mode", 0, true),
    CAMP_TELEPORTATION_FEE = ConfigValueUtils.getValue("server", "camp_teleportation_fee", 0L, true),
    LOCATION_TELEPORTATION_FEE = ConfigValueUtils.getValue("server", "location_teleportation_fee", 0L, true),
    TELEPORTATION_TO_PLAYER_FEE = ConfigValueUtils.getValue("server", "teleportation_to_player_fee", 0L, true);

    @Override
    public String getDomain() {
        return TeleportationMain.MODID;
    }

    @Override
    public String getExternalPath() {
        return CommonReference.getGameFolder() + "/config/oxygen/teleportation.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_TELEPORTATION_MENU_KEY);
        values.add(ENABLE_FAVORITE_CAMP_KEY);

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

        values.add(ENABLE_PLAYER_TELEPORTATION);
        values.add(PLAYER_TELEPORTATION_DELAY_SECONDS);
        values.add(PLAYER_TELEPORTATION_COOLDOWN_SECONDS);
        values.add(DEFAULT_PLAYER_TELEPORTATION_PROFILE);
        values.add(PLAYER_TELEPORTATION_REQUEST_EXPIRE_TIME);

        values.add(FEE_MODE);
        values.add(CAMP_TELEPORTATION_FEE);
        values.add(LOCATION_TELEPORTATION_FEE);
        values.add(TELEPORTATION_TO_PLAYER_FEE);
    }
}
