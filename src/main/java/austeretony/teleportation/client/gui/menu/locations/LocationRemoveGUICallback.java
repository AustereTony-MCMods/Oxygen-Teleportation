package austeretony.teleportation.client.gui.menu.locations;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.teleportation.client.gui.menu.LocationsGUISection;
import austeretony.teleportation.client.gui.menu.MenuGUIScreen;
import austeretony.teleportation.common.menu.locations.LocationsManagerClient;
import net.minecraft.client.resources.I18n;

public class LocationRemoveGUICallback extends AbstractGUICallback {

    private final MenuGUIScreen screen;

    private final LocationsGUISection section;

    private GUIButton confirmButton, cancelButton;

    public LocationRemoveGUICallback(MenuGUIScreen screen, LocationsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    protected void init() {
        this.addElement(new GUIImageLabel(- 2, - 2, this.getWidth() + 4, this.getHeight() + 4).enableStaticBackground(0xFF202020));//main background 1st layer
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), this.getHeight()).enableStaticBackground(0xFF101010));//main background 2nd layer
        this.addElement(new GUITextLabel(1, 1).setDisplayText(I18n.format("teleportation.menu.removeLocationCallback"), true));
        this.addElement(new GUITextLabel(1, 12).setDisplayText(I18n.format("teleportation.menu.removeCallback.request", this.section.currentPoint.getName()), true, 0.8F));     
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 61, this.getHeight() - 11, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.cancelButton"), true, 0.8F));
        this.addElement(this.confirmButton = new GUIButton(21, this.getHeight() - 11, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.confirmButton"), true, 0.8F));
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            LocationsManagerClient.instance().removeLocationPointSynced(this.section.currentPoint.getId());
            this.section.updatePoints();
            this.section.unlockCreateButton();
            this.close();
        }
    }
}
