package austeretony.oxygen_teleportation.common.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import austeretony.oxygen_core.common.util.BufferedImageUtils;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;

public class ImageTransferingClientBuffer {

    private final EnumImageTransfer operation;

    private final long pointId;

    private int fragmentsAmount;

    private final Map<Integer, byte[]> fragments = new HashMap<>();

    private ImageTransferingClientBuffer(EnumImageTransfer operation, long pointId, int fragmentsAmount) {
        this.pointId = pointId;
        this.operation = operation;
        this.fragmentsAmount = fragmentsAmount;
    }

    public static void create(EnumImageTransfer operation, long pointId, int fragmentsAmount) {
        if (!exist(pointId))
            TeleportationManagerClient.instance().getImagesManager().getImageTransfers().put(pointId, new ImageTransferingClientBuffer(operation, pointId, fragmentsAmount));
    }

    public static void processFragment(EnumImageTransfer operation, long pointId, int fragmentsAmount, int index, byte[] fragment) {
        if (exist(pointId))
            get(pointId).addPart(index, fragment);
        else {
            create(operation, pointId, fragmentsAmount);
            get(pointId).addPart(index, fragment);
        }
    }

    public static boolean exist(long pointId) {
        return TeleportationManagerClient.instance().getImagesManager().getImageTransfers().containsKey(pointId);
    }

    public static ImageTransferingClientBuffer get(long pointId) {
        return TeleportationManagerClient.instance().getImagesManager().getImageTransfers().get(pointId);
    }

    public void addPart(int index, byte[] fragment) {
        this.fragments.put(index, fragment);
        if (this.fragments.size() == this.fragmentsAmount)
            this.process();
    }

    private void process() {
        List<byte[]> ordered = new ArrayList<>();
        for (int i = 0; i < this.fragmentsAmount; i++)
            ordered.add(this.fragments.get(i));
        BufferedImage image = BufferedImageUtils.convertByteArraysListToBufferedImage(ordered, TeleportationConfig.IMAGE_WIDTH.getIntValue(), TeleportationConfig.IMAGE_HEIGHT.getIntValue());
        if (this.operation == EnumImageTransfer.DOWNLOAD_CAMP)
            TeleportationManagerClient.instance().getImagesLoader().saveCampPreviewImageAsync(this.pointId, image);
        else
            TeleportationManagerClient.instance().getImagesLoader().saveLocationPreviewImageAsync(this.pointId, image);
        TeleportationManagerClient.instance().getImagesManager().cacheImage(this.pointId, image);
        TeleportationManagerClient.instance().getImagesManager().getImageTransfers().remove(this.pointId);
        TeleportationMain.LOGGER.info("Image {}.png saved.", this.pointId);
    }

    public enum EnumImageTransfer {

        DOWNLOAD_CAMP,
        DOWNLOAD_LOCATION,
    }
}
