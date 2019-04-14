package austeretony.teleportation.client.gui.menu.players;

import austeretony.alternateui.screen.list.GUIDropDownElement;
import austeretony.teleportation.common.main.TeleportationPlayerData;

public class JumpProfileGUIDropDownElement extends GUIDropDownElement {

    public final TeleportationPlayerData.EnumJumpProfile profile;

    public JumpProfileGUIDropDownElement(TeleportationPlayerData.EnumJumpProfile profile) {
        super();
        this.profile = profile;
    }
}
