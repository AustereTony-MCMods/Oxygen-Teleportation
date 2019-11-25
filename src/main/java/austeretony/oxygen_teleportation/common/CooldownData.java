package austeretony.oxygen_teleportation.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;

public class CooldownData {

    private UUID playerUUID;

    private long nextCamp, nextLocation, nextJump;

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void movedToCamp() {
        this.nextCamp = System.currentTimeMillis() + PrivilegeProviderServer.getValue(this.playerUUID, EnumTeleportationPrivilege.CAMP_TELEPORTATION_COOLDOWN_SECONDS.toString(), TeleportationConfig.CAMP_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()) * 1000L;
    }

    public void movedToLocation() {
        this.nextLocation = System.currentTimeMillis() + PrivilegeProviderServer.getValue(this.playerUUID, EnumTeleportationPrivilege.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.toString(), TeleportationConfig.LOCATION_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()) * 1000L;;
    }

    public void jumped() {
        this.nextJump = System.currentTimeMillis() + PrivilegeProviderServer.getValue(this.playerUUID, EnumTeleportationPrivilege.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.toString(), TeleportationConfig.PLAYER_TELEPORTATION_COOLDOWN_SECONDS.getIntValue()) * 1000L;
    }

    public void updateCooldown(int campCooldownLeftSeconds, int locationCooldownLeftSeconds, int jumpCooldownLeftSeconds) {
        this.nextCamp = System.currentTimeMillis() + campCooldownLeftSeconds * 1000L;
        this.nextLocation = System.currentTimeMillis() + locationCooldownLeftSeconds * 1000L;
        this.nextJump = System.currentTimeMillis() + jumpCooldownLeftSeconds * 1000L;
    }

    public long getNextCampTime() {
        return this.nextCamp;
    }

    public long getNextLocationTime() {
        return this.nextLocation;
    }

    public long getNextJumpTime() {
        return this.nextJump;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.nextCamp, bos);
        StreamUtils.write(this.nextLocation, bos);
        StreamUtils.write(this.nextJump, bos);
    }

    public void read(BufferedInputStream bis) throws IOException {  
        this.nextCamp = StreamUtils.readLong(bis);
        this.nextLocation = StreamUtils.readLong(bis);
        this.nextJump = StreamUtils.readLong(bis);
    }
}
