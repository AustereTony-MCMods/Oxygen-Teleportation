package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenuElement.ContextMenuAction;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsGUISection;

public class MakeFavoriteContextAction implements ContextMenuAction {

    private CampsGUISection section;

    public MakeFavoriteContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    public String getName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_teleportation.gui.menu.makeFvorite");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId() != this.section.getCurrentPoint().getId();
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        TeleportationManagerClient.instance().getPlayerDataManager().setFavoriteCampSynced(this.section.getCurrentPoint().getId());
    }
}
