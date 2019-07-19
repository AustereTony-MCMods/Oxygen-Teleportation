package austeretony.oxygen_teleportation.common.network.client;

import java.util.UUID;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.common.api.OxygenGUIHelper;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.TeleportationManagerServer;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.world.WorldPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncWorldPoints extends ProxyPacket {

    private WorldPoint.EnumPointType type;

    private long[] points;

    public CPSyncWorldPoints() {}

    public CPSyncWorldPoints(WorldPoint.EnumPointType type, long[] pointIds) {
        this.type = type;
        this.points = pointIds;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        buffer.writeByte(this.type.ordinal());
        buffer.writeShort(this.points.length);
        switch (this.type) {
        case CAMP:
            TeleportationPlayerData ownerData = TeleportationManagerServer.instance().getPlayerData(playerUUID);
            for (long id : this.points) {
                if (TeleportationManagerServer.instance().getSharedCampsManager().haveInvitation(playerUUID, id))
                    TeleportationManagerServer.instance().getSharedCampsManager().getCamp(id).write(buffer);
                else
                    ownerData.getCamp(id).write(buffer);
            }
            break;
        case LOCATION:
            for (long id : this.points)
                TeleportationManagerServer.instance().getWorldData().getLocation(id).write(buffer);
            break;
        }
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        this.type = WorldPoint.EnumPointType.values()[buffer.readByte()];
        int 
        amount = buffer.readShort(),
        i;
        switch (this.type) {
        case CAMP:
            for (i = 0; i < amount; i++)        
                TeleportationManagerClient.instance().getPlayerData().addCamp(WorldPoint.read(buffer));
            OxygenHelperClient.savePersistentDataDelegated(TeleportationManagerClient.instance().getPlayerData());
            OxygenGUIHelper.dataRecieved(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
            break;
        case LOCATION:
            for (i = 0; i < amount; i++)
                TeleportationManagerClient.instance().getWorldData().addLocation(WorldPoint.read(buffer));
            OxygenHelperClient.savePersistentDataDelegated(TeleportationManagerClient.instance().getWorldData());
            OxygenGUIHelper.dataRecieved(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
            break;
        }
    }
}
