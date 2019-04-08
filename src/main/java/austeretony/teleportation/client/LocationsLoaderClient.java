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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LocationsLoaderClient {

    private final TeleportationManagerClient manager;

    public LocationsLoaderClient(TeleportationManagerClient manager) {
        this.manager = manager;
    }

    public void loadLocationsDataDelegated() {
        OxygenHelperClient.addIOTaskClient(new IOxygenTask() {

            @Override
            public void execute() {
                loadLocationsData();
                manager.getImagesLoader().loadLocationPreviewImages();
            }     
        });
    }

    public void loadLocationsData() {
        String folder = OxygenHelperClient.getDataFolder() + "/client/world/teleportation/locations/locations.dat";
        Path path = Paths.get(folder);
        if (Files.exists(path)) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(folder))) {    
                this.manager.getWorldProfile().read(bis);
                TeleportationMain.LOGGER.info("World client data loaded.");
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("World client data loading failed.");
                exception.printStackTrace();
            }
        }
    }

    public void saveLocationsDataDelegated() {
        OxygenHelperClient.addIOTaskClient(new IOxygenTask() {

            @Override
            public void execute() {
                saveLocationsData();
            }
        });
    }

    public void saveLocationsData() {
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
            this.manager.getWorldProfile().write(bos);
        } catch (IOException exception) {
            TeleportationMain.LOGGER.error("Locations client data saving failed.");
            exception.printStackTrace();
        }
    }
}
