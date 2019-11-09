package austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback;

import java.util.UUID;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackGUIFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButtonPanel;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsGUISection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.InvitedPlayerGUIButton;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;

public class InvitationsGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    protected final CampsGUISection section;

    private OxygenGUIButtonPanel invitedPanel;

    private OxygenGUIButton closeButton;

    public InvitationsGUICallback(TeleportationMenuGUIScreen screen, CampsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new OxygenCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_teleportation.gui.menu.callback.invitations"), GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.invitedPanel = new OxygenGUIButtonPanel(this.screen, 6, 15, this.getWidth() - 12, 10, 1, 5, 5, GUISettings.get().getPanelTextScale(), false));   

        this.addElement(this.closeButton = new OxygenGUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen_teleportation.gui.closeButton")));
    }

    private void updateInvited() {
        this.invitedPanel.reset();
        for (UUID playerUUID : TeleportationManagerClient.instance().getSharedCampsContainer().getInvitedPlayers(this.section.getCurrentPoint().getId()))
            this.invitedPanel.addButton(new InvitedPlayerGUIButton(OxygenHelperClient.getPlayerSharedData(playerUUID), this.section.getCurrentPoint().getId()));

        int 
        maxAmount = TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.getIntValue(),
        amount = TeleportationManagerClient.instance().getSharedCampsContainer().getInvitedPlayers(this.section.getCurrentPoint().getId()).size();
        this.invitedPanel.getScroller().updateRowsAmount(MathUtils.clamp(amount, 5, maxAmount));
        this.invitedPanel.getScroller().resetPosition();
    }

    @Override
    protected void onOpen() {
        this.updateInvited();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.closeButton)
                this.close();
        }
    }

    public void playerUninvited(long pointId, UUID playerUUID) {
        this.updateInvited();
    }
}
