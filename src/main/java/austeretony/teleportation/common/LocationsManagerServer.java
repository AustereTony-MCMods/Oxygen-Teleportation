package austeretony.teleportation.common;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen.common.reference.CommonReference;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumChatMessages;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;

public class LocationsManagerServer {

    private final TeleportationManagerServer manager;

    public LocationsManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    //TODO moveToLocation()
    public void moveToLocation(EntityPlayerMP playerMP, long pointId) {
        if (TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue()) {
            if (this.locationExist(pointId)) { 
                UUID playerUUID = CommonReference.uuid(playerMP);
                WorldPoint worldPoint = this.getLocation(pointId);
                if (this.locationAvailable(worldPoint, playerUUID) && !this.teleporting(playerUUID) && this.readyMoveToLocation(playerUUID)) {
                    if (!PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                            && playerMP.dimension != worldPoint.getDimensionId()) {
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
    }

    //TODO createLocation()
    public void createLocation(EntityPlayerMP playerMP, long pointId, String name, String description) {
        if (TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue()) {
            UUID playerUUID = CommonReference.uuid(playerMP);
            if (this.canCreateLocation(playerUUID) && this.manager.getWorldProfile().getLocationsAmount() < TeleportationConfig.LOCATIONS_MAX_AMOUNT.getIntValue()) {
                WorldPoint worldPoint = new WorldPoint(
                        CommonReference.uuid(playerMP),
                        CommonReference.username(playerMP), 
                        name, 
                        description,
                        playerMP.dimension,
                        (float) playerMP.posX, 
                        (float) playerMP.posY, 
                        (float) playerMP.posZ,
                        playerMP.rotationYawHead, 
                        playerMP.rotationPitch);
                worldPoint.setId(pointId);
                this.manager.getWorldProfile().addLocation(worldPoint);
                this.manager.getLocationsLoader().saveLocationsDataDelegated();
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.LOCATION_CREATED.ordinal(), worldPoint.getName());
            }
        }
    }

    //TODO removeLocation()
    public void removeLocation(EntityPlayerMP playerMP, long pointId) {
        if (this.locationExist(pointId)) {
            UUID playerUUID = CommonReference.uuid(playerMP);
            WorldPoint worldPoint = this.getLocation(pointId);
            if (this.canEditLocation(playerUUID, worldPoint)) {
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.LOCATION_REMOVED.ordinal(), worldPoint.getName());
                this.manager.getWorldProfile().removeLocation(pointId);
                this.manager.getLocationsLoader().saveLocationsDataDelegated();
                this.manager.getImagesLoader().removeLocationPreviewImageDelegated(pointId);
            }
        }
    }

    //TODO lockLocation()
    public void lockLocation(EntityPlayerMP playerMP, long oldPointId, boolean flag) {
        if (this.locationExist(oldPointId)) {
            UUID playerUUID = CommonReference.uuid(playerMP);
            WorldPoint worldPoint = this.getLocation(oldPointId);
            if (this.canEditLocation(playerUUID, worldPoint)) {
                worldPoint.setLocked(flag);
                worldPoint.setId(oldPointId + 1L);
                this.manager.getWorldProfile().addLocation(worldPoint);
                this.manager.getWorldProfile().removeLocation(oldPointId);
                this.manager.getLocationsLoader().saveLocationsDataDelegated();
                this.manager.getImagesLoader().renameLocationPreviewImageDelegated(oldPointId, worldPoint.getId());
                this.manager.getImagesManager().replaceImageBytes(oldPointId, worldPoint.getId());
                if (flag)
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.LOCATION_LOCKED.ordinal(), worldPoint.getName());
                else
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.LOCATION_UNLOCKED.ordinal(), worldPoint.getName());
            }
        }
    }

    //TODO editLocation()
    public void editLocation(EntityPlayerMP playerMP, long oldPointId, String name, String description, boolean updateName, 
            boolean updateDescription, boolean updateImage, boolean updatePosition) {
        if (this.locationExist(oldPointId)) { 
            UUID playerUUID = CommonReference.uuid(playerMP);
            WorldPoint worldPoint = this.getLocation(oldPointId);
            if (this.canEditLocation(playerUUID, worldPoint)) {
                long newPointId = oldPointId + 1L;
                boolean edited = false;
                if (updateName)
                    worldPoint.setName(name);
                if (updateDescription)
                    worldPoint.setDescription(description);
                if (updateImage)
                    this.manager.getImagesLoader().removeLocationPreviewImageDelegated(oldPointId);
                if (updatePosition)
                    worldPoint.setPosition(playerMP.rotationYaw, playerMP.rotationPitch, (float) playerMP.posX, (float) playerMP.posY, (float) playerMP.posZ, playerMP.dimension);
                edited = updateName || updateDescription || updateImage || updatePosition;
                if (edited) {
                    worldPoint.setId(newPointId);
                    this.manager.getWorldProfile().addLocation(worldPoint);
                    this.manager.getWorldProfile().removeLocation(oldPointId);
                    this.manager.getLocationsLoader().saveLocationsDataDelegated();
                    if (!updateImage) {
                        this.manager.getImagesLoader().renameLocationPreviewImageDelegated(oldPointId, newPointId);
                        this.manager.getImagesManager().replaceImageBytes(oldPointId, newPointId);
                    }
                }
            }
        }
    }

    private boolean canCreateLocation(UUID playerUUID) {
        return PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.LOCATIONS_CREATION.toString(), false);
    }

    private boolean locationExist(long pointId) {
        return this.manager.getWorldProfile().locationExist(pointId);
    }

    private WorldPoint getLocation(long pointId) {
        return this.manager.getWorldProfile().getLocation(pointId);
    }

    private boolean locationAvailable(WorldPoint worldPoint, UUID playerUUID) {
        return !worldPoint.isLocked() 
                || PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.ENABLE_MOVE_TO_LOCKED_LOCATIONS.toString(), false) 
                || worldPoint.isOwner(playerUUID);
    }

    private boolean canEditLocation(UUID playerUUID, WorldPoint worldPoint) {
        return PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.LOCATIONS_EDITING.toString(), false) 
                || worldPoint.isOwner(playerUUID);
    }

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToLocation(UUID playerUUID) {
        return System.currentTimeMillis() - this.manager.getPlayerProfile(playerUUID).getCooldownInfo().getLastLocationTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.LOCATION_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.LOCATIONS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}
