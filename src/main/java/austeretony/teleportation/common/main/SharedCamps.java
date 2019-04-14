package austeretony.teleportation.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import austeretony.oxygen.common.util.PacketBufferUtils;
import austeretony.oxygen.common.util.StreamUtils;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.network.PacketBuffer;

public class SharedCamps {

    public final UUID playerUUID;

    public final String username;

    private final Set<Long> camps = new ConcurrentSet<Long>();

    public SharedCamps(UUID playerUUID, String username) {
        this.playerUUID = playerUUID;
        this.username = username;
    }

    public Set<Long> getCamps() {
        return this.camps;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.playerUUID, bos);
        StreamUtils.write(this.username, bos);
        StreamUtils.write((byte) this.camps.size(), bos);
        for (long id : this.camps)
            StreamUtils.write(id, bos);
    }

    public static SharedCamps read(BufferedInputStream bis) throws IOException {
        SharedCamps sharedCamps = new SharedCamps(
                StreamUtils.readUUID(bis), 
                StreamUtils.readString(bis));
        int size = StreamUtils.readByte(bis);
        for (int i = 0; i < size; i++)
            sharedCamps.getCamps().add(StreamUtils.readLong(bis));
        return sharedCamps;
    }

    public void write(PacketBuffer buffer) {
        PacketBufferUtils.writeUUID(this.playerUUID, buffer);
        PacketBufferUtils.writeString(this.username, buffer);
        buffer.writeByte(this.camps.size());
        for (long id : this.camps)
            buffer.writeLong(id);
    }

    public static SharedCamps read(PacketBuffer buffer) {
        SharedCamps sharedCamps = new SharedCamps(
                PacketBufferUtils.readUUID(buffer), 
                PacketBufferUtils.readString(buffer));
        int size = buffer.readByte();
        for (int i = 0; i < size; i++)
            sharedCamps.getCamps().add(buffer.readLong());
        return sharedCamps;
    }
}
