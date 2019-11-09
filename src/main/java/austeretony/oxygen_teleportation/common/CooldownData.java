package austeretony.oxygen_teleportation.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen_core.common.util.StreamUtils;

public class CooldownData {

    private long camp, location, jump;

    public void movedToCamp() {
        this.camp = System.currentTimeMillis();
    }

    public void movedToLocation() {
        this.location = System.currentTimeMillis();
    }

    public void jumped() {
        this.jump = System.currentTimeMillis();
    }

    public void updateCooldown(long campTime, long locationTime, long jumpTime) {
        this.camp = campTime;
        this.location = locationTime;
        this.jump = jumpTime;
    }

    public long getLastCampTime() {
        return this.camp;
    }

    public long getLastLocationTime() {
        return this.location;
    }

    public long getLastJumpTime() {
        return this.jump;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.camp, bos);
        StreamUtils.write(this.location, bos);
        StreamUtils.write(this.jump, bos);
    }

    public void read(BufferedInputStream bis) throws IOException {  
        this.camp = StreamUtils.readLong(bis);
        this.location = StreamUtils.readLong(bis);
        this.jump = StreamUtils.readLong(bis);
    }
}
