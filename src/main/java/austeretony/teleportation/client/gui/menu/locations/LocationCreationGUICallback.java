package austeretony.teleportation.client.gui.menu.locations;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.LocationsGUISection;
import austeretony.teleportation.client.gui.menu.MenuGUIScreen;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;

public class LocationCreationGUICallback extends AbstractGUICallback {

    private final MenuGUIScreen screen;

    private final LocationsGUISection section;

    private GUITextField nameField, descriptionField;

    private GUIButton confirmButton, cancelButton;

    public LocationCreationGUICallback(MenuGUIScreen screen, LocationsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    protected void init() {
        this.addElement(new GUIImageLabel(- 1, - 1, this.getWidth() + 2, this.getHeight() + 2).enableStaticBackground(GUISettings.instance().getBaseGUIBackgroundColor()));//main background 1st layer
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), 11).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUIImageLabel(0, 12, this.getWidth(), this.getHeight() - 12).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUITextLabel(2, 2).setDisplayText(I18n.format("teleportation.menu.locationCreationCallback"), true));   
        this.addElement(new GUITextLabel(2, 17).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.name")));    
        this.addElement(new GUITextLabel(2, 37).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.desc")));    

        this.addElement(this.nameField = new GUITextField(2, 25, 162, 16).setScale(0.8F).enableDynamicBackground());
        this.addElement(this.descriptionField = new GUITextField(2, 45, 162, 64).setScale(0.8F).enableDynamicBackground());

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.confirmButton"), true, 0.8F));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.cancelButton"), true, 0.8F));
    }

    @Override
    protected void onClose() {
        this.nameField.reset();
        this.descriptionField.reset();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            String 
            name = this.nameField.getTypedText().isEmpty() 
            ? I18n.format("teleportation.menu.locationGenericName") + " #" + String.valueOf(TeleportationManagerClient.instance().getWorldProfile().getLocationsAmount() + 1) 
            : this.nameField.getTypedText(),
            description = this.descriptionField.getTypedText();
            WorldPoint worldPoint = new WorldPoint(
                    OxygenHelperClient.getPlayerUUID(),
                    this.mc.player.getName(), 
                    name, 
                    description,
                    this.mc.player.dimension,
                    (float) this.mc.player.posX, 
                    (float) this.mc.player.posY, 
                    (float) this.mc.player.posZ,
                    this.mc.player.rotationYawHead, 
                    this.mc.player.rotationPitch);
            worldPoint.createId();
            TeleportationManagerClient.instance().getLocationsManager().setLocationPointSynced(worldPoint);
            this.section.updatePoints();
            this.section.lockCreateButton();     
            this.close();
        }
    }
}
