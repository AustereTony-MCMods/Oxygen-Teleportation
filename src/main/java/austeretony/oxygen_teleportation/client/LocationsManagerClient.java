package austeretony.oxygen_teleportation.client;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_teleportation.common.WorldPoint;
import austeretony.oxygen_teleportation.common.WorldPoint.EnumWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPChangePointLockState;
import austeretony.oxygen_teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPEditWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToPoint;
import austeretony.oxygen_teleportation.common.network.server.SPRemoveWorldPoint;

public class LocationsManagerClient {

    private final TeleportationManagerClient manager;

    protected LocationsManagerClient(TeleportationManagerClient manager) { 
        this.manager = manager;
    }

    public void moveToLocationSynced(long pointId) {        
        OxygenMain.network().sendToServer(new SPMoveToPoint(EnumWorldPoint.LOCATION, pointId));
    }

    public void createLocationPointSynced(String name, String description) {
        OxygenMain.network().sendToServer(new SPCreateWorldPoint(EnumWorldPoint.LOCATION, name, description));
    }  

    public void removeLocationPointSynced(long pointId) {
        OxygenMain.network().sendToServer(new SPRemoveWorldPoint(EnumWorldPoint.LOCATION, pointId));
    }

    public void lockLocationSynced(long pointId, boolean flag) {    
        OxygenMain.network().sendToServer(new SPChangePointLockState(EnumWorldPoint.LOCATION, pointId, flag));
    }

    public void editLocationPointSynced(long pointId, String name, String description, boolean updatePosition, boolean updateImage) {
        OxygenMain.network().sendToServer(new SPEditWorldPoint(EnumWorldPoint.LOCATION, pointId, name, description, updatePosition, updateImage));
    }

    public void locationCreated(WorldPoint worldPoint) {
        this.manager.getLocationsContainer().addLocation(worldPoint);
        this.manager.getLocationsContainer().setChanged(true);
        this.manager.getImagesManager().cacheLatestImage(worldPoint.getId());
        this.manager.getImagesLoader().saveLatestLocationPreviewImageAsync(worldPoint.getId());
        this.manager.getImagesManager().uploadLocationPreviewToServerAsync(worldPoint.getId());

        this.manager.getTeleportationMenuManager().locationCreated(worldPoint);
    }

    public void locationEdited(long oldPointId, WorldPoint worldPoint, boolean updateImage) {
        this.manager.getLocationsContainer().removeLocation(oldPointId);
        this.manager.getLocationsContainer().addLocation(worldPoint);
        this.manager.getLocationsContainer().setChanged(true);

        if (updateImage) {
            this.manager.getImagesManager().cacheLatestImage(worldPoint.getId());
            this.manager.getImagesManager().removeCachedImage(oldPointId);
            this.manager.getImagesLoader().saveLatestLocationPreviewImageAsync(worldPoint.getId());
            this.manager.getImagesManager().uploadLocationPreviewToServerAsync(worldPoint.getId());
        } else {
            this.manager.getImagesLoader().renameLocationPreviewImageAsync(oldPointId, worldPoint.getId());
            this.manager.getImagesManager().replaceCachedImage(oldPointId, worldPoint.getId());
        }

        this.manager.getTeleportationMenuManager().locationEdited(oldPointId, worldPoint, updateImage);
    }

    public void locationRemoved(long pointId) {
        this.manager.getLocationsContainer().removeLocation(pointId);
        this.manager.getLocationsContainer().setChanged(true);
        this.manager.getImagesManager().removeCachedImage(pointId);

        this.manager.getTeleportationMenuManager().locationRemoved(pointId);
    }
}
