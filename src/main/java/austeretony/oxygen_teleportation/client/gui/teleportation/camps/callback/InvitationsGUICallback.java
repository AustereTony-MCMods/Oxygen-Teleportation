package austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback;

import java.util.UUID;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.panel.GUIButtonPanel;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.alternateui.util.EnumGUIOrientation;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.util.MathUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsGUISection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.InvitedPlayerGUIButton;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;

public class InvitationsGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final CampsGUISection section;

    private GUIButtonPanel invitedPlayersPanel;

    private GUIButton cancelButton;

    public InvitationsGUICallback(TeleportationMenuGUIScreen screen, CampsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new InvitationsCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 2).setDisplayText(ClientReference.localize("teleportation.gui.menu.invitationsCallback"), true, GUISettings.instance().getTitleScale()));               

        this.invitedPlayersPanel = new GUIButtonPanel(EnumGUIOrientation.VERTICAL, 0, 12, 137, 10).setButtonsOffset(1).setTextScale(GUISettings.instance().getPanelTextScale());
        this.addElement(this.invitedPlayersPanel);
        GUIScroller scroller = new GUIScroller(MathUtils.clamp(TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.getIntValue(), 5, 100), 5);
        this.invitedPlayersPanel.initScroller(scroller);
        GUISlider slider = new GUISlider(this.getX() + 138, this.getY() + 12, 2, 54);
        slider.setDynamicBackgroundColor(GUISettings.instance().getEnabledSliderColor(), GUISettings.instance().getDisabledSliderColor(), GUISettings.instance().getHoveredSliderColor());
        scroller.initSlider(slider);

        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(ClientReference.localize("teleportation.gui.closeButton"), true, GUISettings.instance().getButtonTextScale()));
    }

    public void updatePlayers() {
        this.invitedPlayersPanel.reset();
        InvitedPlayerGUIButton button;
        for (UUID playerUUID : TeleportationManagerClient.instance().getSharedCampsManager().getInvitedPlayers(this.section.getCurrentPoint().getId())) {
            button = new InvitedPlayerGUIButton(playerUUID, this.section.getCurrentPoint().getId());
            button.enableDynamicBackground(GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getHoveredElementColor());
            button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            button.setDisplayText(OxygenHelperClient.getObservedSharedData(playerUUID).getUsername());
            button.setTextAlignment(EnumGUIAlignment.LEFT, 2);
            this.invitedPlayersPanel.addButton(button);
        }
    }

    @Override
    protected void onOpen() {
        this.updatePlayers();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (element == this.cancelButton)
            this.close();
    }
}
