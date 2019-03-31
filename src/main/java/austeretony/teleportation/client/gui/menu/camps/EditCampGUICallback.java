package austeretony.teleportation.client.gui.menu.camps;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUICheckBoxButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import austeretony.teleportation.client.gui.menu.MenuGUIScreen;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import net.minecraft.client.resources.I18n;

public class EditCampGUICallback extends AbstractGUICallback {

    private final MenuGUIScreen screen;

    private final CampsGUISection section;

    private GUITextField nameField, descriptionField;

    private GUICheckBoxButton updateImageButton, updatePositionButton;

    private GUITextLabel updateImageTextLabel, updatePositionTextLabel;

    private GUIButton confirmButton, cancelButton;

    public EditCampGUICallback(MenuGUIScreen screen, CampsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    protected void init() {
        this.addElement(new GUIImageLabel(- 1, - 1, this.getWidth() + 2, this.getHeight() + 2).enableStaticBackground(0xFF202020));//main background 1st layer
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), 11).enableStaticBackground(0xFF101010));//main background 2nd layer
        this.addElement(new GUIImageLabel(0, 12, this.getWidth(), this.getHeight() - 12).enableStaticBackground(0xFF101010));//main background 2nd layer
        this.addElement(new GUITextLabel(2, 2).setDisplayText(I18n.format("teleportation.menu.editCampCallback"), true));       
        this.addElement(new GUITextLabel(2, 14).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.name")));    
        this.addElement(new GUITextLabel(2, 34).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.desc")));    

        this.addElement(this.updateImageButton = new GUICheckBoxButton(2, 54, 10).enableDynamicBackground());    
        this.addElement(this.updatePositionButton = new GUICheckBoxButton(2, 66, 10).enableDynamicBackground());   
        this.addElement(this.updateImageTextLabel = new GUITextLabel(15, 56).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.updateImage"))); 
        this.addElement(this.updatePositionTextLabel = new GUITextLabel(15, 68).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.updatePosition")));    

        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 61, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.cancelButton"), true, 0.8F));
        this.addElement(this.confirmButton = new GUIButton(21, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.confirmButton"), true, 0.8F));
        this.addElement(this.nameField = new GUITextField(2, 21, 162, 16).setScale(0.8F).enableDynamicBackground());
        this.addElement(this.descriptionField = new GUITextField(2, 41, 162, 64).setScale(0.8F).enableDynamicBackground());
    }

    @Override
    protected void onOpen() {
        this.nameField.setText(this.section.currentPoint.getName());
        this.descriptionField.setText(this.section.currentPoint.getDescription());
    }

    @Override
    protected void onClose() {
        this.nameField.reset();
        this.descriptionField.reset();
        this.updateImageButton.setToggled(false);
        this.updatePositionButton.setToggled(false);
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            CampsManagerClient.instance().editCampPointSynced(this.section.currentPoint, this.nameField.getTypedText(), this.descriptionField.getTypedText(), 
                    this.updateImageButton.isToggled(), this.updatePositionButton.isToggled());
            this.section.updatePoints();
            this.close();
        }
    }
}
