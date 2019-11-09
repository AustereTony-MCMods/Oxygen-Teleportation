package austeretony.oxygen_teleportation.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import austeretony.oxygen_core.common.sync.SynchronizedData;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;

public class WorldPoint implements SynchronizedData {

    public static final int 
    MAX_NAME_LENGTH = 20,
    MAX_DESCRIPTION_LENGTH = 400;

    public static final DateFormat CREATED_DATE_FORMAT = new SimpleDateFormat("d MM yyyy");

    private long id;

    private UUID ownerUUID;

    private String ownerName;

    private String name, desc;

    private float yaw, pitch, xPos, yPos, zPos;

    private int dimension;

    private boolean locked;

    public WorldPoint() {}

    public WorldPoint(long id, UUID ownerUUID, String ownerName, String name, String description, int dimension, float xPos, float yPos, float zPos, float yaw, float pitch) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;	
        this.name = name;		
        this.desc = description;
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

    public UUID getOwnerUUID() {                
        return this.ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {          
        this.ownerUUID = ownerUUID;
    }

    public String getOwnerName() {                
        return this.ownerName;
    }

    public void setOwnerName(String ownerName) {          
        this.ownerName = ownerName;
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
        return CREATED_DATE_FORMAT.format(new Date(this.id));
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

    public void setPosition(float x, float y, float z, int dimension, float yaw, float pitch) {
        this.xPos = x;
        this.yPos = y;
        this.zPos = z;
        this.dimension = dimension;
        this.yaw = yaw;
        this.pitch = pitch;
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
        StreamUtils.write(this.id, bos);
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
    }

    public static WorldPoint read(BufferedInputStream bis) throws IOException {
        WorldPoint worldPoint = new WorldPoint(
                StreamUtils.readLong(bis),
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
        return worldPoint;
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(this.id);
        ByteBufUtils.writeUUID(this.ownerUUID, buffer);
        ByteBufUtils.writeString(this.ownerName, buffer);
        ByteBufUtils.writeString(this.getName(), buffer);
        ByteBufUtils.writeString(this.getDescription(), buffer);
        buffer.writeInt(this.getDimensionId());
        buffer.writeFloat(this.getXPos());
        buffer.writeFloat(this.getYPos());
        buffer.writeFloat(this.getZPos());
        buffer.writeFloat(this.getPitch());
        buffer.writeFloat(this.getYaw());
        buffer.writeBoolean(this.isLocked());
    }

    @Override
    public void read(ByteBuf buffer) {
        this.id = buffer.readLong();
        this.ownerUUID = ByteBufUtils.readUUID(buffer);
        this.ownerName = ByteBufUtils.readString(buffer);
        this.name = ByteBufUtils.readString(buffer);
        this.desc = ByteBufUtils.readString(buffer);
        this.dimension = buffer.readInt();
        this.xPos = buffer.readFloat();
        this.yPos = buffer.readFloat();
        this.zPos = buffer.readFloat();
        this.yaw = buffer.readFloat();
        this.pitch = buffer.readFloat();
        this.locked = buffer.readBoolean();
    }

    public enum EnumWorldPoint {

        CAMP,
        LOCATION
    }
}
