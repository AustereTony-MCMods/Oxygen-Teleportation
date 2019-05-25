package austeretony.oxygen_teleportation.common.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class ImageTransferingClientBuffer {

    private final EnumImageTransfer operation;

    private final long pointId;

    private int partsAmount;

    private final Map<Integer, byte[]> imageParts = new HashMap<Integer, byte[]>();

    private ImageTransferingClientBuffer(EnumImageTransfer operation, long pointId, int partsAmount) {
        this.pointId = pointId;
        this.operation = operation;
        this.partsAmount = partsAmount;
    }

    public static void create(EnumImageTransfer operation, long pointId, int partsAmount) {
        TeleportationManagerClient.instance().getImagesManager().getImageTransfers().put(pointId, new ImageTransferingClientBuffer(operation, pointId, partsAmount));
    }

    public static boolean exist(long pointId) {
        return TeleportationManagerClient.instance().getImagesManager().getImageTransfers().containsKey(pointId);
    }

    public static ImageTransferingClientBuffer get(long pointId) {
        return TeleportationManagerClient.instance().getImagesManager().getImageTransfers().get(pointId);
    }

    public void addPart(int index, byte[] imagePart) {
        this.imageParts.put(index, imagePart);
        if (this.imageParts.size() == this.partsAmount)
            this.process();
    }

    private void process() {
        List<byte[]> orderedParts = new ArrayList<byte[]>();
        for (int i = 0; i < this.partsAmount; i++)
            orderedParts.add(this.imageParts.get(i));
        BufferedImage image = BufferedImageUtils.convertByteArraysListToBufferedImage(orderedParts);
        if (this.operation == EnumImageTransfer.DOWNLOAD_CAMP)
            TeleportationManagerClient.instance().getImagesLoader().saveCampPreviewImageDelegated(this.pointId, image);
        else
            TeleportationManagerClient.instance().getImagesLoader().saveLocationPreviewImageDelegated(this.pointId, image);
        TeleportationManagerClient.instance().getImagesManager().cacheImage(this.pointId, image);
        TeleportationManagerClient.instance().getImagesManager().getImageTransfers().remove(this.pointId);
        TeleportationMain.LOGGER.info("Image {}.png saved.", this.pointId);
    }

    public enum EnumImageTransfer {

        DOWNLOAD_CAMP,
        DOWNLOAD_LOCATION,
    }
}
