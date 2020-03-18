package austeretony.oxygen_teleportation.server.command;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPSyncFeeItemStack;
import austeretony.oxygen_teleportation.server.TeleportationLoaderServer;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class TeleportationArgumentOperator implements ArgumentExecutor {

    @Override
    public String getName() {
        return "teleportation";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP))
            throw new CommandException("Command available only for player!");
        if (args.length == 2) {
            if (args[1].equals("-set-fee-stack")) {
                EntityPlayerMP senderPlayerMP = (EntityPlayerMP) sender;
                if (!senderPlayerMP.getHeldItemMainhand().isEmpty()) {
                    TeleportationManagerServer.instance().setFeeStack(ItemStackWrapper.of(senderPlayerMP.getHeldItemMainhand()));
                    TeleportationLoaderServer.saveFeeItemStackAsync();

                    OxygenHelperServer.sendStatusMessage(senderPlayerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.FEE_STACK_SPECIFIED.ordinal());                 
                    for (EntityPlayerMP player : CommonReference.getServer().getPlayerList().getPlayers())
                        OxygenMain.network().sendTo(new CPSyncFeeItemStack(), player);
                } else
                    throw new CommandException("oxygen_teleportation.command.exception.mainHandEmpty");
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 2)
            return CommandBase.getListOfStringsMatchingLastWord(args, "-set-fee-stack");
        return Collections.<String>emptyList();
    }
}
