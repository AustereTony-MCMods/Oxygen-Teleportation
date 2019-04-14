package austeretony.teleportation.client.gui.menu.camps;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.image.GUIImageLabel;
import austeretony.alternateui.screen.panel.GUIButtonPanel;
import austeretony.alternateui.screen.panel.GUIButtonPanel.GUIEnumOrientation;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.main.SharedPlayerData;
import austeretony.teleportation.client.TeleportationManagerClient;
import austeretony.teleportation.client.gui.menu.CampsGUISection;
import austeretony.teleportation.client.gui.menu.TeleportationMenuGUIScreen;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.client.resources.I18n;

public class InvitationGUICallback extends AbstractGUICallback {

    private final TeleportationMenuGUIScreen screen;

    private final CampsGUISection section;

    private GUITextField usernameField;

    private GUITextLabel playerStatusLabel;

    private GUIButtonPanel invitedPlayersPanel;

    private GUIButton confirmButton, cancelButton;

    private final Map<String, UUID> players = new HashMap<String, UUID>();

    private String 
    playerFoundStr = I18n.format("oxygen.gui.playerFound"),
    playerNotFoundStr = I18n.format("oxygen.gui.playerNotFound");

    public InvitationGUICallback(TeleportationMenuGUIScreen screen, CampsGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    protected void init() {
        for (SharedPlayerData sharedData : OxygenHelperClient.getSharedPlayersData())
            if (sharedData != OxygenHelperClient.getSharedClientPlayerData() && this.getPlayerStatus(sharedData) != OxygenPlayerData.EnumStatus.OFFLINE)
                this.players.put(sharedData.getUsername(), sharedData.getPlayerUUID());

        this.addElement(new GUIImageLabel(- 1, - 1, this.getWidth() + 2, this.getHeight() + 2).enableStaticBackground(GUISettings.instance().getBaseGUIBackgroundColor()));//main background 1st layer
        this.addElement(new GUIImageLabel(0, 0, this.getWidth(), 11).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUIImageLabel(0, 12, this.getWidth(), this.getHeight() - 12).enableStaticBackground(GUISettings.instance().getAdditionalGUIBackgroundColor()));//main background 2nd layer
        this.addElement(new GUITextLabel(2, 2).setDisplayText(I18n.format("teleportation.gui.menu.invitationsCallback"), true, GUISettings.instance().getTitleScale()));   
        this.addElement(new GUITextLabel(2, 16).setDisplayText(I18n.format("oxygen.gui.username"), false, GUISettings.instance().getSubTextScale()));  
        this.addElement(this.usernameField = new GUITextField(2, 25, 187, WorldPoint.MAX_POINT_NAME_LENGTH).setScale(0.7F).enableDynamicBackground().cancelDraggedElementLogic());       
        this.addElement(this.playerStatusLabel = new GUITextLabel(2, 34).setTextScale(GUISettings.instance().getSubTextScale()).disableFull());    

        this.addElement(new GUITextLabel(2, 48).setDisplayText(I18n.format("teleportation.gui.menu.invitationsCallback.list"), true, GUISettings.instance().getTextScale()));
        this.invitedPlayersPanel = new GUIButtonPanel(GUIEnumOrientation.VERTICAL, 2, 58, 100, 10).setButtonsOffset(1).setTextScale(GUISettings.instance().getTextScale());
        this.addElement(this.invitedPlayersPanel);
        GUIScroller panelScroller = new GUIScroller(TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.getIntValue(), 5);
        this.invitedPlayersPanel.initScroller(panelScroller);

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("oxygen.gui.confirmButton"), true, GUISettings.instance().getButtonTextScale()));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).enableDynamicBackground().setDisplayText(I18n.format("oxygen.gui.cancelButton"), true, GUISettings.instance().getButtonTextScale()));

        this.confirmButton.disable();
    }

    private OxygenPlayerData.EnumStatus getPlayerStatus(SharedPlayerData sharedData) {
        return OxygenPlayerData.EnumStatus.values()[sharedData.getData(OxygenMain.STATUS_DATA_ID).get(0)];
    }

    private void updatePlayers() {
        if (TeleportationManagerClient.instance().getPlayerData().haveInvitedPlayers(this.section.getCurrentPoint().getId())) {
            Set<UUID> invitedPlayers = TeleportationManagerClient.instance().getPlayerData().getInvitedPlayersByCampId(this.section.getCurrentPoint().getId()).getPlayers();
            if (invitedPlayers.size() >= TeleportationConfig.MAX_INVITED_PLAYERS_PER_CAMP.getIntValue()) 
                this.confirmButton.disable();
            if (!invitedPlayers.isEmpty()) {         
                this.invitedPlayersPanel.reset();
                InvitedPlayerGUIButton button;
                for (UUID playerUUID : invitedPlayers) {
                    button = new InvitedPlayerGUIButton(playerUUID, this.section.getCurrentPoint().getId());
                    button.enableDynamicBackground(GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getHoveredElementColor());
                    button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
                    button.setDisplayText(TeleportationManagerClient.instance().getPlayerData().getSharedCampsByPlayerUUID(playerUUID).username);
                    button.setTextAlignment(EnumGUIAlignment.LEFT, 2);
                    this.invitedPlayersPanel.addButton(button);
                }
            } else
                this.confirmButton.enable();
        }
    }

    @Override
    protected void onClose() {
        this.usernameField.reset();
        this.playerStatusLabel.disableFull();
        this.confirmButton.disable();
    }

    @Override
    protected void onOpen() {
        this.updatePlayers();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        boolean flag = super.keyTyped(typedChar, keyCode);   
        if (this.usernameField.isDragged()) {
            if (!this.usernameField.getTypedText().isEmpty()) {
                this.playerStatusLabel.enableFull();
                if (this.players.containsKey(this.usernameField.getTypedText())) {
                    this.playerStatusLabel.setDisplayText(this.playerFoundStr);
                    this.confirmButton.enable();
                } else {
                    this.playerStatusLabel.setDisplayText(this.playerNotFoundStr);
                    this.confirmButton.disable();
                }
            } else {
                this.playerStatusLabel.disableFull();
                this.confirmButton.disable();
            }
        }
        return flag;   
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {
        if (element == this.cancelButton)
            this.close();
        else if (element == this.confirmButton) {
            this.section.resetPointInfo();
            if (this.players.containsKey(this.usernameField.getTypedText())) 
                TeleportationManagerClient.instance().getCampsManager().invitePlayerSynced(this.section.getCurrentPoint().getId(), this.players.get(this.usernameField.getTypedText()), this.usernameField.getTypedText());
            this.section.sortPoints(0);
            this.close();
        }
    }
}
