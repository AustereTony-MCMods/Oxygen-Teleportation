package austeretony.oxygen_teleportation.client;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.BufferedImageUtils;
import austeretony.oxygen_teleportation.client.util.ScreenshotHelper;
import austeretony.oxygen_teleportation.common.network.server.SPStartImageUpload;
import austeretony.oxygen_teleportation.common.network.server.SPUploadImagePart;
import austeretony.oxygen_teleportation.common.util.ImageTransferingClientBuffer;
import austeretony.oxygen_teleportation.common.util.ImageTransferingServerBuffer;

public class ImagesManagerClient {

    private final TeleportationManagerClient manager;

    private final Map<Long, ImageTransferingClientBuffer> transfers = new ConcurrentHashMap<>(5);

    private final Map<Long, BufferedImage> images = new ConcurrentHashMap<>();

    private BufferedImage latestImage;

    protected ImagesManagerClient(TeleportationManagerClient manager) {
        this.manager = manager;
    }

    public void preparePreviewImage() {
        this.latestImage = ScreenshotHelper.createScreenshot();
    }

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

    public Map<Long, ImageTransferingClientBuffer> getImageTransfers() {
        return this.transfers;
    }

    public void uploadCampPreviewToServerAsync(long pointId) {
        OxygenHelperClient.addIOTask(()->this.uploadCampPreviewToServer(pointId));
    }

    public void uploadCampPreviewToServer(long pointId) {
        List<int[]> fragments = BufferedImageUtils.convertBufferedImageToIntArraysList(this.getLatestImage());
        OxygenMain.network().sendToServer(new SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_CAMP, pointId, fragments.size()));  
        int index = 0;
        for (int[] part : fragments) {
            OxygenMain.network().sendToServer(new SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_CAMP, pointId, index, part, fragments.size()));
            index++;
        }
    }

    public void uploadLocationPreviewToServerAsync(long pointId) {
        OxygenHelperClient.addIOTask(()->this.uploadLocationPreviewToServer(pointId));
    }

    public void uploadLocationPreviewToServer(long pointId) {
        List<int[]> fragments = BufferedImageUtils.convertBufferedImageToIntArraysList(this.getLatestImage());
        OxygenMain.network().sendToServer(new SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_LOCATION, pointId, fragments.size()));  
        int index = 0;
        for (int[] part : fragments) {
            OxygenMain.network().sendToServer(new SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_LOCATION, pointId, index, part, fragments.size()));
            index++;
        }
    }
}