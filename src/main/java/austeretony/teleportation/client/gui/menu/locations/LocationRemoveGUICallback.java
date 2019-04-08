package austeretony.teleportation.client.gui.menu.locations;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.LocationsGUISection;
import austeretony.teleportation.client.gui.menu.MenuGUIScreen;
import net.minecraft.client.resources.I18n;

public class LocationRemoveGUICallback extends AbstractGUICallback {

    private final MenuGUIScreen screen;

    private final LocationsGUISection section;

    private GUITextLabel requestLabel;

    private GUIButton confirmButton, cancelButton;

    public LocationRemoveGUICallback(MenuGUIScreen screen, LocationsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    protected void init() {
        this.addElement(new GUIImageLabel(- 1, - 1, this.getWidth() + 2, this.getHeight() + 2).enableStaticBackground(GUISettings.instance().getBaseGUIBackgroundColor()));//main background 1st layer
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), 11).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUIImageLabel(0, 12, this.getWidth(), this.getHeight() - 12).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUITextLabel(2, 2).setDisplayText(I18n.format("teleportation.menu.removeLocationCallback"), true));
        this.addElement(this.requestLabel = new GUITextLabel(2, 16));     

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.confirmButton"), true, 0.8F));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.cancelButton"), true, 0.8F));
    }

    @Override
    protected void onOpen() {
        this.requestLabel.setDisplayText(I18n.format("teleportation.menu.removeCallback.request", this.section.currentPoint.getName()), true, 0.8F);
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            TeleportationManagerClient.instance().getLocationsManager().removeLocationPointSynced(this.section.currentPoint.getId());
            this.section.updatePoints();
            this.section.unlockCreateButton();
            this.close();
        }
    }
}
