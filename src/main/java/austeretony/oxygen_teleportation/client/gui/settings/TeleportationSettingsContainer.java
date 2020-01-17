package austeretony.oxygen_teleportation.client.gui.settings;

import austeretony.alternateui.screen.framework.GUIElementsFramework;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.elements.OxygenCheckBoxButton;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.settings.ElementsContainer;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetColorCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetKeyCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetOffsetCallback;
import austeretony.oxygen_core.client.gui.settings.gui.callback.SetScaleCallback;
import austeretony.oxygen_teleportation.client.settings.EnumTeleportationClientSetting;
import austeretony.oxygen_teleportation.client.settings.gui.EnumTeleportationGUISetting;

public class TeleportationSettingsContainer implements ElementsContainer {

    //common

    private OxygenCheckBoxButton addTeleportationMenuButton;

    //interface

    private OxygenDropDownList alignmentTeleportationMenu;


    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_teleportation.gui.settings.module.teleportation");
    }

    @Override
    public boolean hasCommonSettings() {
        return true;
    }

    @Override
    public boolean hasGUISettings() {
        return true;
    }

    @Override
    public void addCommon(GUIElementsFramework framework) {
        framework.addElement(new OxygenTextLabel(68, 25, ClientReference.localize("oxygen_core.gui.settings.option.oxygenMenu"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //add teleportation menu to menu
        framework.addElement(new OxygenTextLabel(78, 34, ClientReference.localize("oxygen_teleportation.gui.settings.option.addTeleportationMenu"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.1F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        framework.addElement(this.addTeleportationMenuButton = new OxygenCheckBoxButton(68, 29));
        this.addTeleportationMenuButton.setToggled(EnumTeleportationClientSetting.ADD_TELEPORTATION_MENU.get().asBoolean());
        this.addTeleportationMenuButton.setClickListener((mouseX, mouseY, mouseButton)->{
            EnumTeleportationClientSetting.ADD_TELEPORTATION_MENU.get().setValue(String.valueOf(this.addTeleportationMenuButton.isToggled()));
            OxygenManagerClient.instance().getClientSettingManager().changed();
        });
    }

    @Override
    public void addGUI(GUIElementsFramework framework) {
        framework.addElement(new OxygenTextLabel(68, 25, ClientReference.localize("oxygen_core.gui.settings.option.alignment"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        //teleportation menu alignment

        String currAlignmentStr;
        switch (EnumTeleportationGUISetting.TELEPORTATION_MENU_ALIGNMENT.get().asInt()) {
        case - 1: 
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.left");
            break;
        case 0:
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.center");
            break;
        case 1:
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.right");
            break;    
        default:
            currAlignmentStr = ClientReference.localize("oxygen_core.alignment.center");
            break;
        }
        framework.addElement(this.alignmentTeleportationMenu = new OxygenDropDownList(68, 35, 55, currAlignmentStr));
        this.alignmentTeleportationMenu.addElement(new OxygenDropDownListEntry<Integer>(- 1, ClientReference.localize("oxygen_core.alignment.left")));
        this.alignmentTeleportationMenu.addElement(new OxygenDropDownListEntry<Integer>(0, ClientReference.localize("oxygen_core.alignment.center")));
        this.alignmentTeleportationMenu.addElement(new OxygenDropDownListEntry<Integer>(1, ClientReference.localize("oxygen_core.alignment.right")));

        this.alignmentTeleportationMenu.<OxygenDropDownListEntry<Integer>>setClickListener((element)->{
            EnumTeleportationGUISetting.TELEPORTATION_MENU_ALIGNMENT.get().setValue(String.valueOf(element.index));
            OxygenManagerClient.instance().getClientSettingManager().changed();
        });

        framework.addElement(new OxygenTextLabel(68, 33, ClientReference.localize("oxygen_teleportation.gui.settings.option.alignmentTeleportationMenu"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.1F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
    }

    @Override
    public void resetCommon() {
        //add teleportation menu to menu
        this.addTeleportationMenuButton.setToggled(true);
        EnumTeleportationClientSetting.ADD_TELEPORTATION_MENU.get().reset();    

        OxygenManagerClient.instance().getClientSettingManager().changed();
    }

    @Override
    public void resetGUI() {
        //teleportation menu alignment
        this.alignmentTeleportationMenu.setDisplayText(ClientReference.localize("oxygen_core.alignment.center"));
        EnumTeleportationGUISetting.TELEPORTATION_MENU_ALIGNMENT.get().reset();

        OxygenManagerClient.instance().getClientSettingManager().changed();
    }

    @Override
    public void initSetColorCallback(SetColorCallback callback) {}

    @Override
    public void initSetScaleCallback(SetScaleCallback callback) {}

    @Override
    public void initSetOffsetCallback(SetOffsetCallback callback) {}

    @Override
    public void initSetKeyCallback(SetKeyCallback callback) {}
}
