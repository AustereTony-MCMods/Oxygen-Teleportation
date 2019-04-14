package austeretony.teleportation.client;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.IOxygenTask;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.client.util.ScreenshotHelper;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.network.server.SPStartImageUpload;
import austeretony.teleportation.common.network.server.SPUploadImagePart;
import austeretony.teleportation.common.util.BufferedImageUtils;
import austeretony.teleportation.common.util.ImageTransferingClientBuffer;
import austeretony.teleportation.common.util.ImageTransferingServerBuffer;

public class ImagesManagerClient {

    private final TeleportationManagerClient manager;

    private final Map<Long, ImageTransferingClientBuffer> transfers = new ConcurrentHashMap<Long, ImageTransferingClientBuffer>();

    private final Map<Long, BufferedImage> previewImages = new ConcurrentHashMap<Long, BufferedImage>();

    private BufferedImage latestImage;

    public ImagesManagerClient(TeleportationManagerClient manager) {
        this.manager = manager;
    }

    public void preparePreviewImage() {
        this.latestImage = ScreenshotHelper.createScreenshot();
    }

    public BufferedImage getLatestImage() {
        return this.latestImage;
    }

    public Map<Long, BufferedImage> getPreviewImages() {
        return this.previewImages;
    }

    public void cacheImage(long pointId, BufferedImage image) {
        this.previewImages.put(pointId, image);
    }

    public void cacheLatestImage(long pointId) {
        this.previewImages.put(pointId, this.getLatestImage());
    }

    public void removeCachedImage(long pointId) {
        this.previewImages.remove(pointId);
    }

    public void replaceCachedImage(long oldPointId, long newPointId) {
        if (this.previewImages.containsKey(oldPointId)) {
            this.previewImages.put(newPointId, this.previewImages.get(oldPointId));
            this.previewImages.remove(oldPointId);
        }
    }

    public Map<Long, ImageTransferingClientBuffer> getImageTransfers() {
        return this.transfers;
    }

    public void uploadCampPreviewToServerDelegated(long pointId) {
        OxygenHelperClient.addRoutineTask(new IOxygenTask() {

            @Override
            public void execute() {
                uploadCampPreviewToServer(pointId);
            }  
        });
    }

    public void uploadCampPreviewToServer(long pointId) {
        List<int[]> imageParts = BufferedImageUtils.convertBufferedImageToIntArraysList(this.getLatestImage());
        TeleportationMain.network().sendToServer(new SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_CAMP, pointId, imageParts.size()));  
        int index = 0;
        for (int[] part : imageParts) {
            TeleportationMain.network().sendToServer(new SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_CAMP, pointId, index, part, imageParts.size()));
            index++;
        }
    }

    public void uploadLocationPreviewToServerDelegated(long pointId) {
        OxygenHelperClient.addRoutineTask(new IOxygenTask() {

            @Override
            public void execute() {
                uploadLocationPreviewToServer(pointId);
            }  
        });
    }

    public void uploadLocationPreviewToServer(long pointId) {
        List<int[]> imageParts = BufferedImageUtils.convertBufferedImageToIntArraysList(this.getLatestImage());
        TeleportationMain.network().sendToServer(new SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_LOCATION, pointId, imageParts.size()));  
        int index = 0;
        for (int[] part : imageParts) {
            TeleportationMain.network().sendToServer(new SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_LOCATION, pointId, index, part, imageParts.size()));
            index++;
        }
    }
}
