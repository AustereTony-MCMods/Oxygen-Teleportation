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

    private GUITextField nameField, descField;

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
        this.addElement(new GUIImageLabel(- 2, - 2, this.getWidth() + 4, this.getHeight() + 4).enableStaticBackground(0xFF202020));//main background 1st layer
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), this.getHeight()).enableStaticBackground(0xFF101010));//main background 2nd layer
        this.addElement(new GUITextLabel(1, 1).setDisplayText(I18n.format("teleportation.menu.editCampCallback"), true));       
        this.addElement(new GUITextLabel(1, 12).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.name")));    
        this.addElement(new GUITextLabel(1, 32).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.desc")));    

        this.addElement(this.updateImageButton = new GUICheckBoxButton(2, 52, 10).enableDynamicBackground());    
        this.addElement(this.updatePositionButton = new GUICheckBoxButton(2, 64, 10).enableDynamicBackground());   
        this.addElement(this.updateImageTextLabel = new GUITextLabel(15, 54).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.updateImage"))); 
        this.addElement(this.updatePositionTextLabel = new GUITextLabel(15, 66).setScale(0.7F).setDisplayText(I18n.format("teleportation.menu.spec.updatePosition")));    

        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 61, this.getHeight() - 11, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.cancelButton"), true, 0.8F));
        this.addElement(this.confirmButton = new GUIButton(21, this.getHeight() - 11, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("teleportation.menu.confirmButton"), true, 0.8F));
        this.addElement(this.nameField = new GUITextField(1, 19, 165, 16).setScale(0.8F).enableDynamicBackground());
        this.addElement(this.descField = new GUITextField(1, 39, 165, 64).setScale(0.8F).enableDynamicBackground());
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            String 
            prevName,
            name = this.nameField.getTypedText().isEmpty() ? this.section.currentPoint.getName() : this.nameField.getTypedText(),
                    desc = this.descField.getTypedText().isEmpty() ? this.section.currentPoint.getDescription() : this.descField.getTypedText();
                    CampsManagerClient.instance().editCampPointSynced(this.section.currentPoint, name, desc, this.updateImageButton.isToggled(), this.updatePositionButton.isToggled());
                    this.section.updatePoints();
                    this.close();
        }
    }
}
