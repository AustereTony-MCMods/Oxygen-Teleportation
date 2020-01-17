package austeretony.oxygen_teleportation.client.gui.teleportation.camps.callback;

import java.util.UUID;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenButton;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackBackgroundFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.client.gui.teleportation.CampsSection;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuScreen;
import austeretony.oxygen_teleportation.client.gui.teleportation.camps.InvitedPlayerPanelEntry;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;

public class InvitationsCallback extends AbstractGUICallback {

    private final TeleportationMenuScreen screen;

    protected final CampsSection section;

    private OxygenScrollablePanel invitedPanel;

    private OxygenButton closeButton;

    public InvitationsCallback(TeleportationMenuScreen screen, CampsSection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.enableDefaultBackground(EnumBaseGUISetting.FILL_CALLBACK_COLOR.get().asInt());
        this.addElement(new OxygenCallbackBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_teleportation.gui.menu.callback.invitations"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.invitedPanel = new OxygenScrollablePanel(this.screen, 6, 15, this.getWidth() - 12, 10, 1, TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.asInt(), 5, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));   

        this.addElement(this.closeButton = new OxygenButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen_core.gui.close")));
        this.closeButton.setKeyPressListener(Keyboard.KEY_X, ()->this.close());
    }

    private void updateInvited() {
        this.invitedPanel.reset();
        for (UUID playerUUID : TeleportationManagerClient.instance().getSharedCampsContainer().getInvitedPlayers(this.section.getCurrentPoint().getId()))
            this.invitedPanel.addEntry(new InvitedPlayerPanelEntry(OxygenHelperClient.getPlayerSharedData(playerUUID), this.section.getCurrentPoint().getId()));

        int 
        maxAmount = TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.asInt(),
        amount = TeleportationManagerClient.instance().getSharedCampsContainer().getInvitedPlayers(this.section.getCurrentPoint().getId()).size();

        this.invitedPanel.getScroller().reset();
        this.invitedPanel.getScroller().updateRowsAmount(MathUtils.clamp(amount, 5, maxAmount));
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
