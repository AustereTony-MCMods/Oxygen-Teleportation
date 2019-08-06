package austeretony.oxygen_teleportation.client.gui.teleportation.locations;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsGUISection;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import net.minecraft.client.resources.I18n;

public class EditContextAction extends AbstractContextAction {

    private LocationsGUISection section;

    public EditContextAction(LocationsGUISection section) {
        this.section = section;
    }

    @Override   
    protected String getName(GUIBaseElement currElement) {
        return I18n.format("teleportation.gui.menu.edit");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false) 
                || this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openPointEditingCallback();
    }
}
