package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsSection;

public class LockContextAction implements OxygenContextMenuAction {

    private CampsSection section;

    public LockContextAction(CampsSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isLocked() ? ClientReference.localize("oxygen_teleportation.gui.menu.unlock") : ClientReference.localize("oxygen_teleportation.gui.menu.lock");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        TeleportationManagerClient.instance().getPlayerDataManager().lockCampSynced(this.section.getCurrentPoint().getId(), !this.section.getCurrentPoint().isLocked());
    }
}
