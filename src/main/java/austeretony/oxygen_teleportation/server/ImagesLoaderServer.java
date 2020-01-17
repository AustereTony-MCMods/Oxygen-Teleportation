package austeretony.oxygen_teleportation.server;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
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
        BufferedImage bufferedImage;
        for (long id : campIds) {
            if (this.manager.getSharedCampsContainer().haveInvitation(ownerUUID, id))
                ownerUUID = this.manager.getSharedCampsContainer().getCampOwner(id);
            bufferedImage = this.loadCampPreviewImage(ownerUUID, id);
            if (bufferedImage != null)
                this.manager.getImagesManager().downloadCampPreviewToClientAsync(playerMP, id, bufferedImage);
        }
    }

    public void loadAndSendCampPreviewImageAsync(EntityPlayerMP playerMP, long campId) {
        OxygenHelperServer.addIOTask(()->this.loadAndSendCampPreviewImage(playerMP, campId));
    }

    public void loadAndSendCampPreviewImage(EntityPlayerMP playerMP, long campId) {
        UUID ownerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.manager.getSharedCampsContainer().haveInvitation(ownerUUID, campId))
            ownerUUID = this.manager.getSharedCampsContainer().getCampOwner(campId);
        BufferedImage bufferedImage = this.loadCampPreviewImage(ownerUUID, campId);
        if (bufferedImage != null)
            this.manager.getImagesManager().downloadCampPreviewToClientAsync(playerMP, campId, bufferedImage);
    }

    public BufferedImage loadCampPreviewImage(UUID playerUUID, long pointId) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/images/camps/" + pointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            File file = new File(folder);
            BufferedImage bufferedImage;
            try {
                bufferedImage = ImageIO.read(file);
                return bufferedImage;
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Failed to load camp preview image {}.png for player {}.", pointId, playerUUID);
                exception.printStackTrace();
            }
        }
        return null;
    }

    public void saveCampPreviewImageAsync(UUID playerUUID, long pointId, BufferedImage image) {
        OxygenHelperServer.addIOTask(()->this.saveCampPreviewImage(playerUUID, pointId, image));
    }

    public void saveCampPreviewImage(UUID playerUUID, long pointId, BufferedImage image) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/images/camps/" + pointId + ".png";
        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
            try {                   
                Files.createDirectories(path.getParent());              
            } catch (IOException exception) {               
                exception.printStackTrace();
            }
        }
        try {
            ImageIO.write(image, "png", path.toFile());
        } catch (IOException exception) {          
            TeleportationMain.LOGGER.error("Failed to save camp preview image {}.png for player {}.", pointId, playerUUID);
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
                TeleportationMain.LOGGER.error("Failed to remove camp preview image {}.png for player: {}.", pointId, playerUUID);
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
                TeleportationMain.LOGGER.error("Failed to rename camp preview image {}.png.", oldPointId);
                exception.printStackTrace();
            }
        }
    }

    public void loadLocationPreviewImagesAsync() {
        OxygenHelperServer.addIOTask(()->this.loadLocationPreviewImages());
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
                        TeleportationMain.LOGGER.error("Invalid location preview image {}.", fileName);
                        return;
                    }
                    this.manager.getImagesManager().getLocationPreviews().put(
                            Long.parseLong(StringUtils.remove(fileName, ".png")), 
                            ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData());              
                } catch (IOException exception) {
                    TeleportationMain.LOGGER.error("Filed to load location preview image {}.", fileName);
                    exception.printStackTrace();
                }
            }
            TeleportationMain.LOGGER.info("Loaded locations preview images.");
        }
    }

    public void saveAndLoadBytesLocationPreviewAsync(long pointId, BufferedImage image) {        
        OxygenHelperServer.addIOTask(()->{
            this.saveLocationPreview(pointId, image);
            this.loadLocationPreviewBytes(pointId);   
        });
    }

    public void saveLocationPreview(long pointId, BufferedImage image) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/images/" + pointId + ".png";
        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
            try {                   
                Files.createDirectories(path.getParent());              
            } catch (IOException exception) {               
                exception.printStackTrace();
            }
        }
        try {
            ImageIO.write(image, "png", path.toFile());
        } catch (IOException exception) {        
            TeleportationMain.LOGGER.error("Failed to save location preview image {}.png", pointId);
            exception.printStackTrace();
        }
    }

    public void loadLocationPreviewBytes(long pointId) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/images/" + pointId + ".png";
        Path path = Paths.get(folder);
        BufferedImage bufferedImage;
        if (Files.exists(path)) {
            bufferedImage = loadLocationPreviewImageServer(pointId);
            if (bufferedImage != null)
                this.manager.getImagesManager().getLocationPreviews().put(
                        pointId, 
                        ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData());              
        }
    }

    public BufferedImage loadLocationPreviewImageServer(long pointId) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/images/" + pointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            File file = new File(folder);
            BufferedImage bufferedImage;
            try {
                bufferedImage = ImageIO.read(file);
                return bufferedImage;
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Failed to load location preview image {}.png", pointId);
                exception.printStackTrace();
            }
        }
        return null;
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
                TeleportationMain.LOGGER.error("Failed to remove location preview image {}.png.", pointId);
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
                TeleportationMain.LOGGER.error("Failed to rename location preview image {}.png.", oldPointId);
                exception.printStackTrace();
            }
        }
    }
}