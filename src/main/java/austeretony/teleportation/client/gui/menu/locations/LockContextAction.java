package austeretony.teleportation.client.gui.menu.locations;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.LocationsGUISection;
import austeretony.teleportation.common.main.EnumPrivileges;
import net.minecraft.client.resources.I18n;

public class LockContextAction extends AbstractContextAction {

    private LocationsGUISection section;

    public LockContextAction(LocationsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName() {
        return this.section.getCurrentPoint().isLocked() ? I18n.format("teleportation.gui.menu.unlock") : I18n.format("teleportation.gui.menu.lock");
    }

    @Override
    public boolean isValid() {
        return PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.LOCATIONS_MANAGEMENT.toString(), false) 
                || this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute() {
        if (!this.section.getCurrentPoint().isLocked()) {
            TeleportationManagerClient.instance().getLocationsManager().lockLocationSynced(this.section.getCurrentPoint(), true);
            this.section.getCurrentButton().setTextDynamicColor(GUISettings.instance().getEnabledTextColorDark(), GUISettings.instance().getDisabledTextColorDark(), GUISettings.instance().getHoveredTextColorDark());
        } else {
            TeleportationManagerClient.instance().getLocationsManager().lockLocationSynced(this.section.getCurrentPoint(), false);
            this.section.getCurrentButton().setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
        }
    }
}
