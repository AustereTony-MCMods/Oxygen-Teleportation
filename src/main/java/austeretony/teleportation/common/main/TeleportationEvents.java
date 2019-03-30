package austeretony.teleportation.common.main;

import austeretony.oxygen.common.event.OxygenClientInitEvent;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.menu.locations.LocationsManagerClient;
import austeretony.teleportation.common.menu.players.PlayersManagerClient;
import austeretony.teleportation.common.menu.players.PlayersManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TeleportationEvents {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        TeleportationMain.LOGGER.info("Initialized client data.");
        CampsManagerClient.create();
        LocationsManagerClient.create();
        PlayersManagerClient.create();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        CampsManagerServer.instance().onPlayerLoggedIn(event.player);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        CampsManagerServer.instance().onPlayerLoggedOut(event.player);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {     
        if (event.phase == TickEvent.Phase.START) {
            CampsManagerServer.instance().processTeleportations();
            PlayersManagerServer.instance().processJumpRequests();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {     
        if (event.phase == TickEvent.Phase.START && PlayersManagerClient.instance() != null)
            PlayersManagerClient.instance().getJumpRequest().process();
    }
}
