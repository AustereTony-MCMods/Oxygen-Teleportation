package austeretony.oxygen_teleportation.client.gui.teleportation.camps;

import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenWrapperPanelEntry;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import net.minecraft.client.renderer.GlStateManager;

public class WorldPointPanelEntry extends OxygenWrapperPanelEntry<Long> {

    private boolean favorite, locked, shared, downloaded;

    public WorldPointPanelEntry(EnumWorldPoint type, WorldPoint worldPoint) {
        super(worldPoint.getId());
        this.setLocked(worldPoint.isLocked());
        if (type == EnumWorldPoint.CAMP) {
            this.setFavorite(worldPoint.getId() == TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId());
            this.setDownloaded(!worldPoint.isOwner(OxygenHelperClient.getPlayerUUID()));
            this.setShared(TeleportationManagerClient.instance().getSharedCampsContainer().invitedPlayersExist(worldPoint.getId()));
        }
        this.setDisplayText(worldPoint.getName());
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());        
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();           
        GlStateManager.translate(this.getX(), this.getY(), 0.0F);   
        GlStateManager.scale(this.getScale(), this.getScale(), 0.0F); 
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int 
        color = this.getEnabledBackgroundColor(), 
        textColor = this.getEnabledTextColor(), 
        textY = (this.getHeight() - this.textHeight(this.getTextScale())) / 2 + 1;                      
        if (!this.isEnabled()) {                 
            color = this.getDisabledBackgroundColor();
            textColor = this.getDisabledTextColor();           
        } else if (this.isHovered() || this.isToggled()) {                 
            color = this.getHoveredBackgroundColor();
            textColor = this.getHoveredTextColor();
        }

        int third = this.getWidth() / 3;
        OxygenGUIUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
        drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
        OxygenGUIUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

        GlStateManager.pushMatrix();           
        GlStateManager.translate(2.0F, textY, 0.0F); 
        GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
        this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, textColor, this.isTextShadowEnabled());
        GlStateManager.popMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend(); 
        int iconX = this.getWidth() - 2;
        if (this.shared) {
            iconX -= 8;
            this.mc.getTextureManager().bindTexture(OxygenGUITextures.SHARE_ICONS);                        
            drawCustomSizedTexturedRect(iconX, 1, 0, 0, 8, 8, 24, 8);      
        } else if (this.downloaded) {
            iconX -= 8;
            this.mc.getTextureManager().bindTexture(OxygenGUITextures.DOWNLOAD_ICONS);                        
            drawCustomSizedTexturedRect(iconX, 1, 0, 0, 8, 8, 24, 8);
        }
        if (this.locked) {
            iconX -= 8;
            this.mc.getTextureManager().bindTexture(OxygenGUITextures.LOCK_ICONS);                        
            drawCustomSizedTexturedRect(iconX, 1, 0, 0, 8, 8, 24, 8);      
        }
        if (this.favorite) {
            iconX -= 8;
            this.mc.getTextureManager().bindTexture(OxygenGUITextures.STAR_ICONS);                        
            drawCustomSizedTexturedRect(iconX, 1, 0, 0, 8, 8, 24, 8);      
        }
        GlStateManager.disableBlend(); 

        GlStateManager.popMatrix();
    }

    public void setFavorite(boolean flag) {
        this.favorite = flag;
    }

    public void setShared(boolean flag) {
        this.shared = flag;
    }

    public void setDownloaded(boolean flag) {
        this.downloaded = flag;
    }

    public void setLocked(boolean flag) {
        this.locked = flag;
    }
}
