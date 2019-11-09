package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenuElement.ContextMenuAction;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsGUISection;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;

public class InviteContextAction implements ContextMenuAction {

    private CampsGUISection section;

    public InviteContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    public String getName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_teleportation.gui.menu.invite");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {      
        return TeleportationConfig.ENABLE_CAMP_INVITATIONS.getBooleanValue() 
                && this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID())
                && TeleportationManagerClient.instance().getSharedCampsContainer().getInvitedPlayersAmountForCamp(this.section.getCurrentPoint().getId()) < TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.getIntValue();
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openInviteCallback();
    }
}
