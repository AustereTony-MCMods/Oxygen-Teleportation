package austeretony.oxygen_teleportation.common.util;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import austeretony.oxygen_core.common.util.BufferedImageUtils;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.server.TeleportationManagerServer;

public class ImageTransferingServerBuffer {

    private final EnumImageTransfer operation;

    private UUID playerUUID;

    private final long pointId;

    private int fragmentsAmount;

    private final Map<Integer, int[]> fragments = new HashMap<>();

    private ImageTransferingServerBuffer(EnumImageTransfer operation, UUID playerUUID, long pointId, int fragmentsAmount) {
        this.playerUUID = playerUUID;
        this.pointId = pointId;
        this.operation = operation;
        this.fragmentsAmount = fragmentsAmount;
    }

    public static boolean create(EnumImageTransfer operation, UUID playerUUID, long pointId, int fragmentsAmount) {
        if (!exist(pointId)
                && ((operation == EnumImageTransfer.UPLOAD_CAMP && TeleportationManagerServer.instance().getPlayersDataContainer().getPlayerData(playerUUID).campExist(pointId))
                        || (operation == EnumImageTransfer.UPLOAD_LOCATION && TeleportationManagerServer.instance().getLocationsContainer().locationExist(pointId)))) {
            TeleportationManagerServer.instance().getImagesManager().getImageTransfers().put(pointId, new ImageTransferingServerBuffer(operation, playerUUID, pointId, fragmentsAmount));
            return true;
        }
        return false;
    }

    public static void processFragment(EnumImageTransfer operation, UUID playerUUID, long pointId, int fragmentsAmount, int index, int[] fragment) {
        if (exist(pointId))
            get(pointId).addPart(index, fragment);
        else {
            if (create(operation, playerUUID, pointId, fragmentsAmount))
                get(pointId).addPart(index, fragment);
        }
    }

    public static boolean exist(long pointId) {
        return TeleportationManagerServer.instance().getImagesManager().getImageTransfers().containsKey(pointId);
    }

    public static ImageTransferingServerBuffer get(long pointId) {
        return TeleportationManagerServer.instance().getImagesManager().getImageTransfers().get(pointId);
    }

    public void addPart(int index, int[] fragment) {
        this.fragments.put(index, fragment);
        if (this.fragments.size() == this.fragmentsAmount)     
            this.process();
    }

    private void process() {
        List<int[]> ordered = new ArrayList<>();
        for (int i = 0; i < this.fragmentsAmount; i++)
            ordered.add(this.fragments.get(i));
        if (this.operation == EnumImageTransfer.UPLOAD_CAMP)
            TeleportationManagerServer.instance().getImagesLoader().saveCampPreviewImageAsync(this.playerUUID, this.pointId, convertIntArraysListToBufferedImage(ordered, TeleportationConfig.IMAGE_WIDTH.getIntValue(), TeleportationConfig.IMAGE_HEIGHT.getIntValue()));
        else
            TeleportationManagerServer.instance().getImagesLoader().saveAndLoadBytesLocationPreviewAsync(this.pointId, convertIntArraysListToBufferedImage(ordered, TeleportationConfig.IMAGE_WIDTH.getIntValue(), TeleportationConfig.IMAGE_HEIGHT.getIntValue()));
        TeleportationManagerServer.instance().getImagesManager().getImageTransfers().remove(this.pointId);
        TeleportationMain.LOGGER.info("Image {}.png saved.", this.pointId);
    }

    public static BufferedImage convertIntArraysListToBufferedImage(List<int[]> imageIntParts, int imageWidth, int imageHeight) {
        int[] imageArray = BufferedImageUtils.mergeIntArrays(imageIntParts);               
        BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        bufferedImage.setData(Raster.createRaster(bufferedImage.getSampleModel(), new DataBufferInt(imageArray, imageArray.length), new Point()));
        return bufferedImage;
    }

    public enum EnumImageTransfer {

        UPLOAD_CAMP,
        UPLOAD_LOCATION,
    }
}
