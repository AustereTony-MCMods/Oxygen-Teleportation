package austeretony.teleportation.client.gui.overlay;

import austeretony.alternateui.overlay.core.GUIOverlay;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.teleportation.client.handler.KeyHandler;
import austeretony.teleportation.common.menu.players.JumpRequestClient;
import austeretony.teleportation.common.menu.players.PlayersManagerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NotificationRenderer {

    private GUIOverlay overlay;

    private GUITextLabel requestTextLabel, elapsedTimeTextLabel, acceptTextLabel, rejectTextLabel;

    private JumpRequestClient jumpRequest;

    public NotificationRenderer() {
        this.overlay = new GUIOverlay(0.85F);
        this.overlay.addElement(this.requestTextLabel = new GUITextLabel(0, 0));
        this.overlay.addElement(this.elapsedTimeTextLabel = new GUITextLabel(0, 20));
        this.overlay.addElement(this.acceptTextLabel = new GUITextLabel(25, 20));
        this.overlay.addElement(this.rejectTextLabel = new GUITextLabel(180, 20));
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == ElementType.CROSSHAIRS)
            event.setCanceled(!Minecraft.getMinecraft().inGameHasFocus);
        if (event.getType() == ElementType.TEXT)
            this.draw();
    }

    private void draw() {
        if (PlayersManagerClient.instance() == null) return;
        this.jumpRequest = PlayersManagerClient.instance().getJumpRequest();
        if (this.jumpRequest.exist()) {
            if (this.jumpRequest.recentlyStarted()) {
                this.overlay.setAlignment(EnumGUIAlignment.CENTER, - 118, 60);
                this.requestTextLabel.setDisplayText(I18n.format("teleportation.overlay.request", this.jumpRequest.getVisitorUsername()), true);
                this.acceptTextLabel.setDisplayText("[" + KeyHandler.ACCEPT.getDisplayName() + "] " + I18n.format(KeyHandler.ACCEPT.getKeyDescription()), true);
                this.rejectTextLabel.setDisplayText("[" + KeyHandler.REJECT.getDisplayName() + "] " + I18n.format(KeyHandler.REJECT.getKeyDescription()), true);
            }
            this.elapsedTimeTextLabel.setDisplayText("(" + this.jumpRequest.getElapsedTime() + ")", true);
            this.overlay.draw();
        }
    }
}
