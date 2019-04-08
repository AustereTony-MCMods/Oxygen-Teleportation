package austeretony.teleportation.client.gui.menu.players;

import austeretony.alternateui.screen.list.GUIDropDownElement;
import austeretony.teleportation.common.main.PlayerProfile;

public class ProfileGUIDropDownElement extends GUIDropDownElement {

    public final PlayerProfile.EnumJumpProfile profile;

    public ProfileGUIDropDownElement(PlayerProfile.EnumJumpProfile profile) {
        super();
        this.profile = profile;
    }
}
