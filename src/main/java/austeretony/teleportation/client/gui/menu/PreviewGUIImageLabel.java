package austeretony.teleportation.client.gui.menu;

import java.util.HashMap;
import java.util.Map;

import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.world.EnumDimensions;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class PreviewGUIImageLabel extends GUIImageLabel {

    private final Map<Long, ResourceLocation> cache = new HashMap<Long, ResourceLocation>();

    private String name, owner, creationDate, position, dimension, description, noImage;

    private boolean enableFavMark, hasImage;

    public PreviewGUIImageLabel(int xPosition, int yPosition) {
        super(xPosition, yPosition);
        this.disableFull();
        this.noImage = I18n.format("teleportation.menu.noImage");
    }

    @Override
    public void draw(int mouseX, int mouseY) {  
        if (this.isVisible()) {   
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);           
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);    
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();             
            if (this.hasImage) {
                this.mc.getTextureManager().bindTexture(this.getTexture());                         
                this.drawCustomSizedTexturedRect(ZERO, ZERO, this.getTextureU(), this.getTextureV(), this.getTextureWidth(), this.getTextureHeight(), this.getImageWidth(), this.getImageHeight());  
            } else {
                this.drawRect(ZERO, ZERO, 240, 135, 0xFF333333);
                GlStateManager.pushMatrix();           
                GlStateManager.translate(0.0F, 0.0F, 0.0F);           
                GlStateManager.scale(1.3F, 1.3F, 0.0F);  
                this.mc.fontRenderer.drawString(this.noImage, 240 / 2 - (int) ((float) this.width(this.noImage) * 1.3F) + 5, 48, 0xFFD1D1D1);
                GlStateManager.popMatrix();
            }
            this.drawGradientRect(ZERO, ZERO, 240, 70, 0x00000000, 0xC8000000);
            GlStateManager.disableBlend(); 
            this.drawRect(0, 121, 240, 135, 0xB4101010);
            if (this.enableFavMark) {
                GlStateManager.color(1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(MenuGUIScreen.FAVORITE_ICONS);                         
                this.drawCustomSizedTexturedRect(8 + (int) ((float) this.width(this.name) * 1.3F), 3, 10, 0, 10, 10, 30, 10);       	
                GlStateManager.disableBlend(); 
            }
            GlStateManager.pushMatrix();           
            GlStateManager.translate(0.0F, 0.0F, 0.0F);           
            GlStateManager.scale(1.3F, 1.3F, 0.0F);  
            this.mc.fontRenderer.drawString(this.name, 4, 3, 0xFFD1D1D1, true);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();           
            GlStateManager.translate(0.0F, 0.0F, 0.0F);           
            GlStateManager.scale(0.7F, 0.7F, 0.0F);  
            this.mc.fontRenderer.drawString(this.owner, 8, 24, 0xFFD1D1D1, true);
            this.mc.fontRenderer.drawString(this.creationDate, 8, 38, 0xFFD1D1D1, true);
            this.mc.fontRenderer.drawString(this.position, 8, 50, 0xFFD1D1D1, true);
            this.mc.fontRenderer.drawString(this.dimension, 8, 62, 0xFFD1D1D1, true);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();           
            GlStateManager.translate(0.0F, 0.0F, 0.0F);           
            GlStateManager.scale(0.9F, 0.9F, 0.0F);  
            this.mc.fontRenderer.drawString(this.description, 16, 60, 0xFFD1D1D1, true);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    public void show(WorldPoint worldPoint, boolean forceLoad) {
        ResourceLocation imageLocation = this.getPreviewImage(worldPoint.getId(), forceLoad);
        if (imageLocation != null)
            this.setTexture(imageLocation, 240, 135);
        this.enableFavMark = worldPoint.getId() == CampsManagerClient.instance().getPlayerProfile().getFavoriteCampId();
        this.name = worldPoint.getName();
        this.owner = I18n.format("teleportation.menu.info.owner") + " " + worldPoint.ownerName;
        this.creationDate = worldPoint.getCreationDate();
        this.position = String.valueOf((int) worldPoint.getXPos()) + ", " + String.valueOf((int) worldPoint.getYPos()) + ", " + String.valueOf((int) worldPoint.getZPos());
        this.dimension = EnumDimensions.getLocalizedNameFromId(worldPoint.getDimensionId());
        this.description = worldPoint.getDescription();
        this.setVisible(true);
    }

    public void hide() {
        this.setVisible(false);
    }

    private ResourceLocation getPreviewImage(long pointId, boolean forceLoad) {
        this.hasImage = true;
        if (!this.cache.containsKey(pointId) || forceLoad) {
            if (CampsManagerClient.instance().getPreviewImages().get(pointId) != null) {
                ResourceLocation textureLocation = this.mc.getTextureManager().getDynamicTextureLocation(
                        "preview_" + String.valueOf(pointId),
                        new DynamicTexture(CampsManagerClient.instance().getPreviewImages().get(pointId)));
                this.cache.put(pointId, textureLocation);
                this.hasImage = true;
                return textureLocation;		
            } else {
                this.hasImage = false;
                return null;
            }
        } else
            return this.cache.get(pointId);
    }
}
