package austeretony.teleportation.client.gui.menu.players;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.util.EnumGUIAlignment;
import net.minecraft.client.renderer.GlStateManager;

public class PlayerGUIButton extends GUIButton {

    private String username, dimension, jumpProfile;

    public PlayerGUIButton(String username, String dimension, String jumpProfile) {
        super();
        this.username = username;
        this.dimension = dimension;
        this.jumpProfile = jumpProfile;
        this.setDisplayText(username, false, 0.85F);//need for search mechanic
        this.setTextAlignment(EnumGUIAlignment.LEFT, 2);
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        super.draw(mouseX, mouseY);
        if (this.isVisible()) {         
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);           
            GlStateManager.scale(0.85F, 0.85F, 0.0F);    
            int color;                      
            if (!this.isEnabled())                  
                color = this.getDisabledTextColor();           
            else if (this.isHovered() || this.isToggled())                                          
                color = this.getHoveredTextColor();
            else                    
                color = this.getEnabledTextColor();        
            this.mc.fontRenderer.drawString(this.dimension, 110, 2, color, this.isTextShadowEnabled());
            this.mc.fontRenderer.drawString(this.jumpProfile, 230, 2, color, this.isTextShadowEnabled());
            GlStateManager.popMatrix();
        }
    }
}
