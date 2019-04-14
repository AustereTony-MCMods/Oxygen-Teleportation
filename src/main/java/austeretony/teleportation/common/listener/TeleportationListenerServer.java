package austeretony.teleportation.common.listener;

import austeretony.oxygen.common.core.api.listeners.server.IPlayerLogInListener;
import austeretony.oxygen.common.core.api.listeners.server.IPlayerLogOutListener;
import austeretony.teleportation.common.TeleportationManagerServer;
import austeretony.teleportation.common.main.TeleportationMain;
import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportationListenerServer implements IPlayerLogInListener, IPlayerLogOutListener {

    @Override
    public String getModId() {
        return TeleportationMain.MODID;
    }

    @Override
    public void onPlayerLogIn(EntityPlayerMP playerMP) {        
        TeleportationManagerServer.instance().onPlayerLoggedIn(playerMP);
    }

    @Override
    public void onPlayerLogOut(EntityPlayerMP playerMP) {
        TeleportationManagerServer.instance().onPlayerLoggedOut(playerMP);
    }
}
