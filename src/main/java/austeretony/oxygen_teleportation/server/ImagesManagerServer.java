package austeretony.oxygen_teleportation.server;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.BufferedImageUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPDownloadImagePart;
import austeretony.oxygen_teleportation.common.network.client.CPStartImageDownload;
import austeretony.oxygen_teleportation.common.util.ImageTransferingClientBuffer;
import austeretony.oxygen_teleportation.common.util.ImageTransferingServerBuffer;
import austeretony.oxygen_teleportation.common.util.SplittedByteArray;
import net.minecraft.entity.player.EntityPlayerMP;

public class ImagesManagerServer {

    private final TeleportationManagerServer manager;

    private final Map<Long, ImageTransferingServerBuffer> transfers = new ConcurrentHashMap<>(5);

    private final Map<Long, SplittedByteArray> images = new ConcurrentHashMap<>();

    protected ImagesManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public Map<Long, ImageTransferingServerBuffer> getImageTransfers() {
        return this.transfers;
    }

    public Map<Long, SplittedByteArray> getLocationPreviews() {
        return this.images;
    }

    public void replaceImageBytes(long oldPointId, long newPointId) {
        if (this.images.containsKey(oldPointId)) {
            this.images.put(newPointId, this.images.get(oldPointId));
            this.images.remove(oldPointId);
        }
    }

    public void downloadCampPreviewToClientAsync(EntityPlayerMP playerMP, long pointId, BufferedImage bufferedImage) {
        OxygenHelperServer.addRoutineTask(()->this.downloadCampPreviewToClient(playerMP, pointId, bufferedImage));
    }

    public void downloadCampPreviewToClient(EntityPlayerMP playerMP, long pointId, BufferedImage bufferedImage) {
        List<byte[]> fragments = BufferedImageUtils.convertBufferedImageToByteArraysList(bufferedImage);
        OxygenMain.network().sendTo(new CPStartImageDownload(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_CAMP, pointId, fragments.size()), playerMP);  
        int index = 0;
        for (byte[] part : fragments) {
            OxygenMain.network().sendTo(new CPDownloadImagePart(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_CAMP, pointId, index, part, fragments.size()), playerMP);
            index++;
        }
    }

    public void downloadLocationPreviewsToClientAsync(EntityPlayerMP playerMP, long[] locationIds) {
        OxygenHelperServer.addRoutineTask(()->{
            for (long id : locationIds)
                this.downloadLocationPreviewToClient(playerMP, id);
        });
    }
    
    public void downloadLocationPreviewToClientAsync(EntityPlayerMP playerMP, long pointId) {
        OxygenHelperServer.addRoutineTask(()->this.downloadLocationPreviewToClient(playerMP, pointId));
    }

    public void downloadLocationPreviewToClient(EntityPlayerMP playerMP, long pointId) {
        if (this.getLocationPreviews().containsKey(pointId)) {
            List<byte[]> fragments = this.getLocationPreviews().get(pointId).getParts();
            OxygenMain.network().sendTo(new CPStartImageDownload(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_LOCATION, pointId, fragments.size()), playerMP);  
            int index = 0;
            for (byte[] part : fragments) {
                OxygenMain.network().sendTo(new CPDownloadImagePart(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_LOCATION, pointId, index, part, fragments.size()), playerMP);
                index++;
            }
        } else 
            TeleportationMain.LOGGER.error("Location preview image {}.png bytes are absent, can't download image.", pointId);
    }
}
