package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsSection;

public class RemoveContextAction implements OxygenContextMenuAction {

    private CampsSection section;

    public RemoveContextAction(CampsSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID()) ? ClientReference.localize("oxygen_teleportation.gui.menu.remove") : ClientReference.localize("oxygen_teleportation.gui.menu.leave");
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
