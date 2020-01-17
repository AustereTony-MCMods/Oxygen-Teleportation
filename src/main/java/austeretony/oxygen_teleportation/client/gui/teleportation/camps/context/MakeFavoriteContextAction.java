package austeretony.oxygen_teleportation.client.gui.teleportation.camps.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsSection;

public class MakeFavoriteContextAction implements OxygenContextMenuAction {

    private CampsSection section;

    public MakeFavoriteContextAction(CampsSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
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
