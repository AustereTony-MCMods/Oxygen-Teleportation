package austeretony.oxygen_teleportation.client.gui.teleportation.camps;

import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenBackgroundFiller;

public class PointsBackgroundFiller extends OxygenBackgroundFiller {

    public PointsBackgroundFiller(int xPosition, int yPosition, int width, int height) {             
        super(xPosition, yPosition, width, height);
    }

    @Override
    public void drawBackground() {
        //main background  
        drawRect(0, 0, this.getWidth(), this.getHeight(), this.getEnabledBackgroundColor());      

        //title underline
        OxygenGUIUtils.drawRect(4.0D, 14.0D, this.getWidth() - 4.0D, 14.4D, this.getDisabledBackgroundColor());

        //panel underline
        OxygenGUIUtils.drawRect(4.0D, this.getHeight() - 12.6D, 89.0F, this.getHeight() - 13.0D, this.getDisabledBackgroundColor());
    }
}
