package austeretony.oxygen_teleportation.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TeleportationLoaderServer {

    public static void loadFeeItemStackDelegated() {
        OxygenHelperServer.addIOTask(()->{
            String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/fee_stack.dat";
            Path path = Paths.get(folder);
            if (Files.exists(path)) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(folder))) {    
                    TeleportationManagerServer.instance().setFeeStack(ItemStackWrapper.read(bis));
                } catch (IOException exception) {
                    TeleportationMain.LOGGER.error("Server fee stack data loading failed! Path: {}", folder);
                    exception.printStackTrace();
                }
            } else
                TeleportationManagerServer.instance().setFeeStack(ItemStackWrapper.getFromStack(new ItemStack(Items.EMERALD)));
        });
    }

    public static void saveFeeItemStackDelegated() {
        OxygenHelperServer.addIOTask(()->{
            String folder = OxygenHelperServer.getDataFolder() + "/server/world/teleportation/fee_stack.dat";
            Path path = Paths.get(folder);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path.getParent());
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder))) {   
                TeleportationManagerServer.instance().getFeeStackWrapper().write(bos);
            } catch (IOException exception) {
                TeleportationMain.LOGGER.error("Server fee stack data saving failed! Path: {}", folder);
                exception.printStackTrace();
            }
        });
    }
}