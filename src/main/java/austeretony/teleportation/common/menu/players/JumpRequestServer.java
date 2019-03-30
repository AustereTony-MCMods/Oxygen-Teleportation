package austeretony.teleportation.common.menu.players;

import java.util.UUID;

import austeretony.teleportation.common.config.TeleportationConfig;

public class JumpRequestServer {

    private UUID visitorUUID;

    private int counter;

    private JumpRequestServer(UUID visitorUUID) {
        this.visitorUUID = visitorUUID;
        this.counter = TeleportationConfig.JUMP_REQUEST_EXPIRE_TIME.getIntValue() * 20;
    }

    public UUID getVisitorUUID() {
        return this.visitorUUID;
    }

    public static void create(UUID targetUUID, UUID visitorUUID) {
        PlayersManagerServer.instance().getJumpRequests().put(targetUUID, new JumpRequestServer(visitorUUID));
    }

    public static boolean exist(UUID targetUUID) {
        return PlayersManagerServer.instance().getJumpRequests().containsKey(targetUUID);
    }

    public static JumpRequestServer get(UUID targetUUID) {
        return PlayersManagerServer.instance().getJumpRequests().get(targetUUID);
    }

    public void reset() {
        this.counter = 0;
    }

    public boolean expired() {
        if (this.counter > 0)
            this.counter--;
        if (this.counter == 0)
            return true;
        return false;
    }
}
