package austeretony.oxygen_teleportation.common.network.client;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_teleportation.client.TeleportationManagerClient;
import austeretony.oxygen_teleportation.common.main.CooldownInfo;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class CPSyncCooldown extends ProxyPacket {

    private CooldownInfo cooldownInfo;

    public CPSyncCooldown() {}

    public CPSyncCooldown(CooldownInfo cooldownInfo) {
        this.cooldownInfo = cooldownInfo;
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        this.cooldownInfo.write(buffer);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        TeleportationManagerClient.instance().getPlayerData().getCooldownInfo().read(buffer);
    }
}
