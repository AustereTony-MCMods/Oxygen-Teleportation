package austeretony.teleportation.client.gui.menu.locations;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.client.gui.menu.LocationsGUISection;
import austeretony.teleportation.common.main.EnumTeleportationPrivileges;
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
        return PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivileges.LOCATIONS_MANAGEMENT.toString(), false) 
                || this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openPointEditingCallback();
    }
}
