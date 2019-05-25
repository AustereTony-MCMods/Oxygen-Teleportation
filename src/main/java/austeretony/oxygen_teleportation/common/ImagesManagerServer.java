package austeretony.oxygen_teleportation.common;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.IOxygenTask;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.ImagesLoaderServer.SplittedByteArray;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPDownloadImagePart;
import austeretony.oxygen_teleportation.common.network.client.CPStartImageDownload;
import austeretony.oxygen_teleportation.common.util.BufferedImageUtils;
import austeretony.oxygen_teleportation.common.util.ImageTransferingClientBuffer;
import austeretony.oxygen_teleportation.common.util.ImageTransferingServerBuffer;
import net.minecraft.entity.player.EntityPlayerMP;

public class ImagesManagerServer {

    private final TeleportationManagerServer manager;

    private final Map<Long, ImageTransferingServerBuffer> transfers = new ConcurrentHashMap<Long, ImageTransferingServerBuffer>();

    private final Map<Long, SplittedByteArray> locationsPreviews = new ConcurrentHashMap<Long, SplittedByteArray>();

    public ImagesManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public Map<Long, ImageTransferingServerBuffer> getImageTransfers() {
        return this.transfers;
    }

    public Map<Long, SplittedByteArray> getLocationPreviews() {
        return this.locationsPreviews;
    }

    public void replaceImageBytes(long oldPointId, long newPointId) {
        if (this.locationsPreviews.containsKey(oldPointId)) {
            this.locationsPreviews.put(newPointId, this.locationsPreviews.get(oldPointId));
            this.locationsPreviews.remove(oldPointId);
        }
    }

    public void downloadCampPreviewToClientDelegated(EntityPlayerMP playerMP, long pointId, BufferedImage bufferedImage) {
        OxygenHelperServer.addRoutineTask(new IOxygenTask() {

            @Override
            public void execute() {
                downloadCampPreviewToClient(playerMP, pointId, bufferedImage);
            }  
        });
    }

    public void downloadCampPreviewToClient(EntityPlayerMP playerMP, long pointId, BufferedImage bufferedImage) {
        List<byte[]> imageParts = BufferedImageUtils.convertBufferedImageToByteArraysList(bufferedImage);
        TeleportationMain.network().sendTo(new CPStartImageDownload(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_CAMP, pointId, imageParts.size()), playerMP);  
        int index = 0;
        for (byte[] part : imageParts) {
            TeleportationMain.network().sendTo(new CPDownloadImagePart(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_CAMP, pointId, index, part, imageParts.size()), playerMP);
            index++;
        }
    }

    public void downloadLocationPreviewsToClientDelegated(EntityPlayerMP playerMP, long[] locationIds) {
        OxygenHelperServer.addRoutineTask(new IOxygenTask() {

            @Override
            public void execute() {
                for (long id : locationIds)
                    downloadLocationPreviewToClient(playerMP, id);
            }  
        });
    }

    public void downloadLocationPreviewToClient(EntityPlayerMP playerMP, long pointId) {
        if (this.getLocationPreviews().containsKey(pointId)) {
            List<byte[]> imageParts = this.getLocationPreviews().get(pointId).getParts();
            TeleportationMain.network().sendTo(new CPStartImageDownload(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_LOCATION, pointId, imageParts.size()), playerMP);  
            int index = 0;
            for (byte[] part : imageParts) {
                TeleportationMain.network().sendTo(new CPDownloadImagePart(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_LOCATION, pointId, index, part, imageParts.size()), playerMP);
                index++;
            }
        } else 
            TeleportationMain.LOGGER.error("Location preview image {}.png bytes are absent, can't download image.", pointId);
    }
}
