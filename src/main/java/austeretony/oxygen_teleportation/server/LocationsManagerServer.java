package austeretony.oxygen_teleportation.server;

import java.util.UUID;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.TimeHelperServer;
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
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_LOCATIONS_USAGE.id(), TeleportationConfig.ENABLE_LOCATIONS.asBoolean())) {
            WorldPoint location = this.getLocation(pointId);
            if (location != null
                    && this.locationAvailable(location, playerUUID) 
                    && !this.manager.getPlayersDataManager().isPlayerTeleporting(playerUUID) 
                    && this.readyMoveToLocation(playerUUID)) {
                if (!PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ENABLE_CROSS_DIM_TELEPORTATION.id(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.asBoolean())
                        && playerMP.dimension != location.getDimensionId()) {
                    OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
                    return;
                }
                this.manager.getPlayersDataManager().addTeleportation(new LocationTeleportation(playerMP, location));
            }
        }
    }

    public void createLocation(EntityPlayerMP playerMP, String name, String description) {
        if (TeleportationConfig.ENABLE_LOCATIONS.asBoolean()) {
            UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
            if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_LOCATIONS_USAGE.id(), TeleportationConfig.ENABLE_LOCATIONS.asBoolean())
                    && this.canCreateLocation(playerUUID) 
                    && this.manager.getLocationsContainer().getLocationsAmount() < TeleportationConfig.LOCATIONS_MAX_AMOUNT.asInt()) {
                name = name.trim();
                if (name.length() > WorldPoint.MAX_NAME_LENGTH)
                    name = name.substring(0, WorldPoint.MAX_NAME_LENGTH);
                if (name.isEmpty())
                    name = String.format("Location #%d", this.manager.getLocationsContainer().getLocationsAmount() + 1);

                description = description.trim();
                if (description.length() > WorldPoint.MAX_DESCRIPTION_LENGTH)
                    description = description.substring(0, WorldPoint.MAX_NAME_LENGTH);

                WorldPoint location = new WorldPoint(
                        this.manager.getLocationsContainer().createId(TimeHelperServer.getCurrentMillis()),
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
                this.manager.getLocationsContainer().addLocation(location);
                this.manager.getLocationsContainer().setChanged(true);

                OxygenMain.network().sendTo(new CPWorldPointCreated(EnumWorldPoint.LOCATION, location), playerMP);

                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_CREATED.ordinal());
            }
        }
    }

    public void removeLocation(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        WorldPoint location = this.getLocation(pointId);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_LOCATIONS_USAGE.id(), TeleportationConfig.ENABLE_LOCATIONS.asBoolean())
                && location != null
                && this.canEditLocation(playerUUID, location)) {
            this.manager.getLocationsContainer().removeLocation(pointId);
            this.manager.getLocationsContainer().setChanged(true);

            this.manager.getImagesLoader().removeLocationPreviewImageAsync(pointId);

            OxygenMain.network().sendTo(new CPWorldPointRemoved(EnumWorldPoint.LOCATION, location.getId()), playerMP);

            OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_REMOVED.ordinal());
        }
    }

    public void changeLocationLockState(EntityPlayerMP playerMP, long pointId, boolean flag) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        WorldPoint location = this.getLocation(pointId);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_LOCATIONS_USAGE.id(), TeleportationConfig.ENABLE_LOCATIONS.asBoolean())
                && location != null
                && this.canEditLocation(playerUUID, location)) {
            location.setLocked(flag);
            location.setId(this.manager.getLocationsContainer().createId(pointId));
            this.manager.getLocationsContainer().addLocation(location);
            this.manager.getLocationsContainer().removeLocation(pointId);
            this.manager.getLocationsContainer().setChanged(true);

            this.manager.getImagesLoader().renameLocationPreviewImageAsync(pointId, location.getId());
            this.manager.getImagesManager().replaceImageBytes(pointId, location.getId());

            OxygenMain.network().sendTo(new CPWorldPointEdited(EnumWorldPoint.LOCATION, pointId, location, false), playerMP);

            if (flag)
                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_LOCKED.ordinal());
            else
                OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_UNLOCKED.ordinal());
        }
    }

    public void editLocation(EntityPlayerMP playerMP, long pointId, String name, String description, boolean updatePosition, boolean updateImage) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        WorldPoint location = this.getLocation(pointId);
        if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ALLOW_LOCATIONS_USAGE.id(), TeleportationConfig.ENABLE_LOCATIONS.asBoolean())
                && location != null
                && this.canEditLocation(playerUUID, location)) {
            long newPointId = this.manager.getLocationsContainer().createId(pointId);
            name = name.trim();
            if (name.length() > WorldPoint.MAX_NAME_LENGTH)
                name = name.substring(0, WorldPoint.MAX_NAME_LENGTH);
            if (name.isEmpty())
                name = "Location";

            description = description.trim();
            if (description.length() > WorldPoint.MAX_DESCRIPTION_LENGTH)
                description = description.substring(0, WorldPoint.MAX_NAME_LENGTH);

            location.setName(name);
            location.setDescription(description);
            if (updatePosition)
                location.setPosition((float) playerMP.posX, (float) playerMP.posY, (float) playerMP.posZ, playerMP.dimension, playerMP.rotationYawHead, playerMP.rotationPitch);
            if (updateImage)
                this.manager.getImagesLoader().removeLocationPreviewImageAsync(pointId);
            location.setId(newPointId);
            this.manager.getLocationsContainer().addLocation(location);
            this.manager.getLocationsContainer().removeLocation(pointId);
            this.manager.getLocationsContainer().setChanged(true);

            if (!updateImage) {
                this.manager.getImagesLoader().renameLocationPreviewImageAsync(pointId, newPointId);
                this.manager.getImagesManager().replaceImageBytes(pointId, newPointId);
            }

            OxygenMain.network().sendTo(new CPWorldPointEdited(EnumWorldPoint.LOCATION, pointId, location, updateImage), playerMP);

            OxygenHelperServer.sendStatusMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumTeleportationStatusMessage.LOCATION_EDITED.ordinal());
        }
    }

    private boolean canCreateLocation(UUID playerUUID) {
        return TeleportationConfig.ALLOW_LOCATIONS_CREATION_FOR_ALL.asBoolean() 
                || PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.LOCATIONS_CREATION.id(), false) 
                || PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.id(), false);
    }

    @Nullable
    private WorldPoint getLocation(long pointId) {
        return this.manager.getLocationsContainer().getLocation(pointId);
    }

    private boolean locationAvailable(WorldPoint worldPoint, UUID playerUUID) {
        return !worldPoint.isLocked() 
                || PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.ENABLE_TELEPORTATION_TO_LOCKED_LOCATIONS.id(), false) 
                || worldPoint.isOwner(playerUUID);
    }

    private boolean canEditLocation(UUID playerUUID, WorldPoint worldPoint) {
        return PrivilegesProviderServer.getAsBoolean(playerUUID, EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.id(), false) 
                || worldPoint.isOwner(playerUUID);
    }

    private boolean readyMoveToLocation(UUID playerUUID) {
        return TimeHelperServer.getCurrentMillis() >= this.manager.getPlayersDataContainer().getPlayerData(playerUUID).getCooldownData().getNextLocationTime();
    }
}
