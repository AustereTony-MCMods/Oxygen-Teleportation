package austeretony.teleportation.common.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.menu.camps.CampsLoaderClient;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.menu.locations.LocationsLoaderClient;

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
        CampsManagerClient.instance().getImageTransfers().put(pointId, new ImageTransferingClientBuffer(operation, pointId, partsAmount));
    }

    public static boolean exist(long pointId) {
        return CampsManagerClient.instance().getImageTransfers().containsKey(pointId);
    }

    public static ImageTransferingClientBuffer get(long pointId) {
        return CampsManagerClient.instance().getImageTransfers().get(pointId);
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
            CampsLoaderClient.saveCampPreviewImageDelegated(this.pointId, image);
        else
            LocationsLoaderClient.saveLocationPreviewImageDelegated(this.pointId, image);
        CampsManagerClient.instance().getPreviewImages().put(this.pointId, image);
        CampsManagerClient.instance().getImageTransfers().remove(this.pointId);
        TeleportationMain.LOGGER.info("Image {} saved.", this.pointId);
    }

    public enum EnumImageTransfer {

        DOWNLOAD_CAMP,
        DOWNLOAD_LOCATION,
    }
}
