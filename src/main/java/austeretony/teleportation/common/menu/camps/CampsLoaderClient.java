package austeretony.teleportation.common.menu.camps;

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
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CampsLoaderClient {

    public static void loadCampsDataDelegated() {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                loadPlayerData();
                loadCampPreviewImages();
            }     
        });
    }

    public static void loadPlayerData() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/teleportation.dat";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(folder))) {    
                CampsManagerClient.instance().setPlayerProfile(PlayerProfile.read(bis));
                TeleportationMain.LOGGER.info("Player client data loaded.");
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Player client data loading failed.");
                exception.printStackTrace();
            }
        } else 
            CampsManagerClient.instance().createPlayerProfile();
    }

    public static void loadCampPreviewImages() {
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
                    CampsManagerClient.instance().getPreviewImages().put(Long.parseLong(fileName.substring(0, 15)), bufferedImage);
                } catch (IOException exception) {
                    TeleportationMain.LOGGER.error("Failed to load camp preview image {}.", fileName);
                    exception.printStackTrace();
                }
            }
            TeleportationMain.LOGGER.info("Loaded camps preview images.");
        }
    }

    public static void savePlayerDataDelegated() {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                savePlayerData();
            }     
        });
    }

    public static void savePlayerData() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/teleportation.dat";
        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder))) {   
            CampsManagerClient.instance().getPlayerProfile().write(bos);
        } catch (IOException exception) {
            TeleportationMain.LOGGER.error("Player client data saving failed.");
            exception.printStackTrace();
        }
    }

    public static void saveCampPreviewImageDelegated(long pointId, BufferedImage image) {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                saveCampPreviewImage(pointId, image);
            }     
        });
    }

    public static void saveCampPreviewImage(long pointId, BufferedImage image) {
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

    public static void removeUnusedCampPreviewImagesDelegated() {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                removeUnusedCampPreviewImages();
            }     
        });
    }

    public static void removeUnusedCampPreviewImages() {
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
                if (!CampsManagerClient.instance().getPlayerProfile().campExist(Long.parseLong(fileName.substring(0, 15)))) {
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

    public static void removeCampPreviewImage(long pointId) {
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
    
    public static void renameCampPreviewImageDelegated(long oldPointId, long newPointId) {
        OxygenHelperClient.addIOTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                renameCampPreviewImage(oldPointId, newPointId);
            }     
        });
    }

    public static void renameCampPreviewImage(long oldPointId, long newPointId) {
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
}
