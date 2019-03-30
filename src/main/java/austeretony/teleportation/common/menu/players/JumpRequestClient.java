package austeretony.teleportation.common.menu.players;

import austeretony.teleportation.common.config.TeleportationConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class JumpRequestClient {

    private String visitorUsername;

    private int counter;

    private boolean recentlyStarted;

    public JumpRequestClient() {}

    public String getVisitorUsername() {
        return this.visitorUsername;
    }

    public int getElapsedTime() {
        return this.counter / 20;
    }

    public void reset() {
        this.counter = 0;
    }

    public boolean process() {
        if (this.counter > 0) {
            this.counter--;
            if (this.counter == 0)
                return true;
        }
        return false;
    }

    public boolean exist() {
        return this.counter != 0;
    }

    public void start(String visitorUsername) {
        this.visitorUsername = visitorUsername;
        this.counter = TeleportationConfig.JUMP_REQUEST_EXPIRE_TIME.getIntValue() * 20;
        this.recentlyStarted = true;
    }

    public boolean recentlyStarted() {
        if (this.recentlyStarted) {
            this.recentlyStarted = false;
            return true;
        }
        return false;
    }
}
