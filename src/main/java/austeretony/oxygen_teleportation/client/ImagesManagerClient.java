package austeretony.oxygen_teleportation.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.util.ScreenshotHelper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.network.server.SPStartImageUpload;
import austeretony.oxygen_teleportation.common.network.server.SPUploadImagePart;
import austeretony.oxygen_teleportation.common.util.ImageTransferingServerBuffer;

public class ImagesManagerClient {

    private final TeleportationManagerClient manager;

    private final Map<Long, BufferedImage> images = new ConcurrentHashMap<>();

    private BufferedImage latestImage;

    protected ImagesManagerClient(TeleportationManagerClient manager) {
        this.manager = manager;
    }

    public void preparePreviewImage() {
        this.latestImage = ScreenshotHelper.createScreenshot(TeleportationConfig.IMAGE_WIDTH.asInt(), TeleportationConfig.IMAGE_HEIGHT.asInt());
    }

    @Nullable
    public BufferedImage getLatestImage() {
        return this.latestImage;
    }

    public Map<Long, BufferedImage> getPreviewImages() {
        return this.images;
    }

    public void cacheImage(long pointId, BufferedImage image) {
        this.images.put(pointId, image);
    }

    public void cacheLatestImage(long pointId) {
        this.images.put(pointId, this.getLatestImage());
    }

    public void removeCachedImage(long pointId) {
        this.images.remove(pointId);
    }

    public void replaceCachedImage(long oldPointId, long newPointId) {
        if (this.images.containsKey(oldPointId)) {
            this.images.put(newPointId, this.images.get(oldPointId));
            this.images.remove(oldPointId);
        }
    }

    public void uploadCampPreviewToServerAsync(long pointId) {
        OxygenHelperClient.addIOTask(()->this.uploadCampPreviewToServer(pointId));
    }

    public void uploadCampPreviewToServer(long pointId) {
        byte[] imageRaw = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(this.getLatestImage(), "png", baos);
            imageRaw = baos.toByteArray();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        if (imageRaw != null) {
            List<byte[]> fragments = divideArray(imageRaw, Short.MAX_VALUE / 2);
            OxygenMain.network().sendToServer(new SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_CAMP, pointId, fragments.size()));  

            int index = 0;
            for (byte[] part : fragments) {
                OxygenMain.network().sendToServer(new SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_CAMP, pointId, index, part, fragments.size()));
                index++;
            }
        }
    }

    public static List<byte[]> divideArray(byte[] array, int size) {
        List<byte[]> result = new ArrayList<byte[]>();
        int 
        start = 0,
        end;
        while (start < array.length) {
            end = Math.min(array.length, start + size);
            result.add(Arrays.copyOfRange(array, start, end));
            start += size;
        }
        return result;
    }

    public void uploadLocationPreviewToServerAsync(long pointId) {
        OxygenHelperClient.addIOTask(()->this.uploadLocationPreviewToServer(pointId));
    }

    public void uploadLocationPreviewToServer(long pointId) {
        byte[] imageRaw = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(this.getLatestImage(), "png", baos);
            imageRaw = baos.toByteArray();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        if (imageRaw != null) {
            List<byte[]> fragments = divideArray(imageRaw, Short.MAX_VALUE / 2);
            OxygenMain.network().sendToServer(new SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_LOCATION, pointId, fragments.size()));  

            int index = 0; 
            for (byte[] part : fragments) {
                OxygenMain.network().sendToServer(new SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_LOCATION, pointId, index, part, fragments.size()));
                index++;
            }
        }
    }

    public void processDownloadedPreviewImage(EnumWorldPoint type, long pointId, byte[] imageRaw) {
        ByteArrayInputStream baos = new ByteArrayInputStream(imageRaw);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(baos);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        if (bufferedImage != null) {
            if (type == EnumWorldPoint.CAMP)
                TeleportationManagerClient.instance().getImagesLoader().saveCampPreviewImageAsync(pointId, bufferedImage);
            else
                TeleportationManagerClient.instance().getImagesLoader().saveLocationPreviewImageAsync(pointId, bufferedImage);

            TeleportationManagerClient.instance().getImagesManager().cacheImage(pointId, bufferedImage);

            OxygenMain.LOGGER.info("[Teleportation] Image {}.png saved.", pointId);
        } else
            OxygenMain.LOGGER.info("[Teleportation] Failed to create and save image {}.png.", pointId);
    }
}