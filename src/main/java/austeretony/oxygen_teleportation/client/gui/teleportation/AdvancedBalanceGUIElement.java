package austeretony.oxygen_teleportation.client.gui.teleportation;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen.client.api.ItemRenderHelper;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.gui.settings.GUISettings;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class AdvancedBalanceGUIElement extends GUISimpleElement<AdvancedBalanceGUIElement> {

    private ItemStack itemStack;
    
    private boolean isRed;

    private int balance;

    public AdvancedBalanceGUIElement(int x, int y) {
        this.setPosition(x, y);
        this.setSize(8, 8);
        this.setBalance(0);
        this.enableFull();
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
            
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            
            if (this.itemStack == null) {
                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(OxygenGUITextures.COIN_ICON);
                GUIAdvancedElement.drawCustomSizedTexturedRect(0, 0, 0, 0, 6, 6, 6, 6);          
                GlStateManager.disableBlend();
            } else {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(0.0F, - 1.0F, 0.0F);            
                GlStateManager.scale(0.5F, 0.5F, 0.5F);     

                RenderHelper.enableGUIStandardItemLighting();            
                GlStateManager.enableDepth();
                ItemRenderHelper.renderItemWithoutEffectIntoGUI(this.itemStack, 0, 0);                              
                GlStateManager.disableDepth();
                RenderHelper.disableStandardItemLighting();

                GlStateManager.popMatrix();
            } 
            
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            
            GlStateManager.pushMatrix();           
            GlStateManager.translate(- 1.0F - this.textWidth(this.getDisplayText(), GUISettings.instance().getSubTextScale()), 1.0F, 0.0F);            
            GlStateManager.scale(GUISettings.instance().getSubTextScale(), GUISettings.instance().getSubTextScale(), 0.0F);                                      
            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, this.isRed ? 0xFFCC0000 : this.getEnabledTextColor(), false);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (this.itemStack != null && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + 8 && mouseY < this.getY() + 8)
            this.screen.drawToolTip(this.itemStack, 
                    mouseX - this.textWidth(this.itemStack.getDisplayName(), 1.0F) - (ClientReference.getGameSettings().advancedItemTooltips ? 75 : 25), mouseY);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public AdvancedBalanceGUIElement setBalance(int value) {
        this.balance = value;
        this.setDisplayText(String.valueOf(value));
        return this;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setRed(boolean flag) {
        this.isRed = flag;   
    }
}
