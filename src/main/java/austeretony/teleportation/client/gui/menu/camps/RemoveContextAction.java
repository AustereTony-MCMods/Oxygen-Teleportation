package austeretony.teleportation.client.gui.menu.camps;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import net.minecraft.client.resources.I18n;

public class RemoveContextAction extends AbstractContextAction {

    private CampsGUISection section;

    public RemoveContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName() {
        return this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID()) ? I18n.format("teleportation.gui.menu.remove") : I18n.format("teleportation.gui.menu.leave");
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void execute() {
        if (this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID()))
            this.section.openRemovePointCallback();
        else
            this.section.openLeavePointCallback();
    }
}
