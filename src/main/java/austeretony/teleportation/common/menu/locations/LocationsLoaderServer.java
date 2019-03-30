package austeretony.teleportation.common.menu.locations;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.Validate;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.OxygenTask;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.util.BufferedImageUtils;

public class LocationsLoaderServer {

    public static void loadLocationsDataDelegated() {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                loadLocationsData();
                loadLocationPreviewImages();
            }     
        });
    }

    public static void loadLocationsData() {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/locations.dat";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(folder))) {    
                LocationsManagerServer.instance().getWorldProfile().read(bis);
                TeleportationMain.LOGGER.info("World server data loaded.");
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("World server data loading failed.");
                exception.printStackTrace();
            }
        }
    }

    public static void loadLocationPreviewImages() {
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
                    LocationsManagerServer.instance().getLocationPreviewBytes().put(Long.parseLong(fileName.substring(0, 15)), new SplittedByteArray(BufferedImageUtils.convertBufferedImageToByteArraysList(bufferedImage)));              
                } catch (IOException exception) {
                    TeleportationMain.LOGGER.error("Filed to load location preview image {}.", fileName);
                    exception.printStackTrace();
                }
            }
            TeleportationMain.LOGGER.info("Loaded locations preview images.");
        }
    }

    public static void saveLocationsDataDelegated() {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                saveLocationsData();
            }
        });
    }

    public static void saveLocationsData() {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/locations.dat";
        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder))) {   
            LocationsManagerServer.instance().getWorldProfile().write(bos);
        } catch (IOException exception) {
            TeleportationMain.LOGGER.error("Locations server data saving failed.");
            exception.printStackTrace();
        }
    }

    public static void saveAndLoadBytesLocationPreviewDelegated(long pointId, BufferedImage image) {        
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                saveLocationPreview(pointId, image);
                loadLocationPreviewBytes(pointId);
            }     
        });
    }

    public static void saveLocationPreview(long pointId, BufferedImage image) {
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

    public static void loadLocationPreviewBytes(long pointId) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/images/" + pointId + ".png";
        Path path = Paths.get(folder);
        BufferedImage bufferedImage;
        if (Files.exists(path)) {
            bufferedImage = loadLocationPreviewImageServer(pointId);
            if (bufferedImage != null)
                LocationsManagerServer.instance().getLocationPreviewBytes().put(pointId, new SplittedByteArray(BufferedImageUtils.convertBufferedImageToByteArraysList(bufferedImage)));              
        }
    }

    public static BufferedImage loadLocationPreviewImageServer(long pointId) {
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

    public static void removeLocationPreviewImageDelegated(long pointId) {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                removeLocationPreviewImage(pointId);
            }     
        });
    }

    public static void removeLocationPreviewImage(long pointId) {
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

    public static void renameLocationPreviewImageDelegated(long oldPointId, long newPointId) {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                renameLocationPreviewImage(oldPointId, newPointId);
            }     
        });
    }

    public static void renameLocationPreviewImage(long oldPointId, long newPointId) {
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
