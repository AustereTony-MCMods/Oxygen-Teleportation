package austeretony.oxygen_teleportation.client.settings;

import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.settings.SettingValue;
import austeretony.oxygen_core.common.settings.SettingValueUtils;

public enum EnumTeleportationClientSetting {

    //Oxygen Menu

    ADD_TELEPORTATION_MENU("menu_add_teleportation_menu", EnumValueType.BOOLEAN, String.valueOf(true));

    private final String key, baseValue;

    private final EnumValueType type;

    private SettingValue value;

    EnumTeleportationClientSetting(String key, EnumValueType type, String baseValue) {
        this.key = key;
        this.type = type;
        this.baseValue = baseValue;
    }

    public SettingValue get() {
        if (this.value == null)
            this.value = OxygenManagerClient.instance().getClientSettingManager().getSettingValue(this.key);
        return this.value;
    }

    public static void register() {
        for (EnumTeleportationClientSetting setting : EnumTeleportationClientSetting.values())
            OxygenManagerClient.instance().getClientSettingManager().register(SettingValueUtils.getValue(setting.type, setting.key, setting.baseValue));
    }
}
