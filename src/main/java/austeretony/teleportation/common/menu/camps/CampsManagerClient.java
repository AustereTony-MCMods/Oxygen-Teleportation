package austeretony.teleportation.common.menu.camps;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.client.reference.ClientReference;
import austeretony.oxygen.common.api.OxygenHelperClient;
import austeretony.oxygen.common.api.OxygenTask;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderClient;
import austeretony.teleportation.client.gui.menu.MenuGUIScreen;
import austeretony.teleportation.client.util.ScreenshotHelper;
import austeretony.teleportation.common.config.TeleportationConfig;
import austeretony.teleportation.common.main.EnumPrivileges;
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.menu.locations.LocationsLoaderClient;
import austeretony.teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.teleportation.common.network.server.SPEditWorldPoint;
import austeretony.teleportation.common.network.server.SPLockPoint;
import austeretony.teleportation.common.network.server.SPMoveToPoint;
import austeretony.teleportation.common.network.server.SPRemoveWorldPoint;
import austeretony.teleportation.common.network.server.SPRequest;
import austeretony.teleportation.common.network.server.SPSetFavoriteCamp;
import austeretony.teleportation.common.network.server.SPStartImageUpload;
import austeretony.teleportation.common.network.server.SPUploadImagePart;
import austeretony.teleportation.common.util.BufferedImageUtils;
import austeretony.teleportation.common.util.ImageTransferingClientBuffer;
import austeretony.teleportation.common.util.ImageTransferingServerBuffer;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CampsManagerClient {

    private static CampsManagerClient instance;

    private PlayerProfile playerProfile;

    private boolean isOpped;

    private BufferedImage previewImage;

    private Map<Long, BufferedImage> previewImages = new ConcurrentHashMap<Long, BufferedImage>();

    private final Map<Long, ImageTransferingClientBuffer> imageTransfers = new ConcurrentHashMap<Long, ImageTransferingClientBuffer>();

    private long teleportationTime, delay;

    private CampsManagerClient() {}

    public static void create() {
        instance = new CampsManagerClient();
        CampsLoaderClient.loadCampsDataDelegated();
    }

    public static CampsManagerClient instance() {
        return instance;
    }

    public PlayerProfile getPlayerProfile() {
        return this.playerProfile;
    }

    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    public void createPlayerProfile() {
        this.playerProfile = new PlayerProfile(OxygenHelperClient.getPlayerUUID());
    }

    public boolean isOpped() {
        return this.isOpped;
    }

    public void setOpped(boolean value) {
        this.isOpped = value;
    }

    public void openMenuSynced() {
        if (!this.teleporting()) {
            this.hideGUI();
            TeleportationMain.network().sendToServer(new SPRequest(SPRequest.EnumRequest.OPEN_MENU));
        }
    }

    public void openMenuDelegated() {
        ClientReference.getMinecraft().addScheduledTask(new Runnable() {

            @Override
            public void run() {
                openMenu();
            }
        });
    }

    //TODO openMenu()
    public void openMenu() {
        this.preparePreviewImage();
        this.showGUI();       
        ClientReference.openGuiScreen(new MenuGUIScreen());
        CampsLoaderClient.savePlayerDataDelegated();
        CampsLoaderClient.removeUnusedCampPreviewImagesDelegated();
        LocationsLoaderClient.removeUnusedLocationPreviewImagesDelegated();
    }

    public void hideGUI() {
        ClientReference.getMinecraft().gameSettings.hideGUI = true;
    }

    public void showGUI() {
        ClientReference.getMinecraft().gameSettings.hideGUI = false;
    }

    public void preparePreviewImage() {
        this.previewImage = ScreenshotHelper.createScreenshot();
    }

    public BufferedImage getLatestImage() {
        return this.previewImage;
    }

    public Map<Long, BufferedImage> getPreviewImages() {
        return this.previewImages;
    }

    public void replaceImage(long oldPointId, long newPointId) {
        if (this.previewImages.containsKey(oldPointId)) {
            this.previewImages.put(newPointId, this.previewImages.get(oldPointId));
            this.previewImages.remove(oldPointId);
        }
    }

    public Map<Long, ImageTransferingClientBuffer> getImageTransfers() {
        return this.imageTransfers;
    }

    //TODO downloadCampsDataSynced()
    public void downloadCampsDataSynced() {
        this.playerProfile.getCamps().clear();
        this.openMenuSynced();
    }

    //TODO moveToCampSynced()
    public void moveToCampSynced(long id) {        
        if (id != 0L) {
            TeleportationMain.network().sendToServer(new SPMoveToPoint(WorldPoint.EnumWorldPoints.CAMP, id));
            this.setTeleportationDelay(PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMP_TELEPORTATION_DELAY.toString(), TeleportationConfig.CAMPS_TELEPORT_DELAY.getIntValue()));
        }
    }

    public boolean teleporting() {
        return System.currentTimeMillis() < this.teleportationTime + this.delay;
    }

    public void setTeleportationDelay(long delay) {
        this.delay = delay * 1000;
        this.teleportationTime = System.currentTimeMillis();
    }

    //TODO setCampPointClientSynced()
    public void setCampPointSynced(WorldPoint worldPoint) {
        if (CampsManagerClient.instance().getPlayerProfile().getCampsAmount() 
                < PrivilegeProviderClient.getPrivilegeValue(EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue())) {
            this.playerProfile.addCamp(worldPoint);
            TeleportationMain.network().sendToServer(new SPCreateWorldPoint(WorldPoint.EnumWorldPoints.CAMP, worldPoint));
            CampsLoaderClient.savePlayerDataDelegated();
            this.getPreviewImages().put(worldPoint.getId(), this.getLatestImage());
            CampsLoaderClient.saveCampPreviewImageDelegated(worldPoint.getId(), this.getLatestImage());
            this.uploadCampPreviewToServerDelegated(worldPoint.getId(), this.getLatestImage());
        }
    }

    public void uploadCampPreviewToServerDelegated(long pointId, BufferedImage bufferedImage) {
        OxygenHelperClient.addRoutineTaskClient(new OxygenTask() {

            @Override
            public void execute() {
                uploadCampPreviewToServer(pointId, bufferedImage);
            }  
        });
    }

    public void uploadCampPreviewToServer(long pointId, BufferedImage bufferedImage) {
        List<int[]> imageParts = BufferedImageUtils.convertBufferedImageToIntArraysList(bufferedImage);
        TeleportationMain.network().sendToServer(new SPStartImageUpload(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_CAMP, pointId, imageParts.size()));  
        int index = 0;
        for (int[] part : imageParts) {
            TeleportationMain.network().sendToServer(new SPUploadImagePart(ImageTransferingServerBuffer.EnumImageTransfer.UPLOAD_CAMP, pointId, index, part, imageParts.size()));
            index++;
        }
    }

    //TODO removeCampPointSynced()
    public void removeCampPointSynced(long pointId) {
        this.playerProfile.removeCamp(pointId);
        TeleportationMain.network().sendToServer(new SPRemoveWorldPoint(WorldPoint.EnumWorldPoints.CAMP, pointId));
        if (pointId == this.playerProfile.getFavoriteCampId())
            this.playerProfile.setFavoriteCampId(0L);
        CampsLoaderClient.savePlayerDataDelegated();
        this.getPreviewImages().remove(pointId);
    }

    //TODO setFavoriteCampSynced()
    public void setFavoriteCampSynced(long id) {        
        this.playerProfile.setFavoriteCampId(id);
        TeleportationMain.network().sendToServer(new SPSetFavoriteCamp(id));
        CampsLoaderClient.savePlayerDataDelegated();
    }

    //TODO lockCampSynced()
    public void lockCampSynced(WorldPoint worldPoint, boolean flag) {    
        long oldPointId = worldPoint.getId();
        worldPoint.setLocked(flag);
        worldPoint.setId(worldPoint.getId() + 1L);
        this.playerProfile.addCamp(worldPoint);
        if (this.playerProfile.getFavoriteCampId() == oldPointId)
            this.playerProfile.setFavoriteCampId(worldPoint.getId());
        this.playerProfile.removeCamp(oldPointId);
        CampsLoaderClient.savePlayerDataDelegated();
        TeleportationMain.network().sendToServer(new SPLockPoint(WorldPoint.EnumWorldPoints.CAMP, oldPointId, flag));
        CampsLoaderClient.renameCampPreviewImageDelegated(oldPointId, worldPoint.getId());
        this.replaceImage(oldPointId, worldPoint.getId());
    }

    //TODO editCampPointSynced()
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
            this.getPreviewImages().put(newPointId, this.getLatestImage());
            this.getPreviewImages().remove(oldPointId);
            CampsLoaderClient.saveCampPreviewImageDelegated(newPointId, this.getLatestImage());
            this.uploadCampPreviewToServerDelegated(newPointId, this.getLatestImage());
        }
        if (updatePosition) {
            EntityPlayer player = ClientReference.getClientPlayer();
            worldPoint.setPosition(player.rotationYaw, player.rotationPitch, (float) player.posX, (float) player.posY, (float) player.posZ, player.dimension);

        }
        edited = updateName || updateDescription || updateImage || updatePosition;
        if (edited) {
            worldPoint.setId(newPointId);
            this.playerProfile.addCamp(worldPoint);
            if (this.playerProfile.getFavoriteCampId() == oldPointId)
                this.playerProfile.setFavoriteCampId(newPointId);
            this.playerProfile.removeCamp(oldPointId);
            CampsLoaderClient.savePlayerDataDelegated();
            TeleportationMain.network().sendToServer(new SPEditWorldPoint(WorldPoint.EnumWorldPoints.CAMP, oldPointId, newName, newDescription, 
                    updateName, updateDescription, updateImage, updatePosition));
            if (!updateImage) {
                CampsLoaderClient.renameCampPreviewImageDelegated(oldPointId, newPointId);
                this.replaceImage(oldPointId, newPointId);
            }
        }
    }
}
