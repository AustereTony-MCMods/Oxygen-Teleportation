package austeretony.oxygen_teleportation.common.util;

import java.util.List;

public class SplittedByteArray {

    private final List<byte[]> fragments;

    public SplittedByteArray(List<byte[]> parts) {
        this.fragments = parts;
    }

    public List<byte[]> getParts() {
        return this.fragments;
    }
}
