package austeretony.oxygen_teleportation.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.BufferedImageUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;

public class ImageTransferingServerBuffer {

    private final EnumImageTransfer operation;

    private UUID playerUUID;

    private final long pointId;

    private int fragmentsAmount;

    private final Map<Integer, byte[]> fragments = new HashMap<>();

    private ImageTransferingServerBuffer(EnumImageTransfer operation, UUID playerUUID, long pointId, int fragmentsAmount) {
        this.playerUUID = playerUUID;
        this.pointId = pointId;
        this.operation = operation;
        this.fragmentsAmount = fragmentsAmount;
    }

    public static boolean create(EnumImageTransfer operation, UUID playerUUID, long pointId, int fragmentsAmount) {
        if (!exist(pointId)
                && ((operation == EnumImageTransfer.UPLOAD_CAMP && TeleportationManagerServer.instance().getPlayersDataContainer().getPlayerData(playerUUID).isCampExist(pointId))
                        || (operation == EnumImageTransfer.UPLOAD_LOCATION && TeleportationManagerServer.instance().getLocationsContainer().locationExist(pointId)))) {
            TeleportationManagerServer.instance().getImagesManager().getImageTransfers().put(pointId, new ImageTransferingServerBuffer(operation, playerUUID, pointId, fragmentsAmount));
            return true;
        }
        return false;
    }

    public static void processFragment(EnumImageTransfer operation, UUID playerUUID, long pointId, int fragmentsAmount, int index, byte[] fragment) {
        if (exist(pointId))
            get(pointId).addPart(index, fragment);
        else {
            if (OxygenHelperServer.isNetworkRequestAvailable(playerUUID, TeleportationMain.IMAGE_UPLOAD_REQUEST_ID)//TODO 0.10.2 added packet hacks protection
                    && create(operation, playerUUID, pointId, fragmentsAmount))
                get(pointId).addPart(index, fragment);
        }
    }

    public static boolean exist(long pointId) {
        return TeleportationManagerServer.instance().getImagesManager().getImageTransfers().containsKey(pointId);
    }

    public static ImageTransferingServerBuffer get(long pointId) {
        return TeleportationManagerServer.instance().getImagesManager().getImageTransfers().get(pointId);
    }

    public void addPart(int index, byte[] fragment) {
        if (this.fragments.size() > 100 || this.fragments.containsKey(index))//TODO 0.10.2 added packet hacks protection
            TeleportationManagerServer.instance().getImagesManager().getImageTransfers().remove(this.pointId);
        else {
            this.fragments.put(index, fragment);
            if (this.fragments.size() == this.fragmentsAmount)     
                this.process();
        }
    }

    private void process() {
        List<byte[]> ordered = new ArrayList<>();
        for (int i = 0; i < this.fragmentsAmount; i++)
            ordered.add(this.fragments.get(i));

        if (this.operation == EnumImageTransfer.UPLOAD_CAMP) {
            byte[] merged = this.mergeArrays(ordered);
            if (merged != null)
                TeleportationManagerServer.instance().getImagesLoader().saveCampPreviewImageAsync(this.playerUUID, this.pointId, merged);
            OxygenMain.LOGGER.info("[Teleportation] Camp preview image {}.png uploaded by player: {}", this.pointId, this.playerUUID);
        } else {
            byte[] merged = this.mergeArrays(ordered);
            if (merged != null)
                TeleportationManagerServer.instance().getImagesLoader().saveAndLoadBytesLocationPreviewAsync(this.pointId, merged);
            OxygenMain.LOGGER.info("[Teleportation] Location preview image {}.png uploaded by player: {}", this.pointId, this.playerUUID);
        }
        TeleportationManagerServer.instance().getImagesManager().getImageTransfers().remove(this.pointId);
    }

    @Nullable
    private byte[] mergeArrays(List<byte[]> ordered) {
        byte[] merged = null;
        try {
            merged = BufferedImageUtils.mergeByteArrays(ordered);
        } catch (IOException exception) {
            OxygenMain.LOGGER.error("[Teleportation] Failed to merge raw image fragments {}.png uploaded by player: {}", this.pointId, this.playerUUID);
            exception.printStackTrace();
        }
        return merged;
    }

    public enum EnumImageTransfer {

        UPLOAD_CAMP,
        UPLOAD_LOCATION,
    }
}
