package austeretony.oxygen_teleportation.client.gui.teleportation.players;

import austeretony.oxygen.client.gui.BackgroundGUIFiller;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;

public class PlayersBackgroundGUIFiller extends BackgroundGUIFiller {

    public PlayersBackgroundGUIFiller(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height, TeleportationMenuGUIScreen.PLAYERS_BACKGROUND);
    }

    @Override
    public void drawDefaultBackground() {
        drawRect(- 1, - 1, this.getWidth() + 1, this.getHeight() + 1, GUISettings.instance().getBaseGUIBackgroundColor());//main background
        drawRect(0, 0, this.getWidth(), 13, GUISettings.instance().getAdditionalGUIBackgroundColor());//title background
        drawRect(0, 14, 86, 80, GUISettings.instance().getAdditionalGUIBackgroundColor());//client profile background
        drawRect(0, 81, 86, 149, GUISettings.instance().getAdditionalGUIBackgroundColor());//point player background
        drawRect(87, 14, 172, 23, GUISettings.instance().getAdditionalGUIBackgroundColor());//players list search background
        drawRect(173, 14, 327, 23, GUISettings.instance().getAdditionalGUIBackgroundColor());//players panel amount background
        drawRect(87, 24, 327, 38, GUISettings.instance().getAdditionalGUIBackgroundColor());//players panel sorters background
        drawRect(87, 39, 324, 149, GUISettings.instance().getPanelGUIBackgroundColor());//players panel background
        drawRect(325, 39, 327, 149, GUISettings.instance().getAdditionalGUIBackgroundColor()); //slider background
    }
}
