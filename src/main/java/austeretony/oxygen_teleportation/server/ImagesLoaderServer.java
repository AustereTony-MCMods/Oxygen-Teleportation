package austeretony.oxygen_teleportation.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import net.minecraft.entity.player.EntityPlayerMP;

public class ImagesLoaderServer {

    private final TeleportationManagerServer manager;

    protected ImagesLoaderServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public void loadAndSendCampPreviewImagesAsync(EntityPlayerMP playerMP, long[] campIds) {
        OxygenHelperServer.addIOTask(()->this.loadAndSendCampPreviewImages(playerMP, campIds));
    }

    public void loadAndSendCampPreviewImages(EntityPlayerMP playerMP, long[] campIds) {
        UUID ownerUUID = CommonReference.getPersistentUUID(playerMP);
        for (long id : campIds) {
            if (this.manager.getSharedCampsContainer().haveInvitation(ownerUUID, id))
                ownerUUID = this.manager.getSharedCampsContainer().getCampOwner(id);
            final byte[] imageRaw = this.loadCampPreviewImageBytes(ownerUUID, id);
            if (imageRaw != null)
                this.manager.getImagesManager().downloadCampPreviewToClientAsync(playerMP, id, imageRaw);
        }
    }

    public void loadAndSendCampPreviewImageAsync(EntityPlayerMP playerMP, long campId) {
        OxygenHelperServer.addIOTask(()->this.loadAndSendCampPreviewImage(playerMP, campId));
    }

    public void loadAndSendCampPreviewImage(EntityPlayerMP playerMP, long campId) {
        UUID ownerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.manager.getSharedCampsContainer().haveInvitation(ownerUUID, campId))
            ownerUUID = this.manager.getSharedCampsContainer().getCampOwner(campId);
        final byte[] imageRaw = this.loadCampPreviewImageBytes(ownerUUID, campId);
        if (imageRaw != null)
            this.manager.getImagesManager().downloadCampPreviewToClientAsync(playerMP, campId, imageRaw);
    }

    @Nullable
    public byte[] loadCampPreviewImageBytes(UUID playerUUID, long pointId) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/images/camps/" + pointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            File file = new File(folder);
            BufferedImage bufferedImage;
            try {
                bufferedImage = ImageIO.read(file);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                return baos.toByteArray();
            } catch (IOException exception) {
                OxygenMain.LOGGER.error("[Teleportation] Failed to obtain camp preview image {}.png bytes for player {}.", pointId, playerUUID);
                exception.printStackTrace();
            }
        }
        return null;
    }

    public void saveCampPreviewImageAsync(UUID playerUUID, long pointId, byte[] imageRaw) {
        OxygenHelperServer.addIOTask(()->this.saveCampPreviewImage(playerUUID, pointId, imageRaw));
    }

    public void saveCampPreviewImage(UUID playerUUID, long pointId, byte[] imageRaw) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/images/camps/" + pointId + ".png";
        Path path = Paths.get(folder);
        try {
            if (!Files.exists(path))
                Files.createDirectories(path.getParent());              

            ByteArrayInputStream baos = new ByteArrayInputStream(imageRaw);
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(baos);
            } catch (IOException exception) {
                OxygenMain.LOGGER.error("[Teleportation] Failed to create camp buffered image {}.png of raw bytes for player {}.", pointId, playerUUID);
                exception.printStackTrace();
            }

            if (bufferedImage != null)
                ImageIO.write(bufferedImage, "png", path.toFile());
        } catch (IOException exception) {          
            OxygenMain.LOGGER.error("[Teleportation] Failed to save camp preview image {}.png for player {}.", pointId, playerUUID);
            exception.printStackTrace();
        }
    }

    public void removeCampPreviewImageAsync(UUID playerUUID, long pointId) {
        OxygenHelperServer.addIOTask(()->this.removeCampPreviewImage(playerUUID, pointId));
    }

    public void removeCampPreviewImage(UUID playerUUID, long pointId) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/images/camps/" + pointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException exception) {
                OxygenMain.LOGGER.error("[Teleportation] Failed to remove camp preview image {}.png for player: {}.", pointId, playerUUID);
                exception.printStackTrace();
            }
        }
    }

    public void renameCampPreviewImageAsync(UUID playerUUID, long oldPointId, long newPointId) {
        OxygenHelperServer.addIOTask(()->this.renameCampPreviewImage(playerUUID, oldPointId, newPointId));
    }

    public void renameCampPreviewImage(UUID playerUUID, long oldPointId, long newPointId) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/images/camps/" + oldPointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try {
                Files.move(path, path.resolveSibling(newPointId + ".png"));
            } catch (IOException exception) {
                OxygenMain.LOGGER.error("[Teleportation] Failed to rename camp preview image {}.png.", oldPointId);
                exception.printStackTrace();
            }
        }
    }

    public void loadLocationPreviewImagesAsync() {
        OxygenHelperServer.addIOTask(this::loadLocationPreviewImages);
    }

    public void loadLocationPreviewImages() {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/images";
        String[] files = new File(folder).list((file, name)->name.endsWith(".png"));
        File file;
        BufferedImage bufferedImage;
        if (files != null) {
            for (String fileName : files) {
                file = new File(folder + "/" + fileName);
                try {
                    bufferedImage = ImageIO.read(file);
                    try {
                        Validate.validState(bufferedImage.getWidth() == TeleportationConfig.IMAGE_WIDTH.asInt());
                        Validate.validState(bufferedImage.getHeight() == TeleportationConfig.IMAGE_HEIGHT.asInt());
                    } catch (IllegalStateException exception) {
                        OxygenMain.LOGGER.error("[Teleportation] Invalid location preview image {}.", fileName);
                        return;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "png", baos);

                    this.manager.getImagesManager().getLocationPreviews().put(
                            Long.parseLong(StringUtils.remove(fileName, ".png")), 
                            baos.toByteArray());              
                } catch (IOException exception) {
                    OxygenMain.LOGGER.error("[Teleportation] Filed to load location preview image {}.", fileName);
                    exception.printStackTrace();
                }
            }
            OxygenMain.LOGGER.info("[Teleportation] Loaded locations preview images.");
        }
    }

    public void saveAndLoadBytesLocationPreviewAsync(long pointId, byte[] imageRaw) {    
        this.manager.getImagesManager().getLocationPreviews().put(pointId, imageRaw);

        OxygenHelperServer.addIOTask(()->this.saveLocationPreview(pointId, imageRaw));
    }

    public void saveLocationPreview(long pointId, byte[] imageRaw) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/images/" + pointId + ".png";
        Path path = Paths.get(folder);
        try {
            if (!Files.exists(path))
                Files.createDirectories(path.getParent());              

            ByteArrayInputStream baos = new ByteArrayInputStream(imageRaw);
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(baos);
            } catch (IOException exception) {
                OxygenMain.LOGGER.error("[Teleportation] Failed to create location buffered image {}.png of raw bytes.", pointId);
                exception.printStackTrace();
            }

            if (bufferedImage != null)
                ImageIO.write(bufferedImage, "png", path.toFile());
        } catch (IOException exception) {        
            OxygenMain.LOGGER.error("[Teleportation] Failed to save location preview image {}.png", pointId);
            exception.printStackTrace();
        }
    }

    public void removeLocationPreviewImageAsync(long pointId) {
        OxygenHelperServer.addIOTask(()->this.removeLocationPreviewImage(pointId));
    }

    public void removeLocationPreviewImage(long pointId) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/images/" + pointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException exception) {
                OxygenMain.LOGGER.error("[Teleportation] Failed to remove location preview image {}.png.", pointId);
                exception.printStackTrace();
            }
        }
    }

    public void renameLocationPreviewImageAsync(long oldPointId, long newPointId) {
        OxygenHelperServer.addIOTask(()->this.renameLocationPreviewImage(oldPointId, newPointId));
    }

    public void renameLocationPreviewImage(long oldPointId, long newPointId) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/images/" + oldPointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try {
                Files.move(path, path.resolveSibling(newPointId + ".png"));
            } catch (IOException exception) {
                OxygenMain.LOGGER.error("[Teleportation] Failed to rename location preview image {}.png.", oldPointId);
                exception.printStackTrace();
            }
        }
    }
}
