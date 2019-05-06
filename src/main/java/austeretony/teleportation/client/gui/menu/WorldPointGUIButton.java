package austeretony.teleportation.client.gui.menu;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.renderer.GlStateManager;

public class WorldPointGUIButton extends GUIButton {

    public final WorldPoint worldPoint;

    private boolean favorite, shared, downloaded;

    public WorldPointGUIButton(WorldPoint worldPoint) {
        super();
        this.worldPoint = worldPoint;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
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
        GlStateManager.translate(2.0F, textY, 0.0F); 
        GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
        this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, textColor, this.isTextShadowEnabled());
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend(); 
        if (this.shared) {
            this.mc.getTextureManager().bindTexture(TeleportationGUITextures.SHARED_ICON);                        
            drawCustomSizedTexturedRect(this.getWidth() - 8, 1, 8, 0, 8, 8, 8, 8);      
        } else if (this.downloaded) {
            this.mc.getTextureManager().bindTexture(TeleportationGUITextures.DOWNLOADED_ICON);                        
            drawCustomSizedTexturedRect(this.getWidth() - 8, 1, 8, 0, 8, 8, 8, 8);
        }
        if (this.favorite) {
            this.mc.getTextureManager().bindTexture(TeleportationGUITextures.FAVORITE_ICON);                        
            drawCustomSizedTexturedRect(this.getWidth() - (this.shared || this.downloaded ? 16 : 8), 1, 0, 0, 8, 8, 8, 8);      
        }
        GlStateManager.disableBlend(); 
        GlStateManager.popMatrix();
    }

    public void setFavorite() {
        this.favorite = true;
    }

    public void resetFavorite() {
        this.favorite = false;
    }

    public void setShared() {
        this.shared = true;
    }

    public void setDownloaded() {
        this.downloaded = true;
    }
}
