package austeretony.oxygen_teleportation.server;

import java.util.UUID;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationStatusMessage;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.network.client.CPWorldPointCreated;
import austeretony.oxygen_teleportation.common.network.client.CPWorldPointEdited;
import austeretony.oxygen_teleportation.common.network.client.CPWorldPointRemoved;
import net.minecraft.entity.player.EntityPlayerMP;

public class LocationsManagerServer {

    private final TeleportationManagerServer manager;

    protected LocationsManagerServer(TeleportationManagerServer manager) {
        this.manager = manager;
    }

    public void moveToLocation(EntityPlayerMP playerMP, long pointId) {
        if (TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue()) {
            if (this.locationExist(pointId)) { 
                UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
                WorldPoint worldPoint = this.getLocation(pointId);
                if (this.locationAvailable(worldPoint, playerUUID) 
                        && !this.manager.getPlayersDataManager().isPlayerTeleporting(playerUUID) 
                        && this.readyMoveToLocation(playerUUID)) {
                    if (!PrivilegeProviderServer.getValue(playerUUID, EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                            && playerMP.dimension != worldPoint.getDimensionId()) {
                        OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
                        return;
                    }
                    this.manager.getPlayersDataManager().addTeleportation(new LocationTeleportation(playerMP, worldPoint));
                }
            }
        }
    }

    public void createLocation(EntityPlayerMP playerMP, String name, String description) {
        if (TeleportationConfig.ENABLE_LOCATIONS.getBooleanValue()) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            if (this.canCreateLocation(playerUUID) 
                    && this.manager.getLocationsContainer().getLocationsAmount() < TeleportationConfig.LOCATIONS_MAX_AMOUNT.getIntValue()) {
                if (name.isEmpty())
                    name = String.format("Location #%d", this.manager.getLocationsContainer().getLocationsAmount() + 1);
                name = name.trim();
                description = description.trim();
                if (name.length() > WorldPoint.MAX_NAME_LENGTH)
                    name = name.substring(0, WorldPoint.MAX_NAME_LENGTH);
                if (description.length() > WorldPoint.MAX_DESCRIPTION_LENGTH)
                    description = description.substring(0, WorldPoint.MAX_NAME_LENGTH);
                WorldPoint worldPoint = new WorldPoint(
                        this.manager.getLocationsContainer().createId(System.currentTimeMillis()),
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
                this.manager.getLocationsContainer().addLocation(worldPoint);
                this.manager.getLocationsContainer().setChanged(true);

                OxygenMain.network().sendTo(new CPWorldPointCreated(EnumWorldPoint.LOCATION, worldPoint), playerMP);

                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_CREATED.ordinal());
            }
        }
    }

    public void removeLocation(EntityPlayerMP playerMP, long pointId) {
        if (this.locationExist(pointId)) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            WorldPoint worldPoint = this.getLocation(pointId);
            if (this.canEditLocation(playerUUID, worldPoint)) {
                this.manager.getLocationsContainer().removeLocation(pointId);
                this.manager.getLocationsContainer().setChanged(true);
                this.manager.getImagesLoader().removeLocationPreviewImageAsync(pointId);

                OxygenMain.network().sendTo(new CPWorldPointRemoved(EnumWorldPoint.LOCATION, worldPoint.getId()), playerMP);

                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_REMOVED.ordinal());
            }
        }
    }

    public void changeLocationLockState(EntityPlayerMP playerMP, long pointId, boolean flag) {
        if (this.locationExist(pointId)) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            WorldPoint worldPoint = this.getLocation(pointId);
            if (this.canEditLocation(playerUUID, worldPoint)) {
                worldPoint.setLocked(flag);
                worldPoint.setId(this.manager.getLocationsContainer().createId(pointId));
                this.manager.getLocationsContainer().addLocation(worldPoint);
                this.manager.getLocationsContainer().removeLocation(pointId);
                this.manager.getLocationsContainer().setChanged(true);
                this.manager.getImagesLoader().renameLocationPreviewImageAsync(pointId, worldPoint.getId());
                this.manager.getImagesManager().replaceImageBytes(pointId, worldPoint.getId());

                OxygenMain.network().sendTo(new CPWorldPointEdited(EnumWorldPoint.LOCATION, pointId, worldPoint, false), playerMP);

                if (flag)
                    OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_LOCKED.ordinal());
                else
                    OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_UNLOCKED.ordinal());
            }
        }
    }

    public void editLocation(EntityPlayerMP playerMP, long pointId, String name, String description, boolean updatePosition, boolean updateImage) {
        if (this.locationExist(pointId)) { 
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            WorldPoint worldPoint = this.getLocation(pointId);
            if (this.canEditLocation(playerUUID, worldPoint)) {
                long newPointId = this.manager.getLocationsContainer().createId(pointId);
                if (name.isEmpty())
                    name = "Location";
                name = name.trim();
                description = description.trim();
                if (name.length() > WorldPoint.MAX_NAME_LENGTH)
                    name = name.substring(0, WorldPoint.MAX_NAME_LENGTH);
                if (description.length() > WorldPoint.MAX_DESCRIPTION_LENGTH)
                    description = description.substring(0, WorldPoint.MAX_NAME_LENGTH);
                worldPoint.setName(name);
                worldPoint.setDescription(description);
                if (updatePosition)
                    worldPoint.setPosition((float) playerMP.posX, (float) playerMP.posY, (float) playerMP.posZ, playerMP.dimension, playerMP.rotationYawHead, playerMP.rotationPitch);
                if (updateImage)
                    this.manager.getImagesLoader().removeLocationPreviewImageAsync(pointId);
                worldPoint.setId(newPointId);
                this.manager.getLocationsContainer().addLocation(worldPoint);
                this.manager.getLocationsContainer().removeLocation(pointId);
                this.manager.getLocationsContainer().setChanged(true);
                if (!updateImage) {
                    this.manager.getImagesLoader().renameLocationPreviewImageAsync(pointId, newPointId);
                    this.manager.getImagesManager().replaceImageBytes(pointId, newPointId);
                }

                OxygenMain.network().sendTo(new CPWorldPointEdited(EnumWorldPoint.LOCATION, pointId, worldPoint, updateImage), playerMP);

                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_EDITED.ordinal());
            }
        }
    }

    private boolean canCreateLocation(UUID playerUUID) {
        return TeleportationConfig.ALLOW_LOCATIONS_CREATION_FOR_ALL.getBooleanValue() 
                || PrivilegeProviderServer.getValue(playerUUID, EnumTeleportationPrivilege.LOCATIONS_CREATION.toString(), false) 
                || PrivilegeProviderServer.getValue(playerUUID, EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false);
    }

    private boolean locationExist(long pointId) {
        return this.manager.getLocationsContainer().locationExist(pointId);
    }

    private WorldPoint getLocation(long pointId) {
        return this.manager.getLocationsContainer().getLocation(pointId);
    }

    private boolean locationAvailable(WorldPoint worldPoint, UUID playerUUID) {
        return !worldPoint.isLocked() 
                || PrivilegeProviderServer.getValue(playerUUID, EnumTeleportationPrivilege.ENABLE_MOVE_TO_LOCKED_LOCATIONS.toString(), false) 
                || worldPoint.isOwner(playerUUID);
    }

    private boolean canEditLocation(UUID playerUUID, WorldPoint worldPoint) {
        return PrivilegeProviderServer.getValue(playerUUID, EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false) 
                || worldPoint.isOwner(playerUUID);
    }

    private boolean readyMoveToLocation(UUID playerUUID) {
        return System.currentTimeMillis() >= this.manager.getPlayersDataContainer().getPlayerData(playerUUID).getCooldownData().getNextLocationTime();
    }
}
