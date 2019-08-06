package austeretony.oxygen_teleportation.client.gui.teleportation.camps;

import austeretony.oxygen.client.gui.BackgroundGUIFiller;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;

public class CampsBackgroundGUIFiller extends BackgroundGUIFiller {

    public CampsBackgroundGUIFiller(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height, TeleportationMenuGUIScreen.POINTS_BACKGROUND);
    }

    @Override
    public void drawDefaultBackground() {
        drawRect(- 1, - 1, this.getWidth() + 1, this.getHeight() + 1, GUISettings.instance().getBaseGUIBackgroundColor());//main background
        drawRect(0, 0, this.getWidth(), 13, GUISettings.instance().getAdditionalGUIBackgroundColor());//title background
        drawRect(0, 14, 85, 23, GUISettings.instance().getAdditionalGUIBackgroundColor());//search panel background
        drawRect(0, 24, 82, 133, GUISettings.instance().getPanelGUIBackgroundColor());//panel background
        drawRect(83, 24, 85, 133, GUISettings.instance().getAdditionalGUIBackgroundColor());//slider background
        drawRect(0, 134, 85, 149, GUISettings.instance().getAdditionalGUIBackgroundColor());//create button background
    }
}
