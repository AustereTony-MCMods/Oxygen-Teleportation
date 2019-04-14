package austeretony.teleportation.client.gui.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.EnumDimensions;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class PreviewGUIImageLabel extends GUIImageLabel {

    private String name, owner, creationDate, position, dimension, noImage;

    private boolean favoriteIconEnabled, sharedIconEnabled, hasImage;

    private final Map<Long, ResourceLocation> cache = new HashMap<Long, ResourceLocation>();

    private final List<String> description = new ArrayList<String>();

    public PreviewGUIImageLabel(int xPosition, int yPosition) {
        super(xPosition, yPosition);
        this.disableFull();
        this.noImage = I18n.format("teleportation.gui.menu.noImage");
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
                this.mc.fontRenderer.drawString(this.noImage, 240 / 2 - this.width(this.noImage, 1.3F) + 5, 48, GUISettings.instance().getEnabledTextColor());
                GlStateManager.popMatrix();
            }
            this.drawGradientRect(ZERO, ZERO, 240, 70, 0x00000000, 0xC8000000);
            GlStateManager.disableBlend(); 
            this.drawRect(0, 121, 240, 135, 0xB4101010);
            if (this.sharedIconEnabled) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(TeleportationMenuGUIScreen.SHARED_ICON);                         
                this.drawCustomSizedTexturedRect(10 + this.width(this.name, 1.2F), 6, 10, 0, 10, 10, 30, 10);           
                GlStateManager.disableBlend(); 
            }
            if (this.favoriteIconEnabled) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(TeleportationMenuGUIScreen.FAVORITE_ICONS);                         
                this.drawCustomSizedTexturedRect((this.sharedIconEnabled ? 20 : 10) + this.width(this.name, 1.2F), 6, 10, 0, 10, 10, 30, 10);       	
                GlStateManager.disableBlend(); 
            }
            GlStateManager.pushMatrix();           
            GlStateManager.translate(0.0F, 0.0F, 0.0F);           
            GlStateManager.scale(1.2F, 1.2F, 0.0F);  
            this.mc.fontRenderer.drawString(this.name, 5, 6, GUISettings.instance().getEnabledTextColor(), true);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();           
            GlStateManager.translate(0.0F, 0.0F, 0.0F);           
            GlStateManager.scale(0.7F, 0.7F, 0.0F);  
            this.mc.fontRenderer.drawString(this.owner, 8, 28, GUISettings.instance().getEnabledTextColor(), true);
            this.mc.fontRenderer.drawString(this.creationDate, 8, 42, GUISettings.instance().getEnabledTextColor(), true);
            this.mc.fontRenderer.drawString(this.position, 8, 54, GUISettings.instance().getEnabledTextColor(), true);
            this.mc.fontRenderer.drawString(this.dimension, 8, 66, GUISettings.instance().getEnabledTextColor(), true);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();           
            GlStateManager.translate(0.0F, 0.0F, 0.0F);           
            GlStateManager.scale(0.8F, 0.8F, 0.0F);  
            if (!this.description.isEmpty())                      
                for (String line : this.description)                  
                    this.mc.fontRenderer.drawString(line, 20, 74 + 10 * this.description.indexOf(line), GUISettings.instance().getEnabledTextColor(), true); 
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    public void show(WorldPoint worldPoint, boolean forceLoad) {
        ResourceLocation imageLocation = this.getPreviewImage(worldPoint.getId(), forceLoad);
        if (imageLocation != null)
            this.setTexture(imageLocation, 240, 135);
        this.favoriteIconEnabled = worldPoint.getId() == TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId();
        this.sharedIconEnabled = TeleportationManagerClient.instance().getPlayerData().haveInvitedPlayers(worldPoint.getId());
        this.name = worldPoint.getName();
        this.owner = I18n.format("teleportation.gui.menu.info.owner") + " " + worldPoint.ownerName;
        this.creationDate = worldPoint.getCreationDate();
        this.position = String.valueOf((int) worldPoint.getXPos()) + ", " + String.valueOf((int) worldPoint.getYPos()) + ", " + String.valueOf((int) worldPoint.getZPos());
        this.dimension = EnumDimensions.getLocalizedNameFromId(worldPoint.getDimensionId());
        this.processDescription(worldPoint.getDescription());
        this.setVisible(true);
    }

    private void processDescription(String description) {     
        this.description.clear();     
        StringBuilder stringBuilder = new StringBuilder();      
        String[] words = description.split("[ ]");        
        if (words.length > 0) {                 
            for (int i = 0; i < words.length; i++) {            
                if (this.width(stringBuilder.toString() + words[i]) < 220)                          
                    stringBuilder.append(words[i]).append(" ");
                else {                          
                    if (this.description.size() * 10 <= 20)                                      
                        this.description.add(stringBuilder.toString());                       
                    stringBuilder = new StringBuilder();                                
                    stringBuilder.append(words[i]).append(" ");
                }                       
                if (i == words.length - 1)                              
                    if (this.description.size() * 10 <= 20)                            
                        this.description.add(stringBuilder.toString());
            }
        }       
    }

    public void hide() {
        this.setVisible(false);
    }

    private ResourceLocation getPreviewImage(long pointId, boolean forceLoad) {
        this.hasImage = true;
        if (!this.cache.containsKey(pointId) || forceLoad) {
            if (TeleportationManagerClient.instance().getImagesManager().getPreviewImages().get(pointId) != null) {
                ResourceLocation textureLocation = this.mc.getTextureManager().getDynamicTextureLocation(
                        "preview_" + String.valueOf(pointId),
                        new DynamicTexture(TeleportationManagerClient.instance().getImagesManager().getPreviewImages().get(pointId)));
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
