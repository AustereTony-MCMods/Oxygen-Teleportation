package austeretony.oxygen_teleportation.common.command;

import java.util.Set;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.command.AbstractArgumentExecutor;
import austeretony.oxygen.common.api.command.ArgumentParameter;
import austeretony.oxygen.common.command.IArgumentParameter;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.itemstack.ItemStackWrapper;
import austeretony.oxygen_teleportation.common.TeleportationLoaderServer;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationChatMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPSyncFeeItemStack;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class TeleportationArgumentExecutorServer extends AbstractArgumentExecutor {

    public static final String ACTION_SET_FEE_STACK = "set-fee-stack";

    public TeleportationArgumentExecutorServer(String argument, boolean hasParams) {
        super(argument, hasParams);
    }

    @Override
    public void getParams(Set<IArgumentParameter> params) {
        params.add(new ArgumentParameter(ACTION_SET_FEE_STACK));
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Set<IArgumentParameter> params) throws CommandException {
        if (sender instanceof MinecraftServer)
            throw new CommandException("Command is not available for server console!");
        for (IArgumentParameter param : params) {
            if (param.getBaseName().equals(ACTION_SET_FEE_STACK)) {
                EntityPlayerMP playerMP = (EntityPlayerMP) sender;
                if (!playerMP.getHeldItemMainhand().isEmpty()) {
                    TeleportationManagerServer.instance().setFeeStack(ItemStackWrapper.getFromStack(playerMP.getHeldItemMainhand()));
                    TeleportationLoaderServer.saveFeeItemStackDelegated();
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.FEE_STACK_SPECIFIED.ordinal());
                    for (EntityPlayerMP player : CommonReference.getServer().getPlayerList().getPlayers())
                        TeleportationMain.network().sendTo(new CPSyncFeeItemStack(), player);
                } else
                    throw new CommandException("oxygen_teleportation.command.exception.mainHandEmpty");
            }
        }
    }
}