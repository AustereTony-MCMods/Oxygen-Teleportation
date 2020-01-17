package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsSection;

public class EditContextAction implements OxygenContextMenuAction {

    private CampsSection section;

    public EditContextAction(CampsSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_teleportation.gui.menu.edit");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openPointEditingCallback();
    }
}
