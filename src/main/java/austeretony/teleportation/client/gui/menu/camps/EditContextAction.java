package austeretony.teleportation.client.gui.menu.camps;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import net.minecraft.client.resources.I18n;

public class EditContextAction extends AbstractContextAction {

    private CampsGUISection section;

    public EditContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName() {
        return I18n.format("teleportation.gui.menu.edit");
    }

    @Override
    public boolean isValid() {
        return this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute() {
        this.section.openPointEditingCallback();
    }
}
