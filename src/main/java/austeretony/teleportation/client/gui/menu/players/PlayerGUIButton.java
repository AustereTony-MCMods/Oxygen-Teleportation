package austeretony.teleportation.client.gui.menu.players;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.main.SharedPlayerData;
import net.minecraft.client.renderer.GlStateManager;

public class PlayerGUIButton extends GUIButton {

    public final SharedPlayerData playerData;

    private String dimension, jumpProfile;

    private int statusIconU;

    public PlayerGUIButton(SharedPlayerData playerData, String username, String dimension, String jumpProfile, OxygenPlayerData.EnumStatus status) {
        super();
        this.playerData = playerData;
        this.dimension = dimension;
        this.jumpProfile = jumpProfile;
        this.statusIconU = status.ordinal() * 3;
        this.setDisplayText(username, false, GUISettings.instance().getTextScale());//need for search mechanic
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {         
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);
            int color, textColor, textY;                      
            if (!this.isEnabled()) {                 
                color = this.getDisabledBackgroundColor();
                textColor = this.getDisabledTextColor();           
            } else if (this.isHovered() || this.isToggled()) {                 
                color = this.getHoveredBackgroundColor();
                textColor = this.getHoveredTextColor();
            } else {                   
                color = this.getEnabledBackgroundColor(); 
                textColor = this.getEnabledTextColor();      
            }
            drawRect(0, 0, this.getWidth(), this.getHeight(), color);
            textY = (this.getHeight() - this.textHeight(this.getTextScale())) / 2 + 1;
            GlStateManager.pushMatrix();           
            GlStateManager.translate(24.0F, textY, 0.0F); 
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, textColor, this.isTextShadowEnabled());
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();    
            GlStateManager.translate(110.0F, textY, 0.0F); 
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
            this.mc.fontRenderer.drawString(this.dimension, 0, 0, textColor, this.isTextShadowEnabled());
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();    
            GlStateManager.translate(195.0F, textY, 0.0F); 
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
            this.mc.fontRenderer.drawString(this.jumpProfile, 0, 0, textColor, this.isTextShadowEnabled());
            GlStateManager.popMatrix();
            this.mc.getTextureManager().bindTexture(OxygenGUITextures.STATUS_ICONS); 
            drawCustomSizedTexturedRect(7, 3, this.statusIconU, 0, 3, 3, 12, 3);   
            GlStateManager.popMatrix();
        }
    }
}
