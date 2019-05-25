package austeretony.oxygen_teleportation.client.gui.menu.players;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.oxygen.client.gui.BackgroundGUIFiller;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen_teleportation.client.gui.menu.TeleportationMenuGUIScreen;
import net.minecraft.client.renderer.GlStateManager;

public class PlayersBackgroundGUIFiller extends BackgroundGUIFiller {

    public PlayersBackgroundGUIFiller(int xPosition, int yPosition, int width, int height) {
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
                this.mc.getTextureManager().bindTexture(TeleportationMenuGUIScreen.PLAYERS_BACKGROUND);                         
                GUIAdvancedElement.drawCustomSizedTexturedRect( - GUISettings.instance().getTextureOffsetX(), - GUISettings.instance().getTextureOffsetY(), 0, 0, this.textureWidth, this.textureHeight, this.textureWidth, this.textureHeight);             
                GlStateManager.disableBlend();   
            } else {
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
            GlStateManager.popMatrix();            
        }
    }
}
