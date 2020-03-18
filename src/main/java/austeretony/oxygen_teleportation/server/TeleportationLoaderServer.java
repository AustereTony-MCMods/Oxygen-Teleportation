package austeretony.oxygen_teleportation.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.JsonUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TeleportationLoaderServer {

    public static void loadFeeItemStackAsync() {
        OxygenHelperServer.addIOTask(()->{
            String folder = OxygenHelperCommon.getConfigFolder() + "data/server/teleportation/fee_itemstack.json";
            Path path = Paths.get(folder);
            if (Files.exists(path)) {
                try {    
                    TeleportationManagerServer.instance().setFeeStack(
                            ItemStackWrapper.fromJson(JsonUtils.getExternalJsonData(folder).getAsJsonObject()));
                } catch (IOException exception) {
                    OxygenMain.LOGGER.error("[Teleportation] Fee stack data loading failed! Path: {}", folder);
                    exception.printStackTrace();
                }
            } else
                TeleportationManagerServer.instance().setFeeStack(ItemStackWrapper.of(new ItemStack(Items.EMERALD)));
        });
    }

    public static void saveFeeItemStackAsync() {
        OxygenHelperServer.addIOTask(()->{
            String folder = OxygenHelperCommon.getConfigFolder() + "data/server/teleportation/fee_itemstack.json";
            Path path = Paths.get(folder);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path.getParent());
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
            try {   
                JsonUtils.createExternalJsonFile(folder, TeleportationManagerServer.instance().getFeeStackWrapper().toJson());
            } catch (IOException exception) {
                OxygenMain.LOGGER.error("[Teleportation] Fee stack data saving failed! Path: {}", folder);
                exception.printStackTrace();
            }
        });
    }
}
