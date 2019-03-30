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

import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.api.OxygenTask;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LocationsLoaderClient {

    public static void loadLocationsDataDelegated() {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                loadLocationsData();
                loadLocationPreviewImages();
            }     
        });
    }

    public static void loadLocationsData() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/world/teleportation/locations/locations.dat";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(folder))) {    
                LocationsManagerClient.instance().getWorldProfile().read(bis);
                TeleportationMain.LOGGER.info("World client data loaded.");
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("World client data loading failed.");
                exception.printStackTrace();
            }
        }
    }

    public static void loadLocationPreviewImages() {
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
                    CampsManagerClient.instance().getPreviewImages().put(Long.parseLong(fileName.substring(0, 15)), bufferedImage);
                } catch (IOException exception) {
                    TeleportationMain.LOGGER.error("Failed to load location preview image: {}.", fileName);
                    exception.printStackTrace();
                }
            }
            TeleportationMain.LOGGER.info("Loaded locations preview images.");
        }
    }

    public static void saveLocationsDataDelegated() {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                saveLocationsData();
            }
        });
    }

    public static void saveLocationsData() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/world/teleportation/locations/locations.dat";
        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder))) {   
            LocationsManagerClient.instance().getWorldProfile().write(bos);
        } catch (IOException exception) {
            TeleportationMain.LOGGER.error("Locations client data saving failed.");
            exception.printStackTrace();
        }
    }

    public static void saveLocationPreviewImageDelegated(long pointId, BufferedImage image) {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                saveLocationPreviewImage(pointId, image);
            }     
        });
    }

    public static void saveLocationPreviewImage(long pointId, BufferedImage image) {
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

    public static void removeUnusedLocationPreviewImagesDelegated() {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                removeUnusedLocationPreviewImages();
            }     
        });
    }

    public static void removeUnusedLocationPreviewImages() {
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
                if (!LocationsManagerClient.instance().getWorldProfile().locationExist(Long.parseLong(fileName.substring(0, 15)))) {
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

    public static void removeLocationPreviewImageDelegated(long pointId) {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                removeLocationPreviewImage(pointId);
            }     
        });
    }

    public static void removeLocationPreviewImage(long pointId) {
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

    public static void renameLocationPreviewImageDelegated(long oldPointId, long newPointId) {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                renameLocationPreviewImage(oldPointId, newPointId);
            }     
        });
    }

    public static void renameLocationPreviewImage(long oldPointId, long newPointId) {
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
