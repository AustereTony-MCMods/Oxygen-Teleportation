package austeretony.oxygen_teleportation.client;

import java.util.UUID;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.PlayerGUIButton;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivileges;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;

public class TeleportToPlayerContextAction extends AbstractContextAction {

    @Override
    protected String getName(GUIBaseElement currElement) {
        return ClientReference.localize("contextaction.moveToPlayer");
    }   

    @Override
    protected boolean isValid(GUIBaseElement currElement) {
        UUID targetUUID = ((PlayerGUIButton) currElement).playerUUID;
        return !targetUUID.equals(OxygenHelperClient.getPlayerUUID()) 
                && OxygenHelperClient.isOnline(targetUUID)
                && (PlayersManagerClient.getPlayerJumpProfile(targetUUID) != TeleportationPlayerData.EnumJumpProfile.DISABLED || PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.ENABLE_TELEPORTATION_TO_ANY_PLAYER.toString(), false));
    }

    @Override
    protected void execute(GUIBaseElement currElement) {
        UUID targetUUID = ((PlayerGUIButton) currElement).playerUUID;
        TeleportationManagerClient.instance().getPlayersManager().moveToPlayerSynced(OxygenHelperClient.getPlayerIndex(targetUUID));
        if (ClientReference.getMinecraft().currentScreen != null)
            ClientReference.getMinecraft().displayGuiScreen(null);
    }
}
