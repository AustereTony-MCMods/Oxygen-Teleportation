package austeretony.teleportation.common.menu.camps;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Set;

import austeretony.oxygen.common.util.StreamUtils;
import io.netty.util.internal.ConcurrentSet;

public class CampsList {

    private final Set<Long> camps = new ConcurrentSet<Long>();

    public Set<Long> getCamps() {
        return this.camps;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.camps.size(), bos);
        for (long id : this.camps)
            StreamUtils.write(id, bos);
    }

    public static CampsList read(BufferedInputStream bis) throws IOException {
        CampsList list = new CampsList();
        int size = StreamUtils.readInt(bis);
        for (int i = 0; i < size; i++)
            list.getCamps().add(StreamUtils.readLong(bis));
        return list;
    }
}
