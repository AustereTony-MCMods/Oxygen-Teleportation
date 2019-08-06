package austeretony.oxygen_teleportation.client;

import java.util.Set;

import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.sync.gui.api.IComplexGUIHandlerClient;
import austeretony.oxygen.common.api.network.OxygenNetwork;
import austeretony.oxygen_teleportation.client.gui.teleportation.TeleportationMenuGUIScreen;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.WorldPoint;
import net.minecraft.network.PacketBuffer;

public class TeleportationMenuHandlerClient implements IComplexGUIHandlerClient<WorldPoint, WorldPoint> {

    @Override
    public void open() {
        TeleportationManagerClient.instance().getImagesManager().preparePreviewImage();
        ClientReference.getGameSettings().hideGUI = false;
        ClientReference.displayGuiScreen(new TeleportationMenuGUIScreen());
    }

    @Override
    public OxygenNetwork getNetwork() {
        return TeleportationMain.network();
    }

    @Override
    public Set<Long> getIdentifiersFirst() {
        return TeleportationManagerClient.instance().getPlayerData().getCampIds();
    }

    @Override
    public Set<Long> getIdentifiersSecond() {
        return TeleportationManagerClient.instance().getWorldData().getLocationIds();
    }

    @Override
    public WorldPoint getEntryFirst(long entryId) {
        return TeleportationManagerClient.instance().getPlayerData().getCamp(entryId);
    }

    @Override
    public WorldPoint getEntrySecond(long entryId) {
        return TeleportationManagerClient.instance().getWorldData().getLocation(entryId);
    }

    @Override
    public void clearDataFirst() {
        TeleportationManagerClient.instance().getPlayerData().clearCamps();
    }

    @Override
    public void clearDataSecond() {
        TeleportationManagerClient.instance().getWorldData().reset();
    }

    @Override
    public void addValidEntryFirst(WorldPoint entry) {
        TeleportationManagerClient.instance().getPlayerData().addCamp(entry);
    }

    @Override
    public void addValidEntrySecond(WorldPoint entry) {
        TeleportationManagerClient.instance().getWorldData().addLocation(entry);
    }

    @Override
    public void readEntries(PacketBuffer buffer, int firstAmount, int secondAmount) {
        int i = 0;
        for (; i < firstAmount; i++)
            TeleportationManagerClient.instance().getPlayerData().addCamp(WorldPoint.read(buffer));
        for (i = 0; i < secondAmount; i++)
            TeleportationManagerClient.instance().getWorldData().addLocation(WorldPoint.read(buffer));
    }
}
