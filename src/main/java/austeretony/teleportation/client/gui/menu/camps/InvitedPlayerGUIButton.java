package austeretony.teleportation.client.gui.menu.camps;

import java.util.UUID;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.oxygen.client.gui.notifications.NotificationsGUIScreen;
import austeretony.teleportation.client.TeleportationManagerClient;
import net.minecraft.client.renderer.GlStateManager;

public class InvitedPlayerGUIButton extends GUIButton {

    public final UUID playerUUID;

    public final long pointId;

    private GUIButton uninviteButton;

    private boolean initialized;

    public InvitedPlayerGUIButton(UUID playerUUID, long pointId) {
        this.playerUUID = playerUUID;
        this.pointId = pointId;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (!this.initialized) {
            this.initialized = true;
            this.uninviteButton = new GUIButton(this.getWidth() - 8, 2, 6, 6).setTexture(NotificationsGUIScreen.CROSS_ICONS, 6, 6).initScreen(this.getScreen());
        }
        super.draw(mouseX, mouseY);
        if (this.isVisible()) {         
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);           
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);   
            this.uninviteButton.draw(mouseX, mouseY);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {       
        if (this.uninviteButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            TeleportationManagerClient.instance().getCampsManager().uninvitePlayerSynced(this.pointId, this.playerUUID);
            this.uninviteButton.disable();
            this.disable();
            return true;
        }
        return false;
    }

    @Override
    public void mouseOver(int mouseX, int mouseY) {
        if (this.initialized)
            this.uninviteButton.mouseOver(mouseX - this.getX(), mouseY - this.getY());
        this.setHovered(this.isEnabled() && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + (int) (this.getWidth() * this.getScale()) && mouseY < this.getY() + (int) (this.getHeight() * this.getScale()));   
    }
}
