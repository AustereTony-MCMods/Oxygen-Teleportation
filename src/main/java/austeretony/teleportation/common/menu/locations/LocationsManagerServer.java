package austeretony.teleportation.common.menu.locations;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.OxygenTask;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumChatMessages;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.main.WorldProfile;
import austeretony.teleportation.common.menu.camps.CampsManagerServer;
import austeretony.teleportation.common.network.client.CPDownloadImagePart;
import austeretony.teleportation.common.network.client.CPStartImageDownload;
import austeretony.teleportation.common.util.ImageTransferingClientBuffer;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;

public class LocationsManagerServer {

    private static LocationsManagerServer instance;

    private final WorldProfile worldProfile = new WorldProfile();

    private Map<Long, SplittedByteArray> imagesBytes = new ConcurrentHashMap<Long, SplittedByteArray>();

    private LocationsManagerServer() {}

    public static void create() {
        instance = new LocationsManagerServer();
        LocationsLoaderServer.loadLocationsDataDelegated();
    }

    public static LocationsManagerServer instance() {
        return instance;
    }

    public WorldProfile getWorldProfile() {
        return this.worldProfile;
    }  

    public Map<Long, SplittedByteArray> getLocationPreviewBytes() {
        return this.imagesBytes;
    }

    public void replaceImageBytes(long oldPointId, long newPointId) {
        if (this.imagesBytes.containsKey(oldPointId)) {
            this.imagesBytes.put(newPointId, this.imagesBytes.get(oldPointId));
            this.imagesBytes.remove(oldPointId);
        }
    }

    //TODO moveToLocation()
    public void moveToLocation(EntityPlayerMP playerMP, long pointId) {
        if (TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue()) {
            UUID playerUUID = OxygenHelperServer.uuid(playerMP);
            if (this.locationExist(pointId) && this.locationAvailable(pointId, playerMP, playerUUID) && !this.teleporting(playerUUID) && this.readyMoveToLocation(playerUUID)) {
                if (!PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                        && playerMP.dimension != this.worldProfile.getLocation(pointId).getDimensionId()) {
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
                    return;
                }
                int delay = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.LOCATION_TELEPORTATION_DELAY.toString(), TeleportationConfig.LOCATIONS_TELEPORT_DELAY.getIntValue());
                if (delay < 1)
                    delay = 1;
                if (delay > 1)
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.PREPARE_FOR_TELEPORTATION.ordinal(), String.valueOf(delay));
                TeleportationProcess.create(TeleportationProcess.EnumTeleportations.LOCATION, playerMP, pointId, delay);    
            }
        }
    }

    //TODO createLocation()
    public void createLocation(EntityPlayerMP playerMP, long pointId, String name, String description) {
        if (TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue()) {
            if (this.canCreateLocation(playerMP) && this.worldProfile.getLocationsAmount() < TeleportationConfig.LOCATIONS_MAX_AMOUNT.getIntValue()) {
                WorldPoint worldPoint = new WorldPoint(
                        OxygenHelperServer.uuid(playerMP),
                        OxygenHelperServer.username(playerMP), 
                        name, 
                        description,
                        playerMP.dimension,
                        (float) playerMP.posX, 
                        (float) playerMP.posY, 
                        (float) playerMP.posZ,
                        playerMP.rotationYawHead, 
                        playerMP.rotationPitch);
                worldPoint.setId(pointId);
                this.worldProfile.addLocation(worldPoint);
                LocationsLoaderServer.saveLocationsDataDelegated();
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.LOCATION_CREATED.ordinal(), worldPoint.getName());
            }
        }
    }

    public void downloadLocationPreviewsToClientDelegated(EntityPlayerMP playerMP, long[] locationIds) {
        OxygenHelperServer.addRoutineTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                for (long id : locationIds)
                    downloadLocationPreviewToClient(playerMP, id);
            }  
        });
    }

    public void downloadLocationPreviewToClient(EntityPlayerMP playerMP, long pointId) {
        if (this.getLocationPreviewBytes().containsKey(pointId)) {
            List<byte[]> imageParts = this.getLocationPreviewBytes().get(pointId).getParts();
            TeleportationMain.network().sendTo(new CPStartImageDownload(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_CAMP, pointId, imageParts.size()), playerMP);  
            int index = 0;
            for (byte[] part : imageParts) {
                TeleportationMain.network().sendTo(new CPDownloadImagePart(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_CAMP, pointId, index, part, imageParts.size()), playerMP);
                index++;
            }
        } else 
            TeleportationMain.LOGGER.error("Location preview image {}.png bytes are absent, can't download image.", pointId);
    }

    //TODO removeLocation()
    public void removeLocation(EntityPlayerMP playerMP, long pointId) {
        if (this.locationExist(pointId) && this.canEditLocation(playerMP, pointId)) {
            OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.LOCATION_REMOVED.ordinal(), this.worldProfile.getLocation(pointId).getName());
            this.worldProfile.removeLocation(pointId);
            LocationsLoaderServer.saveLocationsDataDelegated();
            LocationsLoaderServer.removeLocationPreviewImageDelegated(pointId);
        }
    }

    //TODO lockLocation()
    public void lockLocation(EntityPlayerMP playerMP, long oldPointId, boolean flag) {
        UUID playerUUID = OxygenHelperServer.uuid(playerMP);
        if (this.locationExist(oldPointId) && this.canEditLocation(playerMP, oldPointId)) {
            WorldPoint worldPoint = this.worldProfile.getLocation(oldPointId);
            worldPoint.setLocked(flag);
            worldPoint.setId(oldPointId + 1L);
            this.worldProfile.addLocation(worldPoint);
            this.worldProfile.removeLocation(oldPointId);
            LocationsLoaderServer.saveLocationsDataDelegated();
            LocationsLoaderServer.renameLocationPreviewImageDelegated(oldPointId, worldPoint.getId());
            this.replaceImageBytes(oldPointId, worldPoint.getId());
            if (flag)
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.LOCATION_LOCKED.ordinal(), worldPoint.getName());
            else
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.LOCATION_UNLOCKED.ordinal(), worldPoint.getName());
        }
    }

    //TODO editLocation()
    public void editLocation(EntityPlayerMP playerMP, long oldPointId, String name, String description, boolean updateName, 
            boolean updateDescription, boolean updateImage, boolean updatePosition) {
        if (this.locationExist(oldPointId) && this.canEditLocation(playerMP, oldPointId)) {
            WorldPoint worldPoint = this.worldProfile.getLocation(oldPointId);
            long newPointId = oldPointId + 1L;
            boolean edited = false;
            if (updateName)
                worldPoint.setName(name);
            if (updateDescription)
                worldPoint.setDescription(description);
            if (updateImage)
                LocationsLoaderServer.removeLocationPreviewImageDelegated(oldPointId);
            if (updatePosition)
                worldPoint.setPosition(playerMP.rotationYaw, playerMP.rotationPitch, (float) playerMP.posX, (float) playerMP.posY, (float) playerMP.posZ, playerMP.dimension);
            edited = updateName || updateDescription || updateImage || updatePosition;
            if (edited) {
                worldPoint.setId(newPointId);
                this.worldProfile.addLocation(worldPoint);
                this.worldProfile.removeLocation(oldPointId);
                LocationsLoaderServer.saveLocationsDataDelegated();
                if (!updateImage) {
                    LocationsLoaderServer.renameLocationPreviewImageDelegated(oldPointId, newPointId);
                    this.replaceImageBytes(oldPointId, newPointId);
                }
            }
        }
    }

    private boolean canCreateLocation(EntityPlayerMP playerMP) {
        return CommonReference.isOpped(playerMP) || PrivilegeProviderServer.getPrivilegeValue(OxygenHelperServer.uuid(playerMP), EnumPrivileges.LOCATIONS_CREATION.toString(), false);
    }

    private boolean locationExist(long pointId) {
        return this.worldProfile.locationExist(pointId);
    }

    private boolean locationAvailable(long pointId, EntityPlayerMP playerMP, UUID playerUUID) {
        return !this.worldProfile.getLocation(pointId).isLocked() || CommonReference.isOpped(playerMP) || this.worldProfile.getLocation(pointId).isOwner(playerUUID);
    }

    private boolean canEditLocation(EntityPlayerMP playerMP, long pointId) {
        return CommonReference.isOpped(playerMP) || this.worldProfile.getLocation(pointId).isOwner(OxygenHelperServer.uuid(playerMP));
    }

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToLocation(UUID playerUUID) {
        return System.currentTimeMillis() - CampsManagerServer.instance().getPlayerProfile(playerUUID).getCooldownInfo().getLastLocationTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.LOCATION_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.LOCATIONS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}
