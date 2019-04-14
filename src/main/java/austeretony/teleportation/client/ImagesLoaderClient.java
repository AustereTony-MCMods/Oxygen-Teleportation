package austeretony.teleportation.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.Validate;

import austeretony.oxygen.common.api.IOxygenTask;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.TeleportationMain;

public class ImagesLoaderClient {

    private final TeleportationManagerClient manager;

    public ImagesLoaderClient(TeleportationManagerClient manager) {
        this.manager = manager;
    }

    public void loadCampPreviewImages() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/images/camps";
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
                        TeleportationMain.LOGGER.error("Invalid camp preview image {}.", fileName);
                        return;
                    }
                    this.manager.getImagesManager().getPreviewImages().put(Long.parseLong(fileName.substring(0, 15)), bufferedImage);
                } catch (IOException exception) {
                    TeleportationMain.LOGGER.error("Failed to load camp preview image {}.", fileName);
                    exception.printStackTrace();
                }
            }
            TeleportationMain.LOGGER.info("Loaded camps preview images.");
        }
    }

    public void saveLatestCampPreviewImageDelegated(long pointId) {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                saveCampPreviewImage(pointId, manager.getImagesManager().getLatestImage());
            }     
        });
    }

    public void saveCampPreviewImageDelegated(long pointId, BufferedImage image) {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                saveCampPreviewImage(pointId, image);
            }     
        });
    }

    public void saveCampPreviewImage(long pointId, BufferedImage image) {
        String folder = OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/images/camps/" + pointId + ".png";
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
            TeleportationMain.LOGGER.error("Failed to save camp preview image {}.png.", pointId);
            exception.printStackTrace();
        }
    }

    public void removeUnusedCampPreviewImagesDelegated() {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                removeUnusedCampPreviewImages();
            }     
        });
    }

    public void removeUnusedCampPreviewImages() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/images/camps";
        String[] files = new File(folder).list(new FilenameFilter() {

            @Override 
            public boolean accept(File folder, String name) {
                return name.endsWith(".png");
            }          
        });
        if (files != null) {
            Path path;
            for (String fileName : files) {
                path = Paths.get(folder + "/" + fileName);
                if (!this.manager.getPlayerData().campExist(Long.parseLong(fileName.substring(0, 15)))) {
                    try {
                        Files.delete(path);
                    } catch (IOException exception) {
                        TeleportationMain.LOGGER.error("Failed to remove camp preview image {}.", fileName);
                        exception.printStackTrace();
                    }
                }
            }
            TeleportationMain.LOGGER.info("Removed unused camps preview images.");
        }
    }

    public void removeCampPreviewImage(long pointId) {
        String folder = OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/images/camps/" + pointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Failed to remove camp preview image {}.png.", pointId);
                exception.printStackTrace();
            }
        }
    }

    public void renameCampPreviewImageDelegated(long oldPointId, long newPointId) {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                renameCampPreviewImage(oldPointId, newPointId);
            }     
        });
    }

    public void renameCampPreviewImage(long oldPointId, long newPointId) {
        String folder = OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/images/camps/" + oldPointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try {
                Files.move(path, path.resolveSibling(newPointId + ".png"));
                TeleportationMain.LOGGER.info("Renamed camp preview image {}.png to {}.png", oldPointId, newPointId);
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Failed to rename camp preview image {}.png.", oldPointId);
                exception.printStackTrace();
            }
        }
    }

    public void loadLocationPreviewImages() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/world/teleportation/locations/images";
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
                        TeleportationMain.LOGGER.error("Invalid location preview image: {}.", fileName);
                        return;
                    }
                    TeleportationManagerClient.instance().getImagesManager().getPreviewImages().put(Long.parseLong(fileName.substring(0, 15)), bufferedImage);
                } catch (IOException exception) {
                    TeleportationMain.LOGGER.error("Failed to load location preview image: {}.", fileName);
                    exception.printStackTrace();
                }
            }
            TeleportationMain.LOGGER.info("Loaded locations preview images.");
        }
    }

    public void saveLatestLocationPreviewImageDelegated(long pointId) {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                saveLocationPreviewImage(pointId, manager.getImagesManager().getLatestImage());
            }     
        });
    }

    public void saveLocationPreviewImageDelegated(long pointId, BufferedImage image) {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                saveLocationPreviewImage(pointId, image);
            }     
        });
    }

    public void saveLocationPreviewImage(long pointId, BufferedImage image) {
        String folder = OxygenHelperClient.getDataFolder() + "/client/world/teleportation/locations/images/" + pointId + ".png";
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
            TeleportationMain.LOGGER.error("Failed to save location preview image: {}.png", pointId);
            exception.printStackTrace();
        }
    }

    public void removeUnusedLocationPreviewImagesDelegated() {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                removeUnusedLocationPreviewImages();
            }     
        });
    }

    public void removeUnusedLocationPreviewImages() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/world/teleportation/locations/images";
        String[] files = new File(folder).list(new FilenameFilter() {

            @Override 
            public boolean accept(File folder, String name) {
                return name.endsWith(".png");
            }          
        });
        if (files != null) {
            Path path;
            for (String fileName : files) {
                path = Paths.get(folder + "/" + fileName);
                if (!TeleportationManagerClient.instance().getWorldProfile().locationExist(Long.parseLong(fileName.substring(0, 15)))) {
                    try {
                        Files.delete(path);
                    } catch (IOException exception) {
                        TeleportationMain.LOGGER.error("Failed to remove location preview image {}.", fileName);
                        exception.printStackTrace();
                    }
                }
            }
            TeleportationMain.LOGGER.info("Removed unused locations preview images.");
        }
    }

    public void removeLocationPreviewImageDelegated(long pointId) {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                removeLocationPreviewImage(pointId);
            }     
        });
    }

    public void removeLocationPreviewImage(long pointId) {
        String folder = OxygenHelperClient.getDataFolder() + "/client/world/teleportation/locations/images/" + pointId + ".png";
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
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                renameLocationPreviewImage(oldPointId, newPointId);
            }     
        });
    }

    public void renameLocationPreviewImage(long oldPointId, long newPointId) {
        String folder = OxygenHelperClient.getDataFolder() + "/client/world/teleportation/locations/images/" + oldPointId + ".png";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try {
                Files.move(path, path.resolveSibling(newPointId + ".png"));
                TeleportationMain.LOGGER.info("Renamed location preview image {}.png to {}.png", oldPointId, newPointId);
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Failed to rename location preview image {}.png.", oldPointId);
                exception.printStackTrace();
            }
        }
    }
}
