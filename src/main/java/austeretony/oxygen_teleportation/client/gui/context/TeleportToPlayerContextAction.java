package austeretony.oxygen_teleportation.client.gui.context;

import java.util.UUID;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_core.client.gui.elements.OxygenWrapperPanelEntry;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData.EnumJumpProfile;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;

public class TeleportToPlayerContextAction implements OxygenContextMenuAction {

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_teleportation.gui.menu.moveTo");
    }   

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        UUID targetUUID = ((OxygenWrapperPanelEntry<UUID>) currElement).getWrapped();
        return !targetUUID.equals(OxygenHelperClient.getPlayerUUID()) 
                && OxygenHelperClient.isPlayerOnline(targetUUID)
                && (TeleportationManagerClient.instance().getPlayerDataManager().getPlayerJumpProfile(targetUUID) != EnumJumpProfile.DISABLED || PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_ANY_PLAYER.id(), false));
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        UUID targetUUID = ((OxygenWrapperPanelEntry<UUID>) currElement).getWrapped();
        TeleportationManagerClient.instance().getPlayerDataManager().moveToPlayerSynced(OxygenHelperClient.getPlayerIndex(targetUUID));
        if (ClientReference.getMinecraft().currentScreen != null)
            ClientReference.getMinecraft().displayGuiScreen(null);
    }
}
