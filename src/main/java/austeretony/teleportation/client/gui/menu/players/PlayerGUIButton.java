package austeretony.teleportation.client.gui.menu.players;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen.client.gui.friends.FriendsListGUIScreen;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.main.SharedPlayerData;
import net.minecraft.client.renderer.GlStateManager;

public class PlayerGUIButton extends GUIButton {

    public final SharedPlayerData playerData;

    private String dimension, jumpProfile;

    private int statusIconU;

    public PlayerGUIButton(SharedPlayerData playerData, String username, String dimension, String jumpProfile, OxygenPlayerData.EnumStatus status) {
        super();
        this.playerData = playerData;
        this.dimension = dimension;
        this.jumpProfile = jumpProfile;
        this.statusIconU = status.ordinal() * 3;
        this.setDisplayText(username, false, GUISettings.instance().getTextScale());//need for search mechanic
        this.setTextAlignment(EnumGUIAlignment.LEFT, 24);
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        super.draw(mouseX, mouseY);
        if (this.isVisible()) {         
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);    
            this.mc.getTextureManager().bindTexture(FriendsListGUIScreen.STATUS_ICONS); 
            this.drawCustomSizedTexturedRect(7, 3, this.statusIconU, 0, 3, 3, 12, 3); 
            GlStateManager.scale(GUISettings.instance().getTextScale(), GUISettings.instance().getTextScale(), 0.0F);    
            int color;                      
            if (!this.isEnabled())                  
                color = this.getDisabledTextColor();           
            else if (this.isHovered() || this.isToggled())                                          
                color = this.getHoveredTextColor();
            else                    
                color = this.getEnabledTextColor();        
            this.mc.fontRenderer.drawString(this.dimension, 157, 2, color, this.isTextShadowEnabled());
            this.mc.fontRenderer.drawString(this.jumpProfile, 278, 2, color, this.isTextShadowEnabled());
            GlStateManager.popMatrix();
        }
    }
}
