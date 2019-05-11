package austeretony.teleportation.client.gui.menu.camps.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import austeretony.teleportation.common.config.TeleportationConfig;
import net.minecraft.client.resources.I18n;

public class InviteContextAction extends AbstractContextAction {

    private CampsGUISection section;

    public InviteContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName(GUIBaseElement currElement) {
        return I18n.format("teleportation.gui.menu.invite");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {      
        return TeleportationConfig.ENABLE_CAMP_INVITATIONS.getBooleanValue() 
                && this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID())
                && TeleportationManagerClient.instance().getSharedCampsManager().getInvitedPlayersAmountForCamp(this.section.getCurrentPoint().getId()) < TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.getIntValue();
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openInviteCallback();
    }
}
