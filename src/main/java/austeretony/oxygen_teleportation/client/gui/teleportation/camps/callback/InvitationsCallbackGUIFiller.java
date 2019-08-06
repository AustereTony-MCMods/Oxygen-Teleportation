package austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback;

import austeretony.oxygen.client.gui.BackgroundGUIFiller;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;

public class InvitationsCallbackGUIFiller extends BackgroundGUIFiller {

    public InvitationsCallbackGUIFiller(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height, TeleportationMenuGUIScreen.INVITATIONS_CALLBACK_BACKGROUND);
    }

    @Override
    public void drawDefaultBackground() {
        drawRect(- 1, - 1, this.getWidth() + 1, this.getHeight() + 1, GUISettings.instance().getBaseGUIBackgroundColor());
        drawRect(0, 0, this.getWidth(), 11, GUISettings.instance().getAdditionalGUIBackgroundColor());
        drawRect(0, 12, this.getWidth() - 3, 66, GUISettings.instance().getPanelGUIBackgroundColor());
        drawRect(this.getWidth() - 2, 12, this.getWidth(), 66, GUISettings.instance().getAdditionalGUIBackgroundColor());
        drawRect(0, this.getHeight() - 14, this.getWidth(), this.getHeight(), GUISettings.instance().getAdditionalGUIBackgroundColor()); 
    }
}
