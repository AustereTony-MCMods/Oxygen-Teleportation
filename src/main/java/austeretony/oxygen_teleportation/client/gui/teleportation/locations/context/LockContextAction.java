package austeretony.oxygen_teleportation.client.gui.teleportation.locations.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegeProviderClient;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenuElement.ContextMenuAction;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsGUISection;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;

public class LockContextAction implements ContextMenuAction {

    private LocationsGUISection section;

    public LockContextAction(LocationsGUISection section) {
        this.section = section;
    }

    @Override
    public String getName(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isLocked() ? ClientReference.localize("oxygen_teleportation.gui.menu.unlock") : ClientReference.localize("oxygen_teleportation.gui.menu.lock");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return PrivilegeProviderClient.getValue(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false) 
                || this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        TeleportationManagerClient.instance().getLocationsManager().lockLocationSynced(this.section.getCurrentPoint().getId(), !this.section.getCurrentPoint().isLocked());
    }
}
