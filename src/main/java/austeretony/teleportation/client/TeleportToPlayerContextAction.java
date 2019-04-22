package austeretony.teleportation.client;

import java.util.UUID;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.gui.PlayerGUIButton;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.common.main.EnumTeleportationPrivileges;
import austeretony.teleportation.common.main.TeleportationPlayerData;
import net.minecraft.client.resources.I18n;

public class TeleportToPlayerContextAction extends AbstractContextAction {

    @Override
    protected String getName(GUIBaseElement currElement) {
        return I18n.format("contextaction.moveToPlayer");
    }

    @Override
    protected boolean isValid(GUIBaseElement currElement) {
        UUID targetUUID = ((PlayerGUIButton) currElement).playerUUID;
        return !targetUUID.equals(OxygenHelperClient.getPlayerUUID()) 
                && (PlayersManagerClient.getPlayerJumpProfile(targetUUID) != TeleportationPlayerData.EnumJumpProfile.DISABLED || PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false));
    }

    @Override
    protected void execute(GUIBaseElement currElement) {
        UUID targetUUID = ((PlayerGUIButton) currElement).playerUUID;
        TeleportationManagerClient.instance().getPlayersManager().moveToPlayerSynced(targetUUID);
    }
}
