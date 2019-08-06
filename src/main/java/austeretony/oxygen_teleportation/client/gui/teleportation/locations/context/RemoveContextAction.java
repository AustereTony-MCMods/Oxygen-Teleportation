package austeretony.oxygen_teleportation.client.gui.teleportation.locations.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsGUISection;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;

public class RemoveContextAction extends AbstractContextAction {

    private LocationsGUISection section;

    public RemoveContextAction(LocationsGUISection section) {
        this.section = section;
    }   

    @Override
    protected String getName(GUIBaseElement currElement) {
        return ClientReference.localize("teleportation.gui.menu.remove");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false) 
                || this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openRemovePointCallback();
    }
}
