package austeretony.teleportation.common.menu.camps;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Iterator;
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
import austeretony.teleportation.common.main.PlayerProfile;
import austeretony.teleportation.common.main.TeleportationMain;
import austeretony.teleportation.common.main.TeleportationProcess;
import austeretony.teleportation.common.network.client.CPDownloadImagePart;
import austeretony.teleportation.common.network.client.CPStartImageDownload;
import austeretony.teleportation.common.util.BufferedImageUtils;
import austeretony.teleportation.common.util.ImageTransferingClientBuffer;
import austeretony.teleportation.common.util.ImageTransferingServerBuffer;
import austeretony.teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class CampsManagerServer {

    private static CampsManagerServer instance;

    private final Map<UUID, PlayerProfile> playersProfiles = new ConcurrentHashMap<UUID, PlayerProfile>();

    private final Map<Long, ImageTransferingServerBuffer> imageTransfers = new ConcurrentHashMap<Long, ImageTransferingServerBuffer>();

    private final Map<UUID, TeleportationProcess> teleportations = new ConcurrentHashMap<UUID, TeleportationProcess>();

    private CampsManagerServer() {}

    public static void create() {
        instance = new CampsManagerServer();
    }

    public static CampsManagerServer instance() {
        return instance;
    }

    public Map<UUID, PlayerProfile> getPlayersProfiles() {
        return this.playersProfiles;
    }

    public boolean isProfileExist(UUID playerUUID) {
        return this.playersProfiles.containsKey(playerUUID);
    }

    public void addPlayersProfile(UUID playerUUID, PlayerProfile playerProfile) {
        this.playersProfiles.put(playerUUID, playerProfile);
    }

    public void removePlayerProfile(UUID playerUUID) {
        this.playersProfiles.remove(playerUUID);
    }

    public void createPlayerProfile(UUID playerUUID) {
        this.getPlayersProfiles().put(playerUUID, new PlayerProfile(playerUUID));
    }    

    public PlayerProfile getPlayerProfile(UUID playerUUID) {
        return this.playersProfiles.get(playerUUID);
    }

    //TODO onPlayerLoggedIn()
    public void onPlayerLoggedIn(EntityPlayer player) {
        UUID playerUUID = OxygenHelperServer.uuid(player);
        if (!this.isProfileExist(playerUUID))
            CampsLoaderServer.loadPlayerDataDelegated(playerUUID);
        if (this.isProfileExist(playerUUID))//TODO clean up this mess
            CampsManagerServer.instance().appendAdditionalPlayerData(playerUUID);
    }

    //TODO appendAdditionalPlayerData()
    public void appendAdditionalPlayerData(UUID playerUUID) {
        ByteBuffer byteBuff = ByteBuffer.allocate(1);
        byteBuff.put((byte) this.getPlayerProfile(playerUUID).getJumpProfile().ordinal());
        OxygenHelperServer.getPlayerData(playerUUID).addData(TeleportationMain.JUMP_PROFILE_DATA_ID, byteBuff);
    }

    //TODO updateAdditionalPlayerData()
    public void updateAdditionalPlayerData(UUID playerUUID) {
        OxygenHelperServer.getPlayerData(playerUUID).getData(TeleportationMain.JUMP_PROFILE_DATA_ID).put(0, (byte) this.getPlayerProfile(playerUUID).getJumpProfile().ordinal());
    }

    //TODO onPlayerLoggedOut()
    public void onPlayerLoggedOut(EntityPlayer player) {}

    public Map<Long, ImageTransferingServerBuffer> getImageTransfers() {
        return this.imageTransfers;
    }

    public Map<UUID, TeleportationProcess> getTeleportations() {
        return this.teleportations;
    }

    //TODO moveToCamp()
    public void moveToCamp(EntityPlayerMP playerMP, long pointId) {
        if (TeleportationConfig.ENABLE_CAMPS.getBooleanValue()) {
            UUID playerUUID = OxygenHelperServer.uuid(playerMP);
            if (this.campExist(playerUUID, pointId) && this.campAvailable(pointId, playerMP, playerUUID) && !this.teleporting(playerUUID) && this.readyMoveToCamp(playerUUID)) {
                if (!PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.ENABLE_CROSS_DIM_TELEPORTATION.toString(), TeleportationConfig.ENABLE_CROSS_DIM_TELEPORTATION.getBooleanValue())
                        && playerMP.dimension != this.getPlayerProfile(playerUUID).getCamp(pointId).getDimensionId()) {
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CROSS_DIM_TELEPORTSTION_DISABLED.ordinal());
                    return;
                }
                int delay = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.CAMP_TELEPORTATION_DELAY.toString(), TeleportationConfig.CAMPS_TELEPORT_DELAY.getIntValue());
                if (delay < 1)
                    delay = 1;
                if (delay > 1)
                    OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.PREPARE_FOR_TELEPORTATION.ordinal(), String.valueOf(delay));
                TeleportationProcess.create(TeleportationProcess.EnumTeleportations.CAMP, playerMP, pointId, delay);    
            }
        }
    }

    public void processTeleportations() {
        if (!this.getTeleportations().isEmpty()) {
            Iterator<TeleportationProcess> iterator = this.getTeleportations().values().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().expired())
                    iterator.remove();
            }
        }
    }

    //TODO createCamp()
    public void createCamp(EntityPlayerMP playerMP, long pointId, String name, String description) {
        if (TeleportationConfig.ENABLE_CAMPS.getBooleanValue()) {
            UUID playerUUID = OxygenHelperServer.uuid(playerMP);
            if (this.getPlayerProfile(playerUUID).getCampsAmount() 
                    < PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.CAMPS_MAX_AMOUNT.toString(), TeleportationConfig.CAMPS_MAX_AMOUNT.getIntValue())) {
                WorldPoint worldPoint = new WorldPoint(
                        playerUUID,
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
                this.getPlayerProfile(playerUUID).addCamp(worldPoint);
                CampsLoaderServer.savePlayerDataDelegated(playerUUID);
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CAMP_CREATED.ordinal(), worldPoint.getName());
            }
        }
    }

    public void downloadCampPreviewToClientDelegated(EntityPlayerMP playerMP, long pointId, BufferedImage bufferedImage) {
        OxygenHelperServer.addRoutineTaskServer(new OxygenTask() {

            @Override
            public void execute() {
                downloadCampPreviewToClient(playerMP, pointId, bufferedImage);
            }  
        });
    }

    public void downloadCampPreviewToClient(EntityPlayerMP playerMP, long pointId, BufferedImage bufferedImage) {
        List<byte[]> imageParts = BufferedImageUtils.convertBufferedImageToByteArraysList(bufferedImage);
        TeleportationMain.network().sendTo(new CPStartImageDownload(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_CAMP, pointId, imageParts.size()), playerMP);  
        int index = 0;
        for (byte[] part : imageParts) {
            TeleportationMain.network().sendTo(new CPDownloadImagePart(ImageTransferingClientBuffer.EnumImageTransfer.DOWNLOAD_CAMP, pointId, index, part, imageParts.size()), playerMP);
            index++;
        }
    }

    //TODO removeCamp()
    public void removeCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = OxygenHelperServer.uuid(playerMP);
        if (this.campExist(playerUUID, pointId) && this.owner(playerUUID, pointId)) {
            OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CAMP_REMOVED.ordinal(), this.getPlayerProfile(playerUUID).getCamp(pointId).getName());
            this.getPlayerProfile(playerUUID).removeCamp(pointId);
            if (pointId == this.getPlayerProfile(playerUUID).getFavoriteCampId())
                this.getPlayerProfile(playerUUID).setFavoriteCampId(0L);
            CampsLoaderServer.savePlayerDataDelegated(playerUUID);
            CampsLoaderServer.removeCampPreviewImageDelegated(playerUUID, pointId);
        }
    }

    //TODO setFavoriteCamp()
    public void setFavoriteCamp(EntityPlayerMP playerMP, long pointId) {
        UUID playerUUID = OxygenHelperServer.uuid(playerMP);
        if (TeleportationConfig.ENABLE_FAVORITE_CAMP.getBooleanValue() && this.campExist(playerUUID, pointId) && pointId != this.getPlayerProfile(playerUUID).getFavoriteCampId()) {
            this.getPlayerProfile(playerUUID).setFavoriteCampId(pointId);
            CampsLoaderServer.savePlayerDataDelegated(playerUUID);
            OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.SET_FAVORITE.ordinal(), this.getPlayerProfile(playerUUID).getCamp(pointId).getName());
        }
    }

    //TODO lockCamp()
    public void lockCamp(EntityPlayerMP playerMP, long oldPointId, boolean flag) {
        UUID playerUUID = OxygenHelperServer.uuid(playerMP);
        if (this.campExist(playerUUID, oldPointId) && this.owner(playerUUID, oldPointId)) {
            WorldPoint worldPoint = this.getPlayerProfile(playerUUID).getCamp(oldPointId);
            worldPoint.setLocked(flag);
            worldPoint.setId(oldPointId + 1L);
            this.getPlayerProfile(playerUUID).addCamp(worldPoint);
            if (this.getPlayerProfile(playerUUID).getFavoriteCampId() == oldPointId)
                this.getPlayerProfile(playerUUID).setFavoriteCampId(worldPoint.getId());
            this.getPlayerProfile(playerUUID).removeCamp(oldPointId);
            CampsLoaderServer.savePlayerDataDelegated(playerUUID);
            CampsLoaderServer.renameCampPreviewImageDelegated(playerUUID, oldPointId, worldPoint.getId());
            if (flag)
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CAMP_LOCKED.ordinal(), worldPoint.getName());
            else
                OxygenHelperServer.sendMessage(playerMP, TeleportationMain.TELEPORTATION_MOD_INDEX, EnumChatMessages.CAMP_UNLOCKED.ordinal(), worldPoint.getName());
        }
    }

    //TODO editCamp()
    public void editCamp(EntityPlayerMP playerMP, long oldPointId, String name, String description, boolean updateName, 
            boolean updateDescription, boolean updateImage, boolean updatePosition) {
        UUID playerUUID = OxygenHelperServer.uuid(playerMP);
        if (this.campExist(playerUUID, oldPointId) && this.owner(playerUUID, oldPointId)) {
            WorldPoint worldPoint = this.getPlayerProfile(playerUUID).getCamp(oldPointId);
            long newPointId = oldPointId + 1L;
            boolean edited = false;
            if (updateName)
                worldPoint.setName(name);
            if (updateDescription)
                worldPoint.setDescription(description);
            if (updateImage)
                CampsLoaderServer.removeCampPreviewImage(playerUUID, oldPointId);
            if (updatePosition)
                worldPoint.setPosition(playerMP.rotationYaw, playerMP.rotationPitch, (float) playerMP.posX, (float) playerMP.posY, (float) playerMP.posZ, playerMP.dimension);
            edited = updateName || updateDescription || updateImage || updatePosition;
            if (edited) {
                worldPoint.setId(newPointId);
                this.getPlayerProfile(playerUUID).addCamp(worldPoint);
                if (this.getPlayerProfile(playerUUID).getFavoriteCampId() == oldPointId)
                    this.getPlayerProfile(playerUUID).setFavoriteCampId(newPointId);
                this.getPlayerProfile(playerUUID).removeCamp(oldPointId);
                CampsLoaderServer.savePlayerDataDelegated(playerUUID);
                if (!updateImage)
                    CampsLoaderServer.renameCampPreviewImageDelegated(playerUUID, oldPointId, newPointId);
            }
        }
    }

    private boolean campExist(UUID playerUUID, long pointId) {
        return this.getPlayerProfile(playerUUID).campExist(pointId);
    }

    private boolean campAvailable(long pointId, EntityPlayerMP playerMP, UUID playerUUID) {       
        return !this.getPlayerProfile(playerUUID).getCamp(pointId).isLocked() || CommonReference.isOpped(playerMP) || this.getPlayerProfile(playerUUID).getCamp(pointId).isOwner(playerUUID);
    }

    private boolean owner(UUID playerUUID, long pointId) {
        return this.getPlayerProfile(playerUUID).getCamp(pointId).isOwner(playerUUID);
    }

    private boolean teleporting(UUID playerUUID) {
        return TeleportationProcess.exist(playerUUID);
    }

    private boolean readyMoveToCamp(UUID playerUUID) {
        return System.currentTimeMillis() - this.getPlayerProfile(playerUUID).getCooldownInfo().getLastCampTime() 
                > PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumPrivileges.CAMP_TELEPORTATION_COOLDOWN.toString(), TeleportationConfig.CAMPS_TELEPORT_COOLDOWN.getIntValue()) * 1000;
    }
}
