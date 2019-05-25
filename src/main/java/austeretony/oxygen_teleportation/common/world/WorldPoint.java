package austeretony.oxygen_teleportation.common.world;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import austeretony.oxygen.common.util.OxygenUtils;
import austeretony.oxygen.common.util.PacketBufferUtils;
import austeretony.oxygen.common.util.StreamUtils;
import net.minecraft.network.PacketBuffer;

public class WorldPoint {

    public static final int 
    MAX_POINT_NAME_LENGTH = 20,
    MAX_POINT_DESCRIPTION_LENGTH = 80;

    public static final DateFormat CREATED_DATE_FORMAT = new SimpleDateFormat("d MM yyyy");

    private long id;

    public final UUID ownerUUID;

    public final String ownerName;

    private String name, desc, dateCreated;

    private float yaw, pitch, xPos, yPos, zPos;

    private int dimension;

    private boolean locked;

    public WorldPoint(UUID ownerUUID, String ownerName, String name, String description, int dimension, float xPos, float yPos, float zPos, float yaw, float pitch) {	
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;	
        this.name = name.length() > MAX_POINT_NAME_LENGTH ? name.substring(0, MAX_POINT_NAME_LENGTH) : name;		
        this.desc = description.length() > MAX_POINT_DESCRIPTION_LENGTH ? description.substring(0, MAX_POINT_DESCRIPTION_LENGTH) : description;
        this.dimension = dimension;
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.yaw = yaw;
        this.pitch = pitch;     
    }

    public long getId() {              
        return this.id;
    }

    public void setId(long pointId) {
        this.id = pointId;
    }

    public void createId() {
        this.id = OxygenUtils.createDataStampedId();
    }

    public String getName() {		 
        return this.name;
    }

    public void setName(String name) {		
        this.name = name;
    }

    public String getDescription() {		
        return this.desc;
    }

    public void setDescription(String description) {		
        this.desc = description;
    }

    public String getCreationDate() {		
        return this.dateCreated;
    }

    public void setCreationDate(String date) {		
        this.dateCreated = date;
    }

    public void createDate() {
        this.dateCreated = CREATED_DATE_FORMAT.format(new Date());    
    }

    public float getYaw() {		
        return this.yaw;
    }

    public float getPitch() {		
        return this.pitch;
    }

    public float getXPos() {		
        return this.xPos;
    }

    public float getYPos() {		
        return this.yPos;
    }

    public float getZPos() {		
        return this.zPos;
    }

    public int getDimensionId() {		
        return this.dimension;
    }

    public void setPosition(float yaw, float pitch, float x, float y, float z, int dimension) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.xPos = x;
        this.yPos = y;
        this.zPos = z;
        this.dimension = dimension;
    }

    public boolean isOwner(UUID playerUUID) {
        return playerUUID.equals(this.ownerUUID);
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean flag) {
        this.locked = flag;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.ownerUUID, bos);
        StreamUtils.write(this.ownerName, bos);
        StreamUtils.write(this.getName(), bos);
        StreamUtils.write(this.getDescription(), bos);
        StreamUtils.write(this.getDimensionId(), bos);
        StreamUtils.write(this.getXPos(), bos);
        StreamUtils.write(this.getYPos(), bos);
        StreamUtils.write(this.getZPos(), bos);
        StreamUtils.write(this.getYaw(), bos);
        StreamUtils.write(this.getPitch(), bos);
        StreamUtils.write(this.getId(), bos);
        StreamUtils.write(this.isLocked(), bos);
        StreamUtils.write(this.getCreationDate(), bos);
    }

    public static WorldPoint read(BufferedInputStream bis) throws IOException {
        WorldPoint worldPoint = new WorldPoint(
                StreamUtils.readUUID(bis),
                StreamUtils.readString(bis),
                StreamUtils.readString(bis),
                StreamUtils.readString(bis),
                StreamUtils.readInt(bis),
                StreamUtils.readFloat(bis),
                StreamUtils.readFloat(bis),
                StreamUtils.readFloat(bis),
                StreamUtils.readFloat(bis),
                StreamUtils.readFloat(bis));
        worldPoint.setId(StreamUtils.readLong(bis));
        worldPoint.setLocked(StreamUtils.readBoolean(bis));
        worldPoint.setCreationDate(StreamUtils.readString(bis));
        return worldPoint;
    }

    public void write(PacketBuffer buffer) {
        PacketBufferUtils.writeUUID(this.ownerUUID, buffer);
        PacketBufferUtils.writeString(this.ownerName, buffer);
        PacketBufferUtils.writeString(this.getName(), buffer);
        PacketBufferUtils.writeString(this.getDescription(), buffer);
        buffer.writeInt(this.getDimensionId());
        buffer.writeFloat(this.getXPos());
        buffer.writeFloat(this.getYPos());
        buffer.writeFloat(this.getZPos());
        buffer.writeFloat(this.getPitch());
        buffer.writeFloat(this.getYaw());
        buffer.writeLong(this.getId());
        buffer.writeBoolean(this.isLocked());
        PacketBufferUtils.writeString(this.getCreationDate(), buffer);
    }

    public static WorldPoint read(PacketBuffer buffer) {
        WorldPoint worldPoint = new WorldPoint(
                PacketBufferUtils.readUUID(buffer),
                PacketBufferUtils.readString(buffer),
                PacketBufferUtils.readString(buffer),
                PacketBufferUtils.readString(buffer),
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat());
        worldPoint.setId(buffer.readLong());
        worldPoint.setLocked(buffer.readBoolean());
        worldPoint.setCreationDate(PacketBufferUtils.readString(buffer));
        return worldPoint;
    }

    public enum EnumPointType {

        CAMP,
        LOCATION
    }
}
