package austeretony.teleportation.client.gui.menu.camps.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import net.minecraft.client.resources.I18n;

public class InvitationsContextAction extends AbstractContextAction {

    private CampsGUISection section;

    public InvitationsContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName(GUIBaseElement currElement) {
        return I18n.format("teleportation.gui.menu.invitations");
    }

    @Override
    protected boolean isValid(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID())
                && TeleportationManagerClient.instance().getSharedCampsManager().invitedPlayersExist(this.section.getCurrentPoint().getId());
    }

    @Override
    protected void execute(GUIBaseElement currElement) {
        this.section.openInvitationsCallback();
    }
}
