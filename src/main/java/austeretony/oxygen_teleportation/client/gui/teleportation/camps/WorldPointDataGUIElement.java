package austeretony.oxygen_teleportation.client.gui.teleportation.camps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.api.EnumDimension;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.WorldPoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class WorldPointDataGUIElement extends GUIAdvancedElement<WorldPointDataGUIElement> {

    private String name, owner, creationDate, position, dimension;

    private boolean hasImage;

    private final Map<Long, ResourceLocation> cache = new HashMap<>();

    private final List<String> description = new ArrayList<>(5);

    public WorldPointDataGUIElement(int xPosition, int yPosition) {
        this.setPosition(xPosition, yPosition);
        this.setDynamicBackgroundColor(GUISettings.get().getEnabledElementColor(), GUISettings.get().getDisabledElementColor(), GUISettings.get().getHoveredElementColor());
        this.setTextDynamicColor(GUISettings.get().getEnabledTextColor(), GUISettings.get().getDisabledTextColor(), GUISettings.get().getHoveredTextColor());
        this.disableFull();
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
                drawCustomSizedTexturedRect(0, 0, this.getTextureU(), this.getTextureV(), this.getTextureWidth(), this.getTextureHeight(), this.getImageWidth(), this.getImageHeight());  
            } else
                drawRect(0, 0, 241, 135, 0xFF333333);
            drawGradientRect(0, 0, 241, 70, 0x00000000, 0xC8000000);
            GlStateManager.disableBlend(); 

            drawRect(0, 120, 241, 135, GUISettings.get().getBaseGUIBackgroundColor());

            float textScale = GUISettings.get().getTitleScale() + 0.2F;

            GlStateManager.pushMatrix();           
            GlStateManager.translate(5.0F, 6.0F, 0.0F);           
            GlStateManager.scale(textScale, textScale, 0.0F);  
            this.mc.fontRenderer.drawString(this.name, 0, 0, this.getEnabledTextColor(), true);
            GlStateManager.popMatrix();

            textScale = GUISettings.get().getSubTextScale() - 0.05F;

            GlStateManager.pushMatrix();           
            GlStateManager.translate(5.0F, 18.0F, 0.0F);           
            GlStateManager.scale(textScale, textScale, 0.0F);  
            this.mc.fontRenderer.drawString(this.owner, 0, 0, this.getEnabledTextColor(), true);
            GlStateManager.popMatrix();

            textScale = GUISettings.get().getSubTextScale() - 0.02F;
            
            GlStateManager.pushMatrix();           
            GlStateManager.translate(5.0F, 28.0F, 0.0F);           
            GlStateManager.scale(textScale, textScale, 0.0F);  
            this.mc.fontRenderer.drawString(this.creationDate, 0, 0, this.getEnabledTextColor(), true);
            this.mc.fontRenderer.drawString(this.position, 0, 12, this.getEnabledTextColor(), true);
            this.mc.fontRenderer.drawString(this.dimension, 0, 24, this.getEnabledTextColor(), true);
            GlStateManager.popMatrix();

            textScale = GUISettings.get().getSubTextScale() + 0.05F;

            GlStateManager.pushMatrix();           
            GlStateManager.translate(15.0F, 56.0F, 0.0F);           
            GlStateManager.scale(textScale, textScale, 0.0F);  
            int index = 0;
            for (String line : this.description) {
                this.mc.fontRenderer.drawString(line, 0.0F, (this.mc.fontRenderer.FONT_HEIGHT + 2.0F) * index, this.getEnabledTextColor(), true);
                index++;
            }
            GlStateManager.popMatrix();

            GlStateManager.popMatrix();
        }
    }

    public void show(WorldPoint worldPoint, boolean reloadImage) {
        ResourceLocation image = this.getPreviewImage(worldPoint.getId(), reloadImage);
        if (image != null)
            this.setTexture(image, 241, 135);
        this.name = worldPoint.getName();
        this.owner = ClientReference.localize("oxygen_teleportation.gui.menu.info.owner") + " " + worldPoint.getOwnerName();
        this.creationDate = worldPoint.getCreationDate();
        this.position = String.valueOf((int) worldPoint.getXPos()) + ", " + String.valueOf((int) worldPoint.getYPos()) + ", " + String.valueOf((int) worldPoint.getZPos());
        this.dimension = EnumDimension.getLocalizedNameFromId(worldPoint.getDimensionId());
        this.processDescription(worldPoint.getDescription());
        this.setVisible(true);
    }

    private void processDescription(String description) {     
        this.description.clear(); 
        int width = 300;
        StringBuilder builder = new StringBuilder();    
        int 
        index = 0, 
        wordStartIndex = 0;
        boolean
        rechedLimit = false,
        wordProcessing = false;
        char prevSymbol = '0';
        String line;
        for (char symbol : description.toCharArray()) {
            if ((this.textHeight(this.getTextScale()) + 2) * this.description.size() >= 80)
                break;
            if (symbol != ' ') {
                wordProcessing = true;
                if (prevSymbol == ' ')
                    wordStartIndex = index;
            }
            if (symbol == '\n') {
                this.description.add(builder.toString());
                builder.delete(0, builder.length());
                index = 0;
                continue;
            }
            if (this.textWidth(builder.toString() + String.valueOf(symbol), this.getTextScale()) <= width)
                builder.append(symbol);
            else {
                if (symbol == '.' 
                        || symbol == ',' 
                        || symbol == '!'
                        || symbol == '?')
                    builder.append(symbol);
                if (wordProcessing) {
                    this.description.add(builder.toString().substring(0, wordStartIndex));
                    builder.delete(0, wordStartIndex);
                } else {
                    this.description.add(builder.toString());
                    builder.delete(0, builder.length());
                }
                if (symbol != ' ')
                    builder.append(symbol);
                index = builder.length() - 1;
            }
            wordProcessing = false;
            prevSymbol = symbol;
            index++;
        }
        if (builder.length() != 0)
            this.description.add(builder.toString());
    }

    public void hide() {
        this.setVisible(false);
    }

    private ResourceLocation getPreviewImage(long pointId, boolean reloadImage) {
        this.hasImage = true;
        if (!this.cache.containsKey(pointId) || reloadImage) {
            if (TeleportationManagerClient.instance().getImagesManager().getPreviewImages().get(pointId) != null) {
                ResourceLocation image = this.mc.getTextureManager().getDynamicTextureLocation(
                        String.valueOf(pointId),
                        new DynamicTexture(TeleportationManagerClient.instance().getImagesManager().getPreviewImages().get(pointId)));
                this.cache.put(pointId, image);
                this.hasImage = true;
                return image;		
            } else {
                this.hasImage = false;
                return null;
            }
        } else
            return this.cache.get(pointId);
    }
}
