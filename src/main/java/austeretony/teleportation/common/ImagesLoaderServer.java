package austeretony.teleportation.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.Validate;

import austeretony.oxygen.common.api.IOxygenTask;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.locations.SplittedByteArray;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.util.BufferedImageUtils;
import net.minecraft.entity.player.EntityPlayerMP;

public class ImagesLoaderServer {

    private final TeleportationManagerServer manager;

    public ImagesLoaderServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public void loadAndSendCampPreviewImagesDelegated(EntityPlayerMP playerMP, long[] campIds) {
        OxygenHelperServer.addIOTaskServer(new IOxygenTask() {

            @Override
            public void execute() {
                loadAndSendCampPreviewImages(playerMP, campIds);
            }     
        });
    }

    public void loadAndSendCampPreviewImages(EntityPlayerMP playerMP, long[] campIds) {
        UUID ownerUUID;
        BufferedImage bufferedImage;
        for (long id : campIds) {
            if (this.manager.getPlayerProfile(CommonReference.uuid(playerMP)).getOtherCampIds().contains(id))
                ownerUUID = this.manager.getPlayerProfile(CommonReference.uuid(playerMP)).getOtherCampOwner(id);
            else
                ownerUUID = CommonReference.uuid(playerMP);
            bufferedImage = this.loadCampPreviewImage(ownerUUID, id);
            if (bufferedImage != null)
                this.manager.getImagesManager().downloadCampPreviewToClientDelegated(playerMP, id, bufferedImage);
        }
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

    public void saveCampPreviewImageDelegated(UUID playerUUID, long pointId, BufferedImage image) {
        OxygenHelperServer.addIOTaskServer(new IOxygenTask() {

            @Override
            public void execute() {
                saveCampPreviewImage(playerUUID, pointId, image);
            }     
        });
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

    public void removeCampPreviewImageDelegated(UUID playerUUID, long pointId) {
        OxygenHelperServer.addIOTaskServer(new IOxygenTask() {

            @Override
            public void execute() {
                removeCampPreviewImage(playerUUID, pointId);
            }     
        });
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

    public void renameCampPreviewImageDelegated(UUID playerUUID, long oldPointId, long newPointId) {
        OxygenHelperServer.addIOTaskServer(new IOxygenTask() {

            @Override
            public void execute() {
                renameCampPreviewImage(playerUUID, oldPointId, newPointId);
            }     
        });
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

    public void loadLocationPreviewImages() {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/images";
        String[] files = new File(folder).list(new FilenameFilter() {

            @Override 
            public boolean accept(File folder, String name) {
                return name.endsWith(".png");
            }          
        });
        File file;
        BufferedImage bufferedImage;
        if (files != null) {
            for (String fileName : files) {
                file = new File(folder + "/" + fileName);
                try {
                    bufferedImage = ImageIO.read(file);
                    try {
                        Validate.validState(bufferedImage.getWidth() == TeleportationConfig.IMAGE_WIDTH.getIntValue());
                        Validate.validState(bufferedImage.getHeight() == TeleportationConfig.IMAGE_HEIGHT.getIntValue());
                    } catch (IllegalStateException exception) {
                        TeleportationMain.LOGGER.error("Invalid location preview image {}.", fileName);
                        return;
                    }
                    this.manager.getImagesManager().getLocationPreviews().put(Long.parseLong(fileName.substring(0, 15)), new SplittedByteArray(BufferedImageUtils.convertBufferedImageToByteArraysList(bufferedImage)));              
                } catch (IOException exception) {
                    TeleportationMain.LOGGER.error("Filed to load location preview image {}.", fileName);
                    exception.printStackTrace();
                }
            }
            TeleportationMain.LOGGER.info("Loaded locations preview images.");
        }
    }

    public void saveAndLoadBytesLocationPreviewDelegated(long pointId, BufferedImage image) {        
        OxygenHelperServer.addIOTaskServer(new IOxygenTask() {

            @Override
            public void execute() {
                saveLocationPreview(pointId, image);
                loadLocationPreviewBytes(pointId);
            }     
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
                this.manager.getImagesManager().getLocationPreviews().put(pointId, new SplittedByteArray(BufferedImageUtils.convertBufferedImageToByteArraysList(bufferedImage)));              
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

    public void removeLocationPreviewImageDelegated(long pointId) {
        OxygenHelperServer.addIOTaskServer(new IOxygenTask() {

            @Override
            public void execute() {
                removeLocationPreviewImage(pointId);
            }     
        });
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

    public void renameLocationPreviewImageDelegated(long oldPointId, long newPointId) {
        OxygenHelperServer.addIOTaskServer(new IOxygenTask() {

            @Override
            public void execute() {
                renameLocationPreviewImage(oldPointId, newPointId);
            }     
        });
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
