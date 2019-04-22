package austeretony.teleportation.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import austeretony.oxygen.common.api.IOxygenTask;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.teleportation.common.main.TeleportationMain;

public class CampsLoaderServer {

    private final TeleportationManagerServer manager;

    public CampsLoaderServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public void loadPlayerDataDelegated(UUID playerUUID) {
        OxygenHelperServer.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                loadPlayerData(playerUUID);
                manager.appendSharedPlayerData(playerUUID);
            }     
        });
    }

    public void loadPlayerData(UUID playerUUID) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/profile.dat";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(folder))) {    
                this.manager.getPlayerProfile(playerUUID).read(bis);
                //TeleportationMain.LOGGER.info("Player {} server data loaded.", playerUUID);
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Player {} server data loading failed.", playerUUID);
                exception.printStackTrace();
            }
        }
    }

    public void savePlayerDataDelegated(UUID playerUUID) {
        OxygenHelperServer.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                savePlayerData(playerUUID);
            }     
        });
    }

    public void savePlayerData(UUID playerUUID) {
        String folder = OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/profile.dat";
        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder))) {   
            this.manager.getPlayerProfile(playerUUID).write(bos);
        } catch (IOException exception) {
            TeleportationMain.LOGGER.error("Player {} server data saving failed.", playerUUID);
            exception.printStackTrace();
        }
    }
}
