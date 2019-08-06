package austeretony.oxygen_teleportation.client.gui.teleportation.locations.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsGUISection;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;

public class LockContextAction extends AbstractContextAction {

    private LocationsGUISection section;

    public LockContextAction(LocationsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName(GUIBaseElement currElement) {
        return this.section.getCurrentPoint().isLocked() ? ClientReference.localize("teleportation.gui.menu.unlock") : ClientReference.localize("teleportation.gui.menu.lock");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false) 
                || this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        if (!this.section.getCurrentPoint().isLocked()) {
            TeleportationManagerClient.instance().getLocationsManager().lockLocationSynced(this.section.getCurrentPoint(), true);
            this.section.getCurrentButton().setTextDynamicColor(GUISettings.instance().getEnabledTextColorDark(), GUISettings.instance().getDisabledTextColorDark(), GUISettings.instance().getHoveredTextColorDark());
        } else {
            TeleportationManagerClient.instance().getLocationsManager().lockLocationSynced(this.section.getCurrentPoint(), false);
            this.section.getCurrentButton().setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
        }
    }
}
