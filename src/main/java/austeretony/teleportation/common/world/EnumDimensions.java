package austeretony.teleportation.common.world;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum EnumDimensions {

    OVERWORLD(0, "teleportation.dim.overworld"),
    NETHER(- 1, "teleportation.dim.nether"),
    THE_END(1, "teleportation.dim.end"),
    TWILIGHT_FOREST(7, "teleportation.dim.twilightForest");

    public final int dimId;

    public final String nameKey;

    EnumDimensions(int dimId, String nameKey) {
        this.dimId = dimId;
        this.nameKey = nameKey;
    }

    public static String getNameKeyFromId(int dimId) {
        String nameKey = "teleportation.dim.unknown";
        for (EnumDimensions names : values())
            if (dimId == names.dimId)
                nameKey = names.nameKey;
        return nameKey;
    }

    @SideOnly(Side.CLIENT)
    public static String getLocalizedNameFromId(int dimId) {
        String nameKey = "teleportation.dim.unknown";
        for (EnumDimensions names : values())
            if (dimId == names.dimId)
                nameKey = names.nameKey;
        return I18n.format(nameKey);
    }
}
