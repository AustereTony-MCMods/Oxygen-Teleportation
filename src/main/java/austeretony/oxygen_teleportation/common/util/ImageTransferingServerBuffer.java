package austeretony.oxygen_teleportation.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class ImageTransferingServerBuffer {

    private final EnumImageTransfer operation;

    private UUID playerUUID;

    private final long pointId;

    private int partsAmount;

    private final Map<Integer, int[]> imageParts = new HashMap<Integer, int[]>();

    private ImageTransferingServerBuffer(EnumImageTransfer operation, UUID playerUUID, long pointId, int partsAmount) {
        this.playerUUID = playerUUID;
        this.pointId = pointId;
        this.operation = operation;
        this.partsAmount = partsAmount;
    }

    public static void create(EnumImageTransfer operation, UUID playerUUID, long pointId, int partsAmount) {
        TeleportationManagerServer.instance().getImagesManager().getImageTransfers().put(pointId, new ImageTransferingServerBuffer(operation, playerUUID, pointId, partsAmount));
    }

    public static boolean exist(long pointId) {
        return TeleportationManagerServer.instance().getImagesManager().getImageTransfers().containsKey(pointId);
    }

    public static ImageTransferingServerBuffer get(long pointId) {
        return TeleportationManagerServer.instance().getImagesManager().getImageTransfers().get(pointId);
    }

    public void addPart(int index, int[] imagePart) {
        this.imageParts.put(index, imagePart);
        if (this.imageParts.size() == this.partsAmount)     
            this.process();
    }

    private void process() {
        List<int[]> orderedParts = new ArrayList<int[]>();
        for (int i = 0; i < this.partsAmount; i++)
            orderedParts.add(this.imageParts.get(i));
        if (this.operation == EnumImageTransfer.UPLOAD_CAMP)
            TeleportationManagerServer.instance().getImagesLoader().saveCampPreviewImageDelegated(this.playerUUID, this.pointId, BufferedImageUtils.convertIntArraysListToBufferedImage(orderedParts));
        else
            TeleportationManagerServer.instance().getImagesLoader().saveAndLoadBytesLocationPreviewDelegated(this.pointId, BufferedImageUtils.convertIntArraysListToBufferedImage(orderedParts));
        TeleportationManagerServer.instance().getImagesManager().getImageTransfers().remove(this.pointId);
        TeleportationMain.LOGGER.info("Image {}.png saved.", this.pointId);
    }

    public enum EnumImageTransfer {

        UPLOAD_CAMP,
        UPLOAD_LOCATION,
    }
}