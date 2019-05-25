package austeretony.oxygen_teleportation.client.gui.menu.camps;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.oxygen.client.gui.BackgroundGUIFiller;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen_teleportation.client.gui.menu.TeleportationMenuGUIScreen;
import net.minecraft.client.renderer.GlStateManager;

public class CampsBackgroundGUIFiller extends BackgroundGUIFiller {

    public CampsBackgroundGUIFiller(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);
    }

    @Override
    public void draw(int mouseX, int mouseY) {  
        if (this.isVisible()) {         
            GlStateManager.pushMatrix();            
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);                      
            if (GUISettings.instance().shouldUseTextures()) {  
                GlStateManager.enableBlend();    
                this.mc.getTextureManager().bindTexture(TeleportationMenuGUIScreen.POINTS_BACKGROUND);                         
                GUIAdvancedElement.drawCustomSizedTexturedRect( - GUISettings.instance().getTextureOffsetX(), - GUISettings.instance().getTextureOffsetY(), 0, 0, this.textureWidth, this.textureHeight, this.textureWidth, this.textureHeight);             
                GlStateManager.disableBlend();   
            } else {
                drawRect(- 1, - 1, this.getWidth() + 1, this.getHeight() + 1, GUISettings.instance().getBaseGUIBackgroundColor());//main background
                drawRect(0, 0, this.getWidth(), 13, GUISettings.instance().getAdditionalGUIBackgroundColor());//title background
                drawRect(0, 14, 85, 23, GUISettings.instance().getAdditionalGUIBackgroundColor());//search panel background
                drawRect(0, 24, 82, 133, GUISettings.instance().getPanelGUIBackgroundColor());//panel background
                drawRect(83, 24, 85, 133, GUISettings.instance().getAdditionalGUIBackgroundColor());//slider background
                drawRect(0, 134, 85, 149, GUISettings.instance().getAdditionalGUIBackgroundColor());//create button background
            }
            GlStateManager.popMatrix();            
        }
    }
}
