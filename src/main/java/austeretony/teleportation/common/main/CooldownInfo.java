package austeretony.teleportation.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen.common.util.StreamUtils;
import net.minecraft.network.PacketBuffer;

public class CooldownInfo {

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

    public void write(PacketBuffer buffer) {
        buffer.writeLong(this.camp);
        buffer.writeLong(this.location);
        buffer.writeLong(this.jump);
    }

    public void read(PacketBuffer buffer) {  
        this.camp = buffer.readLong();
        this.location = buffer.readLong();
        this.jump = buffer.readLong();
    }
}
