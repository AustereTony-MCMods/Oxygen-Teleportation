package austeretony.teleportation.client.gui.menu.camps;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import net.minecraft.client.resources.I18n;

public class InviteContextAction extends AbstractContextAction {

    private CampsGUISection section;

    public InviteContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName() {
        return I18n.format("teleportation.gui.menu.invite");
    }

    @Override
    public boolean isValid() {
        return false;//this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());//TODO WIP
    }

    @Override
    public void execute() {
        this.section.openInvitationCallback();
    }
}
