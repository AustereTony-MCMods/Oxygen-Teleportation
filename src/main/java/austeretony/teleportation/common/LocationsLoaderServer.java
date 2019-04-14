package austeretony.teleportation.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import austeretony.oxygen.common.api.IOxygenTask;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.teleportation.common.main.TeleportationMain;

public class LocationsLoaderServer {

    private final TeleportationManagerServer manager;

    public LocationsLoaderServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public void loadLocationsDataDelegated() {
        OxygenHelperServer.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                loadLocationsData();
                manager.getImagesLoader().loadLocationPreviewImages();
            }     
        });
    }

    public void loadLocationsData() {
        String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/locations/locations.dat";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(folder))) {    
                this.manager.getWorldData().read(bis);
                TeleportationMain.LOGGER.info("World server data loaded.");
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("World server data loading failed.");
                exception.printStackTrace();
            }
        }
    }

    public void saveLocationsDataDelegated() {
        OxygenHelperServer.addIOTask(new IOxygenTask() {

            @Override
            public void execute() {
                saveLocationsData();
            }
        });
    }

    public void saveLocationsData() {
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
            this.manager.getWorldData().write(bos);
        } catch (IOException exception) {
            TeleportationMain.LOGGER.error("Locations server data saving failed.");
            exception.printStackTrace();
        }
    }
}
