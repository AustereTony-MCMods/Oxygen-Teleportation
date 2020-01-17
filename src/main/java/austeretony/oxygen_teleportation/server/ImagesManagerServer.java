package austeretony.oxygen_teleportation.server;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPDownloadPreviewImage;
import austeretony.oxygen_teleportation.common.util.ImageTransferingServerBuffer;
import net.minecraft.entity.player.EntityPlayerMP;

public class ImagesManagerServer {

    private final TeleportationManagerServer manager;

    private final Map<Long, ImageTransferingServerBuffer> transfers = new ConcurrentHashMap<>(5);

    private final Map<Long, byte[]> rawImages = new ConcurrentHashMap<>();

    protected ImagesManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public Map<Long, ImageTransferingServerBuffer> getImageTransfers() {
        return this.transfers;
    }

    public Map<Long, byte[]> getLocationPreviews() {
        return this.rawImages;
    }

    public void replaceImageBytes(long oldPointId, long newPointId) {
        if (this.rawImages.containsKey(oldPointId)) {
            this.rawImages.put(newPointId, this.rawImages.get(oldPointId));
            this.rawImages.remove(oldPointId);
        }
    }

    public void downloadCampPreviewToClientAsync(EntityPlayerMP playerMP, long pointId, BufferedImage bufferedImage) {
        OxygenHelperServer.addRoutineTask(()->this.downloadCampPreviewToClient(playerMP, pointId, bufferedImage));
    }

    public void downloadCampPreviewToClient(EntityPlayerMP playerMP, long pointId, BufferedImage bufferedImage) {
        final byte[] imageRaw = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        OxygenMain.network().sendTo(new CPDownloadPreviewImage(EnumWorldPoint.CAMP, pointId, imageRaw), playerMP);  
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
        final byte[] imageRaw = this.getLocationPreviews().get(pointId);
        if (imageRaw != null)
            OxygenMain.network().sendTo(new CPDownloadPreviewImage(EnumWorldPoint.LOCATION, pointId, imageRaw), playerMP);  
        else 
            TeleportationMain.LOGGER.error("Location preview image {}.png bytes are absent, can't download image.", pointId);
    }
}
