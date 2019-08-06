package austeretony.oxygen_teleportation.common;

import java.util.UUID;

import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationChatMessage;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationProcess;
import austeretony.oxygen_teleportation.common.main.WorldPoint;
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
                UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
                WorldPoint worldPoint = this.getLocation(pointId);
                if (this.locationAvailable(worldPoint, playerUUID) && !this.teleporting(playerUUID) && this.readyMoveToLocation(playerUUID)) {
                    if (!PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                            && playerMP.dimension != worldPoint.getDimensionId()) {
                        OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
                        return;
                    }
                    int delay = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.LOCATION_TELEPORTATION_DELAY.toString(), TeleportationConfig.LOCATIONS_TELEPORT_DELAY.getIntValue());
                    if (delay < 1)
                        delay = 1;
                    if (delay > 1)
                        OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.PREPARE_FOR_TELEPORTATION.ordinal(), String.valueOf(delay));
                    TeleportationProcess.create(TeleportationProcess.EnumTeleportation.LOCATION, playerMP, pointId, delay);    
                }
            }
        }
    }

    //TODO createLocation()
    public void createLocation(EntityPlayerMP playerMP, long pointId, String name, String description) {
        if (TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue()) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            if (this.canCreateLocation(playerUUID) && this.manager.getWorldData().getLocationsAmount() < TeleportationConfig.LOCATIONS_MAX_AMOUNT.getIntValue()) {
                WorldPoint worldPoint = new WorldPoint(
                        System.currentTimeMillis(),
                        CommonReference.getPersistentUUID(playerMP),
                        CommonReference.getName(playerMP), 
                        name, 
                        description,
                        playerMP.dimension,
                        (float) playerMP.posX, 
                        (float) playerMP.posY, 
                        (float) playerMP.posZ,
                        playerMP.rotationYawHead, 
                        playerMP.rotationPitch);
                worldPoint.setId(pointId);
                this.manager.getWorldData().addLocation(worldPoint);
                TeleportationLoaderServer.savePersistentDataDelegated(this.manager.getWorldData());
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.LOCATION_CREATED.ordinal(), worldPoint.getName());
            }
        }
    }

    //TODO removeLocation()
    public void removeLocation(EntityPlayerMP playerMP, long pointId) {
        if (this.locationExist(pointId)) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            WorldPoint worldPoint = this.getLocation(pointId);
            if (this.canEditLocation(playerUUID, worldPoint)) {
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.LOCATION_REMOVED.ordinal(), worldPoint.getName());
                this.manager.getWorldData().removeLocation(pointId);
                TeleportationLoaderServer.savePersistentDataDelegated(this.manager.getWorldData());
                this.manager.getImagesLoader().removeLocationPreviewImageDelegated(pointId);
            }
        }
    }

    //TODO lockLocation()
    public void lockLocation(EntityPlayerMP playerMP, long oldPointId, boolean flag) {
        if (this.locationExist(oldPointId)) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            WorldPoint worldPoint = this.getLocation(oldPointId);
            if (this.canEditLocation(playerUUID, worldPoint)) {
                worldPoint.setLocked(flag);
                worldPoint.setId(oldPointId + 1L);
                this.manager.getWorldData().addLocation(worldPoint);
                this.manager.getWorldData().removeLocation(oldPointId);
                TeleportationLoaderServer.savePersistentDataDelegated(this.manager.getWorldData());
                this.manager.getImagesLoader().renameLocationPreviewImageDelegated(oldPointId, worldPoint.getId());
                this.manager.getImagesManager().replaceImageBytes(oldPointId, worldPoint.getId());
                if (flag)
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.LOCATION_LOCKED.ordinal(), worldPoint.getName());
                else
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationChatMessage.LOCATION_UNLOCKED.ordinal(), worldPoint.getName());
            }
        }
    }

    //TODO editLocation()
    public void editLocation(EntityPlayerMP playerMP, long oldPointId, String name, String description, boolean updateName, 
            boolean updateDescription, boolean updateImage, boolean updatePosition) {
        if (this.locationExist(oldPointId)) { 
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
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
                    this.manager.getWorldData().addLocation(worldPoint);
                    this.manager.getWorldData().removeLocation(oldPointId);
                    TeleportationLoaderServer.savePersistentDataDelegated(this.manager.getWorldData());
                    if (!updateImage) {
                        this.manager.getImagesLoader().renameLocationPreviewImageDelegated(oldPointId, newPointId);
                        this.manager.getImagesManager().replaceImageBytes(oldPointId, newPointId);
                    }
                }
            }
        }
    }

    private boolean canCreateLocation(UUID playerUUID) {
        return TeleportationConfig.ALLOW_LOCATIONS_CREATION_FOR_ALL.getBooleanValue() 
                || PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.LOCATIONS_CREATION.toString(), false) 
                || PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false);
    }

    private boolean locationExist(long pointId) {
        return this.manager.getWorldData().locationExist(pointId);
    }

    private WorldPoint getLocation(long pointId) {
        return this.manager.getWorldData().getLocation(pointId);
    }

    private boolean locationAvailable(WorldPoint worldPoint, UUID playerUUID) {
        return !worldPoint.isLocked() 
                || PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.ENABLE_MOVE_TO_LOCKED_LOCATIONS.toString(), false) 
                || worldPoint.isOwner(playerUUID);
    }

    private boolean canEditLocation(UUID playerUUID, WorldPoint worldPoint) {
        return PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false) 
                || worldPoint.isOwner(playerUUID);
    }

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToLocation(UUID playerUUID) {
        return System.currentTimeMillis() - this.manager.getPlayerData(playerUUID).getCooldownInfo().getLastLocationTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.LOCATIONS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}
