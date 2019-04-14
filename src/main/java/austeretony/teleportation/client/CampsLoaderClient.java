package austeretony.teleportation.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import austeretony.oxygen.common.api.IOxygenTask;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.teleportation.common.main.TeleportationMain;

public class CampsLoaderClient {

    private final TeleportationManagerClient manager;

    public CampsLoaderClient(TeleportationManagerClient manager) {
        this.manager = manager;
    }

    public void loadCampsDataDelegated() {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                loadPlayerData();
                manager.getImagesLoader().loadCampPreviewImages();
            }     
        });
    }

    public void loadPlayerData() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/profile.dat";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(folder))) {    
                this.manager.getPlayerData().read(bis);
                TeleportationMain.LOGGER.info("Player client data loaded.");
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Player client data loading failed.");
                exception.printStackTrace();
            }
        }
    }

    public void savePlayerDataDelegated() {
        OxygenHelperClient.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                savePlayerData();
            }     
        });
    }

    public void savePlayerData() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/teleportation/profile.dat";
        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder))) {   
            this.manager.getPlayerData().write(bos);
        } catch (IOException exception) {
            TeleportationMain.LOGGER.error("Player client data saving failed.");
            exception.printStackTrace();
        }
    }
}
