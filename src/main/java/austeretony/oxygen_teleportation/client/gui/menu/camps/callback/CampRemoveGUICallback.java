package austeretony.oxygen_teleportation.client.gui.menu.camps.callback;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.menu.CampsGUISection;
import austeretony.oxygen_teleportation.client.gui.menu.TeleportationMenuGUIScreen;
import net.minecraft.client.resources.I18n;

public class CampRemoveGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final CampsGUISection section;

    private GUITextLabel requestLabel;

    private GUIButton confirmButton, cancelButton;

    public CampRemoveGUICallback(TeleportationMenuGUIScreen screen, CampsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new GUIImageLabel(- 1, - 1, this.getWidth() + 2, this.getHeight() + 2).enableStaticBackground(GUISettings.instance().getBaseGUIBackgroundColor()));//main background 1st layer
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), 11).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUIImageLabel(0, 12, this.getWidth(), this.getHeight() - 12).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUITextLabel(2, 2).setDisplayText(I18n.format("teleportation.gui.menu.removeCampCallback"), true, GUISettings.instance().getTitleScale()));
        this.addElement(this.requestLabel = new GUITextLabel(2, 16));     

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(I18n.format("oxygen.gui.confirmButton"), true, GUISettings.instance().getButtonTextScale()));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(I18n.format("oxygen.gui.cancelButton"), true, GUISettings.instance().getButtonTextScale()));
    }

    @Override   
    protected void onOpen() {
        this.requestLabel.setDisplayText(I18n.format("teleportation.gui.menu.removeCallback.request", this.section.getCurrentPoint().getName()), false, GUISettings.instance().getTextScale());
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            TeleportationManagerClient.instance().getCampsManager().removeCampPointSynced(this.section.getCurrentPoint().getId());
            this.section.sortPoints(0);
            this.section.unlockCreateButton();
            this.close();
        }
    }
}
