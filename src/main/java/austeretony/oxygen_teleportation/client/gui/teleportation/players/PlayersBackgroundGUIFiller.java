package austeretony.oxygen_teleportation.client.gui.teleportation.players;

import austeretony.oxygen_core.client.gui.elements.BackgroundGUIFiller;
import austeretony.oxygen_core.client.gui.elements.CustomRectUtils;

public class PlayersBackgroundGUIFiller extends BackgroundGUIFiller {

    public PlayersBackgroundGUIFiller(int xPosition, int yPosition, int width, int height) {             
        super(xPosition, yPosition, width, height);
    }

    @Override
    public void drawBackground() {
        //main background  
        drawRect(0, 0, this.getWidth(), this.getHeight(), this.getEnabledBackgroundColor());      

        //title underline
        CustomRectUtils.drawRect(4.0D, 14.0D, this.getWidth() - 4.0D, 14.4D, this.getDisabledBackgroundColor());

        //panel underline
        CustomRectUtils.drawRect(4.0D, this.getHeight() - 15.0D, this.getWidth() - 4.0D, this.getHeight() - 15.4D, this.getDisabledBackgroundColor());
    }
}
