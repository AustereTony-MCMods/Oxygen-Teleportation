package austeretony.teleportation.common.main;

import austeretony.oxygen.common.api.chat.IChatMessageInfoListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TeleportationChatMessagesListener implements IChatMessageInfoListener {

    @Override
    public String getModId() {
        return TeleportationMain.MODID;
    }

    @Override
    public void show(int mod, int message, String... args) {
        if (mod == TeleportationMain.TELEPORTATION_MOD_INDEX)
            EnumChatMessages.values()[message].show(args);
    }
}
