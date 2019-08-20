package austeretony.oxygen_teleportation.client.gui.teleportation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.EnumDimension;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.main.WorldPoint;
import austeretony.oxygen_teleportation.common.main.WorldPoint.EnumWorldPoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class WorldPointDataGUIElement extends GUIAdvancedElement<WorldPointDataGUIElement> {

    private String name, owner, creationDate, position, dimension, noImage;

    private boolean favorite, shared, downloaded, hasImage;

    private final Map<Long, ResourceLocation> cache = new HashMap<Long, ResourceLocation>();

    private final List<String> lines = new ArrayList<String>(5);

    public WorldPointDataGUIElement(int xPosition, int yPosition) {
        this.setPosition(xPosition, yPosition);
        this.disableFull();
        this.noImage = ClientReference.localize("teleportation.gui.menu.noImage");
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
            } else {
                drawRect(0, 0, 241, 135, 0xFF333333);
                GlStateManager.pushMatrix();           
                GlStateManager.translate(0.0F, 0.0F, 0.0F);           
                GlStateManager.scale(1.3F, 1.3F, 0.0F);  
                this.mc.fontRenderer.drawString(this.noImage, (240 - this.textWidth(this.noImage, 1.3F)) / 2, 48, GUISettings.instance().getEnabledTextColor());
                GlStateManager.popMatrix();
            }
            drawGradientRect(0, 0, 241, 70, 0x00000000, 0xC8000000);
            GlStateManager.disableBlend(); 
            drawRect(0, 120, 241, 135, 0xB4101010);
            if (this.shared) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(TeleportationGUITextures.SHARED_ICON);                         
                drawCustomSizedTexturedRect(10 + this.textWidth(this.name, 1.2F), 6, 10, 0, 10, 10, 10, 10);           
                GlStateManager.disableBlend(); 
            } else if (this.downloaded) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(TeleportationGUITextures.DOWNLOADED_ICON);                         
                drawCustomSizedTexturedRect(10 + this.textWidth(this.name, 1.2F), 6, 10, 0, 10, 10, 10, 10);           
                GlStateManager.disableBlend(); 
            }
            if (this.favorite) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(TeleportationGUITextures.FAVORITE_ICON);                         
                drawCustomSizedTexturedRect((this.shared || this.downloaded ? 20 : 10) + this.textWidth(this.name, 1.2F), 6, 0, 0, 10, 10, 10, 10);       	
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
            GlStateManager.translate(15.0F, 60.0F, 0.0F);           
            GlStateManager.scale(GUISettings.instance().getTextScale(), GUISettings.instance().getTextScale(), 0.0F);  
            int index = 0;
            for (String line : this.lines) {
                this.mc.fontRenderer.drawString(line, 0.0F, (this.mc.fontRenderer.FONT_HEIGHT + 2.0F) * index, this.getEnabledTextColor(), true);
                index++;
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    public void show(WorldPoint worldPoint, EnumWorldPoint type, boolean forceLoad) {
        ResourceLocation imageLocation = this.getPreviewImage(worldPoint.getId(), forceLoad);
        if (imageLocation != null)
            this.setTexture(imageLocation, 241, 135);
        if (type == EnumWorldPoint.CAMP) {
            this.favorite = worldPoint.getId() == TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId();
            this.shared = TeleportationManagerClient.instance().getSharedCampsManager().invitedPlayersExist(worldPoint.getId());
            this.downloaded = !worldPoint.isOwner(OxygenHelperClient.getPlayerUUID());
        }
        this.name = worldPoint.getName();
        this.owner = ClientReference.localize("teleportation.gui.menu.info.owner") + " " + worldPoint.ownerName;
        this.creationDate = worldPoint.getCreationDate();
        this.position = String.valueOf((int) worldPoint.getXPos()) + ", " + String.valueOf((int) worldPoint.getYPos()) + ", " + String.valueOf((int) worldPoint.getZPos());
        this.dimension = EnumDimension.getLocalizedNameFromId(worldPoint.getDimensionId());
        this.processDescription(worldPoint.getDescription());
        this.setVisible(true);
    }

    private void processDescription(String description) {     
        this.lines.clear(); 
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
            if ((this.textHeight(this.getTextScale()) + 2) * this.lines.size() >= 80)
                break;
            if (symbol != ' ') {
                wordProcessing = true;
                if (prevSymbol == ' ')
                    wordStartIndex = index;
            }
            if (symbol == '\n') {
                this.lines.add(builder.toString());
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
                    this.lines.add(builder.toString().substring(0, wordStartIndex));
                    builder.delete(0, wordStartIndex);
                } else {
                    this.lines.add(builder.toString());
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
            this.lines.add(builder.toString());
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
