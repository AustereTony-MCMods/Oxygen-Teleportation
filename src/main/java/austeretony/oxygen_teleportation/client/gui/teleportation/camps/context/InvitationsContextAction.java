package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsSection;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;

public class InvitationsContextAction implements OxygenContextMenuAction {

    private CampsSection section;

    public InvitationsContextAction(CampsSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_teleportation.gui.menu.invitations");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return TeleportationConfig.ENABLE_CAMP_INVITATIONS.asBoolean() 
                && this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID())
                && TeleportationManagerClient.instance().getSharedCampsContainer().invitedPlayersExist(this.section.getCurrentPoint().getId());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openInvitationsCallback();
    }
}
