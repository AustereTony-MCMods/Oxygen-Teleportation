package austeretony.teleportation.client.gui.menu.camps;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUICheckBoxButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import austeretony.teleportation.client.gui.menu.TeleportationMenuGUIScreen;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;

public class EditCampGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final CampsGUISection section;

    private GUITextField nameField, descriptionField;

    private GUICheckBoxButton updateImageButton, updatePositionButton;

    private GUITextLabel updateImageTextLabel, updatePositionTextLabel;

    private GUIButton confirmButton, cancelButton;

    public EditCampGUICallback(TeleportationMenuGUIScreen screen, CampsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new GUIImageLabel(- 1, - 1, this.getWidth() + 2, this.getHeight() + 2).enableStaticBackground(GUISettings.instance().getBaseGUIBackgroundColor()));//main background 1st layer
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), 11).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUIImageLabel(0, 12, this.getWidth(), this.getHeight() - 12).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUITextLabel(2, 2).setDisplayText(I18n.format("teleportation.gui.menu.editCampCallback"), true, GUISettings.instance().getTitleScale()));       
        this.addElement(new GUITextLabel(2, 16).setDisplayText(I18n.format("oxygen.gui.username"), false, GUISettings.instance().getSubTextScale()));    
        this.addElement(new GUITextLabel(2, 36).setDisplayText(I18n.format("oxygen.gui.note"), false, GUISettings.instance().getSubTextScale()));    

        this.addElement(this.nameField = new GUITextField(2, 25, 162, WorldPoint.MAX_POINT_NAME_LENGTH).setScale(0.7F).enableDynamicBackground().cancelDraggedElementLogic());
        this.addElement(this.descriptionField = new GUITextField(2, 45, 162, WorldPoint.MAX_POINT_DESCRIPTION_LENGTH).setScale(0.7F).enableDynamicBackground().cancelDraggedElementLogic());

        this.addElement(this.updateImageButton = new GUICheckBoxButton(2, 58, 10).enableDynamicBackground());    
        this.addElement(this.updatePositionButton = new GUICheckBoxButton(2, 70, 10).enableDynamicBackground());   
        this.addElement(this.updateImageTextLabel = new GUITextLabel(15, 59).setDisplayText(I18n.format("teleportation.gui.menu.updateImage"), false, GUISettings.instance().getSubTextScale())); 
        this.addElement(this.updatePositionTextLabel = new GUITextLabel(15, 71).setDisplayText(I18n.format("teleportation.gui.menu.updatePosition"), false, GUISettings.instance().getSubTextScale()));    

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("oxygen.gui.confirmButton"), true, GUISettings.instance().getButtonTextScale()));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("oxygen.gui.cancelButton"), true, GUISettings.instance().getButtonTextScale()));
    }

    @Override
    protected void onOpen() {
        this.nameField.setText(this.section.getCurrentPoint().getName());
        this.descriptionField.setText(this.section.getCurrentPoint().getDescription());
    }

    @Override
    protected void onClose() {
        this.nameField.reset();
        this.descriptionField.reset();
        this.updateImageButton.setToggled(false);
        this.updatePositionButton.setToggled(false);
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            TeleportationManagerClient.instance().getCampsManager().editCampPointSynced(this.section.getCurrentPoint(), this.nameField.getTypedText(), this.descriptionField.getTypedText(), 
                    this.updateImageButton.isToggled(), this.updatePositionButton.isToggled());
            this.section.sortPoints(0);
            this.close();
        }
    }
}
