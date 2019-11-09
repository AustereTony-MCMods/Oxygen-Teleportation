package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenuElement.ContextMenuAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsGUISection;

public class RemoveContextAction implements ContextMenuAction {

    private CampsGUISection section;

    public RemoveContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    public String getName(GUIBaseElement currElement) {
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
