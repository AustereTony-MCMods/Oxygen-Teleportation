package austeretony.teleportation.client;

import java.util.UUID;

import austeretony.oxygen.client.reference.ClientReference;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.teleportation.common.network.server.SPEditWorldPoint;
import austeretony.teleportation.common.network.server.SPLeaveCampPoint;
import austeretony.teleportation.common.network.server.SPLockPoint;
import austeretony.teleportation.common.network.server.SPManageInvitation;
import austeretony.teleportation.common.network.server.SPMoveToPoint;
import austeretony.teleportation.common.network.server.SPRemoveWorldPoint;
import austeretony.teleportation.common.network.server.SPSetFavoriteCamp;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CampsManagerClient {

    private final TeleportationManagerClient manager;

    public CampsManagerClient(TeleportationManagerClient manager) {
        this.manager = manager;
    }

    public void downloadCampsDataSynced() {
        this.manager.getPlayerProfile().getCamps().clear();
        this.manager.openMenuSynced();
    }

    public void moveToCampSynced(long id) {        
        if (id != 0L) {
            TeleportationMain.network().sendToServer(new SPMoveToPoint(WorldPoint.EnumWorldPoints.CAMP, id));
            this.manager.setTeleportationDelay(PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMP_TELEPORTATION_DELAY.toString(), TeleportationConfig.CAMPS_TELEPORT_DELAY.getIntValue()));
        }
    }

    public void setCampPointSynced(WorldPoint worldPoint) {
        if (this.manager.getPlayerProfile().getCampsAmount() < PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue())) {
            this.manager.getPlayerProfile().addCamp(worldPoint);
            TeleportationMain.network().sendToServer(new SPCreateWorldPoint(WorldPoint.EnumWorldPoints.CAMP, worldPoint));
            this.manager.getCampsLoader().savePlayerDataDelegated();
            this.manager.getImagesManager().cacheLatestImage(worldPoint.getId());
            this.manager.getImagesLoader().saveLatestCampPreviewImageDelegated(worldPoint.getId());
            this.manager.getImagesManager().uploadCampPreviewToServerDelegated(worldPoint.getId());
        }
    }

    public void removeCampPointSynced(long pointId) {
        this.manager.getPlayerProfile().removeCamp(pointId);
        TeleportationMain.network().sendToServer(new SPRemoveWorldPoint(WorldPoint.EnumWorldPoints.CAMP, pointId));
        if (pointId == this.manager.getPlayerProfile().getFavoriteCampId())
            this.manager.getPlayerProfile().setFavoriteCampId(0L);
        this.manager.getCampsLoader().savePlayerDataDelegated();
        this.manager.getImagesManager().getPreviewImages().remove(pointId);
    }

    public void setFavoriteCampSynced(long pointId) {        
        this.manager.getPlayerProfile().setFavoriteCampId(pointId);
        TeleportationMain.network().sendToServer(new SPSetFavoriteCamp(pointId));
        this.manager.getCampsLoader().savePlayerDataDelegated();
    }

    public void lockCampSynced(WorldPoint worldPoint, boolean flag) {    
        long oldPointId = worldPoint.getId();
        worldPoint.setLocked(flag);
        worldPoint.setId(worldPoint.getId() + 1L);
        this.manager.getPlayerProfile().addCamp(worldPoint);
        if (this.manager.getPlayerProfile().getFavoriteCampId() == oldPointId)
            this.manager.getPlayerProfile().setFavoriteCampId(worldPoint.getId());
        this.manager.getPlayerProfile().removeCamp(oldPointId);
        this.manager.getCampsLoader().savePlayerDataDelegated();
        TeleportationMain.network().sendToServer(new SPLockPoint(WorldPoint.EnumWorldPoints.CAMP, oldPointId, flag));
        this.manager.getImagesLoader().renameCampPreviewImageDelegated(oldPointId, worldPoint.getId());
        this.manager.getImagesManager().replaceCachedImage(oldPointId, worldPoint.getId());
    }

    public void editCampPointSynced(WorldPoint worldPoint, String newName, String newDescription, boolean updateImage, boolean updatePosition) {
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
            this.manager.getImagesManager().cacheLatestImage(newPointId);
            this.manager.getImagesManager().removeCachedImage(oldPointId);
            this.manager.getImagesManager().uploadCampPreviewToServerDelegated(newPointId);
            this.manager.getImagesLoader().saveLatestCampPreviewImageDelegated(newPointId);
        }
        if (updatePosition) {
            EntityPlayer player = ClientReference.getClientPlayer();
            worldPoint.setPosition(player.rotationYaw, player.rotationPitch, (float) player.posX, (float) player.posY, (float) player.posZ, player.dimension);

        }
        edited = updateName || updateDescription || updateImage || updatePosition;
        if (edited) {
            worldPoint.setId(newPointId);
            this.manager.getPlayerProfile().addCamp(worldPoint);
            if (this.manager.getPlayerProfile().getFavoriteCampId() == oldPointId)
                this.manager.getPlayerProfile().setFavoriteCampId(newPointId);
            this.manager.getPlayerProfile().removeCamp(oldPointId);
            this.manager.getCampsLoader().savePlayerDataDelegated();
            TeleportationMain.network().sendToServer(new SPEditWorldPoint(WorldPoint.EnumWorldPoints.CAMP, oldPointId, newName, newDescription, 
                    updateName, updateDescription, updateImage, updatePosition));
            if (!updateImage) {
                this.manager.getImagesLoader().renameCampPreviewImageDelegated(oldPointId, newPointId);
                this.manager.getImagesManager().replaceCachedImage(oldPointId, newPointId);
            }
        }
    }

    public void invitePlayerSynced(long pointId, UUID playerUUID, String username) {
        this.manager.getPlayerProfile().inviteToCamp(pointId, playerUUID, username);
        TeleportationMain.network().sendToServer(new SPManageInvitation(SPManageInvitation.EnumOperation.INVITE, pointId, playerUUID));
        this.manager.getCampsLoader().savePlayerDataDelegated();
    }

    public void uninvitePlayerSynced(long pointId, UUID playerUUID) {
        this.manager.getPlayerProfile().uninviteFromCamp(pointId, playerUUID);
        TeleportationMain.network().sendToServer(new SPManageInvitation(SPManageInvitation.EnumOperation.UNINVITE, pointId, playerUUID));
        this.manager.getCampsLoader().savePlayerDataDelegated();
    }

    public void leaveCampPointSynced(long pointId) {
        this.manager.getPlayerProfile().removeCamp(pointId);
        TeleportationMain.network().sendToServer(new SPLeaveCampPoint(pointId));
        if (pointId == this.manager.getPlayerProfile().getFavoriteCampId())
            this.manager.getPlayerProfile().setFavoriteCampId(0L);
        this.manager.getCampsLoader().savePlayerDataDelegated();
        this.manager.getImagesManager().getPreviewImages().remove(pointId);
    }
}
