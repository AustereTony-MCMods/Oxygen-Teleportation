package austeretony.teleportation.client.gui.menu;

import org.lwjgl.opengl.GL11;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldPointGUIButton extends GUIButton {

    public final WorldPoint worldPoint;

    private boolean favorite, shared;

    public WorldPointGUIButton(WorldPoint worldPoint) {
        super();
        this.worldPoint = worldPoint;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        super.draw(mouseX, mouseY);
        GlStateManager.pushMatrix();           
        GlStateManager.translate(this.getX(), this.getY(), 0.0F);           
        GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend(); 
        if (this.shared) {
            this.mc.getTextureManager().bindTexture(TeleportationMenuGUIScreen.SHARED_ICON);                        
            this.drawCustomSizedTexturedRect(this.getWidth() - 8, 1, 8, 0, 8, 8, 24, 8);      
        }
        if (this.favorite) {
            this.mc.getTextureManager().bindTexture(TeleportationMenuGUIScreen.FAVORITE_ICONS);                        
            this.drawCustomSizedTexturedRect(this.getWidth() - (this.shared ? 16 : 8), 1, 8, 0, 8, 8, 24, 8);      
        }
        GlStateManager.disableBlend(); 
        GlStateManager.popMatrix();
    }

    public boolean isFavorite() {
        return this.favorite;
    }

    public void setFavorite() {
        this.favorite = true;
    }

    public void resetFavorite() {
        this.favorite = false;
    }

    public boolean isShared() {
        return this.shared;
    }

    public void setShared() {
        this.shared = true;
    }

    public void resetShared() {
        this.shared = false;
    }
}
