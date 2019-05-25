package austeretony.oxygen_teleportation.common.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class SimpleTeleporter extends Teleporter {

    private final WorldServer worldServer;

    private double xPos, yPos, zPos;

    public SimpleTeleporter(WorldServer worldServer, double x, double y, double z) {
        super(worldServer);
        this.worldServer = worldServer;
        this.xPos = x;
        this.yPos = y;
        this.zPos = z;
    }

    @Override
    public void placeInPortal(Entity entity, float rotationYaw) {
        this.worldServer.getBlockState(new BlockPos((int) this.xPos, (int) this.yPos, (int) this.zPos)); 
        entity.setPosition(this.xPos, this.yPos, this.zPos);
        entity.motionX = entity.motionY = entity.motionZ = 0.0D;
    }

    public static void transferToDimension(EntityPlayerMP playerMP, int dimension, double x, double y, double z) {
        int currDimension = playerMP.dimension;
        WorldServer worldServer = playerMP.getEntityWorld().getMinecraftServer().getWorld(currDimension);
        worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(playerMP, dimension, new SimpleTeleporter(worldServer, x, y, z));
        playerMP.setPositionAndUpdate(x, y, z);
        if (currDimension == 1) {
            worldServer.spawnEntity(playerMP);
            worldServer.updateEntityWithOptionalForce(playerMP, false);
        }
    }
}
