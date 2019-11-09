package austeretony.oxygen_teleportation.server.command;

import java.util.Set;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.command.AbstractArgumentExecutor;
import austeretony.oxygen_core.common.api.command.ArgumentParameterImpl;
import austeretony.oxygen_core.common.command.ArgumentParameter;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPSyncFeeItemStack;
import austeretony.oxygen_teleportation.server.TeleportationLoaderServer;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;
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
    public void getParams(Set<ArgumentParameter> params) {
        params.add(new ArgumentParameterImpl(ACTION_SET_FEE_STACK));
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Set<ArgumentParameter> params) throws CommandException {
        if (sender instanceof MinecraftServer)
            throw new CommandException("Command is not available for server console!");
        for (ArgumentParameter param : params) {
            if (param.getBaseName().equals(ACTION_SET_FEE_STACK)) {
                EntityPlayerMP playerMP = (EntityPlayerMP) sender;
                if (!playerMP.getHeldItemMainhand().isEmpty()) {
                    TeleportationManagerServer.instance().setFeeStack(ItemStackWrapper.getFromStack(playerMP.getHeldItemMainhand()));
                    TeleportationLoaderServer.saveFeeItemStackDelegated();
                    OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.FEE_STACK_SPECIFIED.ordinal());
                    for (EntityPlayerMP player : CommonReference.getServer().getPlayerList().getPlayers())
                        OxygenMain.network().sendTo(new CPSyncFeeItemStack(), player);
                } else
                    throw new CommandException("oxygen_teleportation.command.exception.mainHandEmpty");
            }
        }
    }
}