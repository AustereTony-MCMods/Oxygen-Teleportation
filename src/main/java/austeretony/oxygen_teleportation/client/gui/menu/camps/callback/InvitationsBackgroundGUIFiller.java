package austeretony.oxygen_teleportation.client.gui.menu.camps.callback;

import austeretony.oxygen.client.gui.BackgroundGUIFiller;
import austeretony.oxygen.client.gui.settings.GUISettings;
import net.minecraft.client.renderer.GlStateManager;

public class InvitationsBackgroundGUIFiller extends BackgroundGUIFiller {

    public InvitationsBackgroundGUIFiller(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);
    }

    @Override
    public void draw(int mouseX, int mouseY) {  
        if (this.isVisible()) {         
            GlStateManager.pushMatrix();            
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);                      
            drawRect(- 1, - 1, this.getWidth() + 1, this.getHeight() + 1, GUISettings.instance().getBaseGUIBackgroundColor());
            drawRect(0, 0, this.getWidth(), 11, GUISettings.instance().getAdditionalGUIBackgroundColor());
            drawRect(0, 12, this.getWidth() - 3, 66, GUISettings.instance().getPanelGUIBackgroundColor());
            drawRect(this.getWidth() - 2, 12, this.getWidth(), 66, GUISettings.instance().getAdditionalGUIBackgroundColor());
            drawRect(0, this.getHeight() - 14, this.getWidth(), this.getHeight(), GUISettings.instance().getAdditionalGUIBackgroundColor());
            GlStateManager.popMatrix();            
        }
    }
}
