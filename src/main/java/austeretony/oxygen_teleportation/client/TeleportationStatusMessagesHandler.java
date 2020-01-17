package austeretony.oxygen_teleportation.client;

import austeretony.oxygen_core.common.chat.ChatMessagesHandler;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class TeleportationStatusMessagesHandler implements ChatMessagesHandler {

    @Override
    public int getModIndex() {
        return TeleportationMain.TELEPORTATION_MOD_INDEX;
    }

    @Override
    public String getMessage(int messageIndex) {
        return EnumTeleportationStatusMessage.values()[messageIndex].localizedName();
    }
}
