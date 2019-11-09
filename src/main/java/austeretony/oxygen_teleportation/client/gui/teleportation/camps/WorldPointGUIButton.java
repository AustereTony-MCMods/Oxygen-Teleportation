package austeretony.oxygen_teleportation.client.gui.teleportation.camps;

import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.IndexedGUIButton;
import austeretony.oxygen_core.client.gui.elements.CustomRectUtils;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationGUITextures;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import net.minecraft.client.renderer.GlStateManager;

public class WorldPointGUIButton extends IndexedGUIButton<Long> {

    private boolean favorite, locked, shared, downloaded;

    public WorldPointGUIButton(EnumWorldPoint type, WorldPoint worldPoint) {
        super(worldPoint.getId());
        this.setLocked(worldPoint.isLocked());
        if (type == EnumWorldPoint.CAMP) {
            this.setFavorite(worldPoint.getId() == TeleportationManagerClient.instance().getPlayerData().getFavoriteCampId());
            this.setDownloaded(!worldPoint.isOwner(OxygenHelperClient.getPlayerUUID()));
            this.setShared(TeleportationManagerClient.instance().getSharedCampsContainer().invitedPlayersExist(worldPoint.getId()));
        }
        this.setDynamicBackgroundColor(GUISettings.get().getEnabledElementColor(), GUISettings.get().getDisabledElementColor(), GUISettings.get().getHoveredElementColor());
        this.setTextDynamicColor(GUISettings.get().getEnabledTextColor(), GUISettings.get().getDisabledTextColor(), GUISettings.get().getHoveredTextColor());
        this.setDisplayText(worldPoint.getName());
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
        CustomRectUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
        drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
        CustomRectUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

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
            this.mc.getTextureManager().bindTexture(TeleportationGUITextures.SHARED_ICON);                        
            drawCustomSizedTexturedRect(iconX, 1, 0, 0, 8, 8, 8, 8);      
        } else if (this.downloaded) {
            iconX -= 8;
            this.mc.getTextureManager().bindTexture(TeleportationGUITextures.DOWNLOADED_ICON);                        
            drawCustomSizedTexturedRect(iconX, 1, 0, 0, 8, 8, 8, 8);
        }
        if (this.locked) {
            iconX -= 8;
            this.mc.getTextureManager().bindTexture(TeleportationGUITextures.LOCKED_ICON);                        
            drawCustomSizedTexturedRect(iconX, 1, 0, 0, 8, 8, 8, 8);      
        }
        if (this.favorite) {
            iconX -= 8;
            this.mc.getTextureManager().bindTexture(TeleportationGUITextures.FAVORITE_ICON);                        
            drawCustomSizedTexturedRect(iconX, 1, 0, 0, 8, 8, 8, 8);      
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
