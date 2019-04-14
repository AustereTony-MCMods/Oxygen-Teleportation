package austeretony.teleportation.common.util;

import java.util.List;

public class SplittedByteArray {

    private final List<byte[]> parts;

    public SplittedByteArray(List<byte[]> parts) {
        this.parts = parts;
    }

    public List<byte[]> getParts() {
        return this.parts;
    }
}
