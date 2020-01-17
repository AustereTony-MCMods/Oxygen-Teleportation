package austeretony.oxygen_teleportation.client.command;

import java.util.concurrent.TimeUnit;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuScreen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class TeleportationArgumentClient implements ArgumentExecutor {

    @Override
    public String getName() {
        return "teleportation";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1)
            OxygenHelperClient.scheduleTask(()->this.openMenu(), 100L, TimeUnit.MILLISECONDS);
        else if (args.length == 2) {
            if (args[1].equals("-fav-camp"))
                TeleportationManagerClient.instance().getPlayerDataManager().moveToFavoriteCampSynced();       
            else if (args[1].equals("-reset-data")) {
                TeleportationManagerClient.instance().getPlayerData().reset();
                TeleportationManagerClient.instance().getSharedCampsContainer().reset();
                TeleportationManagerClient.instance().getLocationsContainer().reset();              
                ClientReference.showChatMessage("oxygen_teleportation.command.dataReset");
            }
        }
    }

    private void openMenu() {
        ClientReference.delegateToClientThread(()->ClientReference.displayGuiScreen(new TeleportationMenuScreen()));
    }
}
