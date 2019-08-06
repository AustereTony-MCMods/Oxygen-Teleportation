package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsGUISection;

public class RemoveContextAction extends AbstractContextAction {

    private CampsGUISection section;

    public RemoveContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID()) ? ClientReference.localize("teleportation.gui.menu.remove") : ClientReference.localize("teleportation.gui.menu.leave");
    }

    @Override   
    public boolean isValid(GUIBaseElement currElement) {
        return true;
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        if (this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID()))
            this.section.openRemovePointCallback();
        else
            this.section.openLeavePointCallback();
    }
}
