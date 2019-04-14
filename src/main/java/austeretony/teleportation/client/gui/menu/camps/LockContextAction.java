package austeretony.teleportation.client.gui.menu.camps;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import net.minecraft.client.resources.I18n;

public class LockContextAction extends AbstractContextAction {

    private CampsGUISection section;

    public LockContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName() {
        return this.section.getCurrentPoint().isLocked() ? I18n.format("teleportation.gui.menu.unlock") : I18n.format("teleportation.gui.menu.lock");
    }

    @Override
    public boolean isValid() {
        return this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute() {
        if (!this.section.getCurrentPoint().isLocked()) {
            TeleportationManagerClient.instance().getCampsManager().lockCampSynced(this.section.getCurrentPoint(), true);
            this.section.getCurrentButton().setTextDynamicColor(GUISettings.instance().getEnabledTextColorDark(), GUISettings.instance().getDisabledTextColorDark(), GUISettings.instance().getHoveredTextColorDark());
        } else {
            TeleportationManagerClient.instance().getCampsManager().lockCampSynced(this.section.getCurrentPoint(), false);
            this.section.getCurrentButton().setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
        }
    }
}
