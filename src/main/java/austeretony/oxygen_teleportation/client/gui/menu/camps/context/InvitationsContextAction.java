package austeretony.oxygen_teleportation.client.gui.menu.camps.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.menu.CampsGUISection;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
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
        return TeleportationConfig.ENABLE_CAMP_INVITATIONS.getBooleanValue() 
                && this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID())
                && TeleportationManagerClient.instance().getSharedCampsManager().invitedPlayersExist(this.section.getCurrentPoint().getId());
    }

    @Override
    protected void execute(GUIBaseElement currElement) {
        this.section.openInvitationsCallback();
    }
}
