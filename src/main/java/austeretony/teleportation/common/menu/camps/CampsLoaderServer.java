package austeretony.teleportation.common.menu.camps;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.imageio.ImageIO;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.OxygenTask;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import net.minecraft.entity.player.EntityPlayerMP;

public class CampsLoaderServer {

    public static void loadPlayerDataDelegated(UUID playerUUID) {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                loadPlayerData(playerUUID);
                CampsManagerServer.instance().appendAdditionalPlayerData(playerUUID);
            }     
        });
    }

    public static void loadPlayerData(UUID playerUUID) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/teleportation.dat";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(folder))) {    
                CampsManagerServer.instance().addPlayersProfile(playerUUID, PlayerProfile.read(bis));
                TeleportationMain.LOGGER.info("Player {} server data loaded.", playerUUID);
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Player {} server data loading failed.", playerUUID);
                exception.printStackTrace();
            }
        } else 
            CampsManagerServer.instance().createPlayerProfile(playerUUID);
    }

    public static void loadAndSendCampPreviewImagesDelegated(EntityPlayerMP playerMP, long[] campIds) {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                loadAndSendCampPreviewImages(playerMP, campIds);
            }     
        });
    }

    public static void loadAndSendCampPreviewImages(EntityPlayerMP playerMP, long[] campIds) {
        UUID ownerUUID;
        BufferedImage bufferedImage;
        for (long id : campIds) {
            if (CampsManagerServer.instance().getPlayerProfile(OxygenHelperServer.uuid(playerMP)).getOtherCampIds().contains(id))
                ownerUUID = CampsManagerServer.instance().getPlayerProfile(OxygenHelperServer.uuid(playerMP)).getOtherCampOwner(id);
            else
                ownerUUID = OxygenHelperServer.uuid(playerMP);
            bufferedImage = loadCampPreviewImage(ownerUUID, id);
            if (bufferedImage != null)
                CampsManagerServer.instance().downloadCampPreviewToClientDelegated(playerMP, id, bufferedImage);
        }
    }

    public static BufferedImage loadCampPreviewImage(UUID playerUUID, long pointId) {
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

    public static void savePlayerDataDelegated(UUID playerUUID) {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                savePlayerData(playerUUID);
            }     
        });
    }

    public static void savePlayerData(UUID playerUUID) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/teleportation.dat";
        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder))) {   
            CampsManagerServer.instance().getPlayerProfile(playerUUID).write(bos);
        } catch (IOException exception) {
            TeleportationMain.LOGGER.error("Player {} server data saving failed.", playerUUID);
            exception.printStackTrace();
        }
    }

    public static void saveCampPreviewImageDelegated(UUID playerUUID, long pointId, BufferedImage image) {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                saveCampPreviewImage(playerUUID, pointId, image);
            }     
        });
    }

    public static void saveCampPreviewImage(UUID playerUUID, long pointId, BufferedImage image) {
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

    public static void removeCampPreviewImageDelegated(UUID playerUUID, long pointId) {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                removeCampPreviewImage(playerUUID, pointId);
            }     
        });
    }

    public static void removeCampPreviewImage(UUID playerUUID, long pointId) {
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

    public static void renameCampPreviewImageDelegated(UUID playerUUID, long oldPointId, long newPointId) {
        OxygenHelperServer.addIOTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                renameCampPreviewImage(playerUUID, oldPointId, newPointId);
            }     
        });
    }

    public static void renameCampPreviewImage(UUID playerUUID, long oldPointId, long newPointId) {
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
}
