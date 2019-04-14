package austeretony.teleportation.client.gui.menu.camps;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import net.minecraft.client.resources.I18n;

public class MakeFavoriteContextAction extends AbstractContextAction {

    private CampsGUISection section;

    public MakeFavoriteContextAction(CampsGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName() {
        return I18n.format("teleportation.gui.menu.makeFvorite");
    }

    @Override
    public boolean isValid() {
        return TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId() != this.section.getCurrentPoint().getId();
    }

    @Override
    public void execute() {
        TeleportationManagerClient.instance().getCampsManager().setFavoriteCampSynced(this.section.getCurrentPoint().getId());
        if (this.section.getPreviousFavoriteButton() != null)
            this.section.getPreviousFavoriteButton().resetFavorite();
        this.section.getCurrentButton().setFavorite();
        this.section.showFavoriteMark();
    }
}
