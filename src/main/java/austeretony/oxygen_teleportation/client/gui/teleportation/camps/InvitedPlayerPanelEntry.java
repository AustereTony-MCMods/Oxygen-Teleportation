package austeretony.oxygen_teleportation.client.gui.teleportation.camps;

import java.util.UUID;

import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedButton;
import austeretony.oxygen_core.client.gui.elements.OxygenWrapperPanelEntry;
import austeretony.oxygen_core.common.PlayerSharedData;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import net.minecraft.client.renderer.GlStateManager;

public class InvitedPlayerPanelEntry extends OxygenWrapperPanelEntry<UUID> {

    private OxygenTexturedButton uninviteButton;

    private final long pointId;

    public InvitedPlayerPanelEntry(PlayerSharedData sharedData, long pointId) {
        super(sharedData.getPlayerUUID());
        this.pointId = pointId;
        this.setDisplayText(sharedData.getUsername());
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt()); 
    }

    @Override
    public void init() { 
        this.uninviteButton = new OxygenTexturedButton(this.getWidth() - 8, 2, 6, 6, OxygenGUITextures.CROSS_ICONS, 6, 6, "").initScreen(this.getScreen());
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {          
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            int color;                      
            if (!this.isEnabled())                  
                color = this.getDisabledBackgroundColor();
            else if (this.isHovered() || this.isToggled())                  
                color = this.getHoveredBackgroundColor();
            else                    
                color = this.getEnabledBackgroundColor();     

            int third = this.getWidth() / 3;
            OxygenGUIUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
            drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
            OxygenGUIUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

            if (!this.isEnabled())                  
                color = this.getDisabledTextColor();           
            else if (this.isHovered() || this.isToggled())                                          
                color = this.getHoveredTextColor();
            else                    
                color = this.getEnabledTextColor();

            GlStateManager.pushMatrix();           
            GlStateManager.translate(1.0F, (this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F + 1.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);           
            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, color, this.isTextShadowEnabled());
            GlStateManager.popMatrix();     

            this.uninviteButton.draw(mouseX, mouseY);

            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {       
        if (this.uninviteButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            TeleportationManagerClient.instance().getPlayerDataManager().uninvitePlayerSynced(this.pointId, this.getWrapped());
            return true;
        }
        return false;
    }

    @Override
    public void mouseOver(int mouseX, int mouseY) {
        this.uninviteButton.mouseOver(mouseX - this.getX(), mouseY - this.getY());
        this.setHovered(this.isEnabled() && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + (int) (this.getWidth() * this.getScale()) && mouseY < this.getY() + (int) (this.getHeight() * this.getScale()));   
    }
}
