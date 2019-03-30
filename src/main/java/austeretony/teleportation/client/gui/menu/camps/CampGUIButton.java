package austeretony.teleportation.client.gui.menu.camps;

import austeretony.alternateui.screen.button.GUIButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CampGUIButton extends GUIButton {

    private boolean isFavorite;

    private ResourceLocation favTexture;

    private int favTextureWidth, favTextureHeight, favU, favV, favImageWidth, favImageHeight;

    public CampGUIButton() {
        super();
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        super.draw(mouseX, mouseY);
        if (this.isFavorite) {
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);           
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);                                          
            if (this.favTexture != null) {                  
                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(this.favTexture);                        
                this.drawCustomSizedTexturedRect(this.getWidth() - this.favTextureWidth, (int) ((float) (this.getHeight() - this.favTextureHeight) / 2.0F), this.favU, this.favV, 
                        this.favTextureWidth, this.favTextureHeight, this.favImageWidth, this.favImageHeight);          
                GlStateManager.disableBlend(); 
            }
            GlStateManager.popMatrix();
        }
    }

    public boolean isFavorite() {
        return this.isFavorite;
    }

    public void setFavorite() {
        this.isFavorite = true;
    }

    public void resetFavorite() {
        this.isFavorite = false;
    }

    public void setFavTexture(ResourceLocation favTexture) {
        this.favTexture = favTexture;
    }

    public void setFavTexture(ResourceLocation favTexture, int textureWidth, int textureHeight) {
        this.setFavTexture(favTexture);
        this.favTextureWidth = textureWidth;
        this.favTextureHeight = textureHeight;
    }

    public void setFavTexture(ResourceLocation favTexture, int textureWidth, int textureHeight, int u, int v, int imageWidth, int imageHeight) {
        this.setFavTexture(favTexture, textureWidth, textureHeight);
        this.favU = u;
        this.favV = v;
        this.favImageWidth = imageWidth;
        this.favImageHeight = imageHeight;
    }
}
