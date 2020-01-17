package austeretony.oxygen_teleportation.client.gui.teleportation.locations.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsSection;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;

public class LockContextAction implements OxygenContextMenuAction {

    private LocationsSection section;

    public LockContextAction(LocationsSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isLocked() ? ClientReference.localize("oxygen_teleportation.gui.menu.unlock") : ClientReference.localize("oxygen_teleportation.gui.menu.lock");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.id(), false) 
                || this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        TeleportationManagerClient.instance().getLocationsManager().lockLocationSynced(this.section.getCurrentPoint().getId(), !this.section.getCurrentPoint().isLocked());
    }
}
