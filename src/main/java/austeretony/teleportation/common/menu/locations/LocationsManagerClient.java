package austeretony.teleportation.common.menu.locations;

import java.awt.image.BufferedImage;
import java.util.List;

import austeretony.oxygen.client.reference.ClientReference;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.api.OxygenTask;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.WorldProfile;
import austeretony.teleportation.common.menu.camps.CampsManagerClient;
import austeretony.teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.teleportation.common.network.server.SPEditWorldPoint;
import austeretony.teleportation.common.network.server.SPLockPoint;
import austeretony.teleportation.common.network.server.SPMoveToPoint;
import austeretony.teleportation.common.network.server.SPRemoveWorldPoint;
import austeretony.teleportation.common.network.server.SPStartImageUpload;
import austeretony.teleportation.common.network.server.SPUploadImagePart;
import austeretony.teleportation.common.util.BufferedImageUtils;
import austeretony.teleportation.common.util.ImageTransferingServerBuffer;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LocationsManagerClient {

    private static LocationsManagerClient instance;

    private final WorldProfile worldProfile = new WorldProfile();

    private LocationsManagerClient() {}

    public static void create() {
        instance = new LocationsManagerClient();
        LocationsLoaderClient.loadLocationsDataDelegated();
    }

    public static LocationsManagerClient instance() {
        return instance;
    }

    public WorldProfile getWorldProfile() {
        return this.worldProfile;
    }

    //TODO downloadLocationsDataSynced()
    public void downloadLocationsDataSynced() {
        this.worldProfile.getLocations().clear();
        CampsManagerClient.instance().openMenuSynced();
    }

    //TODO moveToLocationSynced()
    public void moveToLocationSynced(long id) {        
        if (id != 0L) {
            TeleportationMain.network().sendToServer(new SPMoveToPoint(WorldPoint.EnumWorldPoints.LOCATION, id));
            CampsManagerClient.instance().setTeleportationDelay(PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.LOCATION_TELEPORTATION_DELAY.toString(), TeleportationConfig.LOCATIONS_TELEPORT_DELAY.getIntValue()));
        }
    }

    //TODO setLocationPointSynced()
    public void setLocationPointSynced(WorldPoint worldPoint) {
        if (this.canCreateLocation() && this.worldProfile.getLocationsAmount() < TeleportationConfig.LOCATIONS_MAX_AMOUNT.getIntValue()) {
            this.worldProfile.addLocation(worldPoint);
            TeleportationMain.network().sendToServer(new SPCreateWorldPoint(WorldPoint.EnumWorldPoints.LOCATION, worldPoint));
            CampsManagerClient.instance().getPreviewImages().put(worldPoint.getId(), CampsManagerClient.instance().getLatestImage());
            LocationsLoaderClient.saveLocationsDataDelegated();
            LocationsLoaderClient.saveLocationPreviewImageDelegated(worldPoint.getId(), CampsManagerClient.instance().getLatestImage());
            this.uploadLocationPreviewToServerDelegated(worldPoint.getId(), CampsManagerClient.instance().getLatestImage());
        }
    }  

    public void uploadLocationPreviewToServerDelegated(long pointId, BufferedImage bufferedImage) {
        OxygenHelperClient.addRoutineTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                uploadLocationPreviewToServer(pointId, bufferedImage);
            }  
        });
    }

    public void uploadLocationPreviewToServer(long pointId, BufferedImage bufferedImage) {
        List<int[]> imageParts = BufferedImageUtils.convertBufferedImageToIntArraysList(bufferedImage);
        TeleportationMain.network().sendToServer(new SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_LOCATION, pointId, imageParts.size()));  
        int index = 0;
        for (int[] part : imageParts) {
            TeleportationMain.network().sendToServer(new SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_LOCATION, pointId, index, part, imageParts.size()));
            index++;
        }
    }

    //TODO removeLocationPointSynced()
    public void removeLocationPointSynced(long pointId) {
        this.worldProfile.removeLocation(pointId);
        TeleportationMain.network().sendToServer(new SPRemoveWorldPoint(WorldPoint.EnumWorldPoints.LOCATION, pointId));
        LocationsLoaderClient.saveLocationsDataDelegated();      
        CampsManagerClient.instance().getPreviewImages().remove(pointId);
    }

    //TODO lockLocationSynced()
    public void lockLocationSynced(WorldPoint worldPoint, boolean flag) {    
        long oldPointId = worldPoint.getId();
        worldPoint.setLocked(flag);
        worldPoint.setId(worldPoint.getId() + 1L);
        this.worldProfile.addLocation(worldPoint);
        this.worldProfile.removeLocation(oldPointId);
        LocationsLoaderClient.saveLocationsDataDelegated();      
        TeleportationMain.network().sendToServer(new SPLockPoint(WorldPoint.EnumWorldPoints.LOCATION, oldPointId, flag));
        LocationsLoaderClient.renameLocationPreviewImageDelegated(oldPointId, worldPoint.getId());
        CampsManagerClient.instance().replaceImage(oldPointId, worldPoint.getId());
    }

    //TODO editLocationPointSynced()
    public void editLocationPointSynced(WorldPoint worldPoint, String newName, String newDescription, boolean updateImage, boolean updatePosition) {
        long 
        oldPointId = worldPoint.getId(),
        newPointId =  oldPointId + 1L;
        boolean 
        edited = false,
        updateName = false,
        updateDescription = false;
        if (!newName.equals(worldPoint.getName())) {
            updateName = true;
            worldPoint.setName(newName);
        }
        if (!newDescription.equals(worldPoint.getDescription())) {
            updateDescription = true;
            worldPoint.setDescription(newDescription);
        }
        if (updateImage) {
            CampsManagerClient.instance().getPreviewImages().put(newPointId, CampsManagerClient.instance().getLatestImage());
            CampsManagerClient.instance().getPreviewImages().remove(oldPointId);
            LocationsLoaderClient.saveLocationPreviewImageDelegated(worldPoint.getId(), CampsManagerClient.instance().getLatestImage());
            this.uploadLocationPreviewToServerDelegated(newPointId, CampsManagerClient.instance().getLatestImage());
        }
        if (updatePosition) {
            EntityPlayer player = ClientReference.getClientPlayer();
            worldPoint.setPosition(player.rotationYaw, player.rotationPitch, (float) player.posX, (float) player.posY, (float) player.posZ, player.dimension);

        }
        edited = updateName || updateDescription || updateImage || updatePosition;
        if (edited) {
            worldPoint.setId(newPointId);
            this.worldProfile.addLocation(worldPoint);
            this.worldProfile.removeLocation(oldPointId);
            LocationsLoaderClient.saveLocationsDataDelegated();
            TeleportationMain.network().sendToServer(new SPEditWorldPoint(WorldPoint.EnumWorldPoints.LOCATION, oldPointId, newName, newDescription, 
                    updateName, updateDescription, updateImage, updatePosition));
            if (!updateImage) {
                LocationsLoaderClient.renameLocationPreviewImageDelegated(oldPointId, newPointId);
                CampsManagerClient.instance().replaceImage(oldPointId, newPointId);
            }
        }
    }

    private boolean canCreateLocation() {
        return CampsManagerClient.instance().isOpped() || PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.LOCATIONS_CREATION.toString(), false);
    }
}
