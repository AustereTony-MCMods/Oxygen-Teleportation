package austeretony.teleportation.client.gui.menu.locations;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.client.gui.menu.LocationsGUISection;
import austeretony.teleportation.common.main.EnumPrivileges;
import net.minecraft.client.resources.I18n;

public class RemoveContextAction extends AbstractContextAction {

    private LocationsGUISection section;

    public RemoveContextAction(LocationsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName() {
        return I18n.format("teleportation.gui.menu.remove");
    }

    @Override
    public boolean isValid() {
        return PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.LOCATIONS_MANAGEMENT.toString(), false) 
                || this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute() {
        this.section.openRemovePointCallback();
    }
}
