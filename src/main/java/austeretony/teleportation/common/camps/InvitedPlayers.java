package austeretony.teleportation.common.camps;

import java.util.Set;
import java.util.UUID;

import io.netty.util.internal.ConcurrentSet;

public class InvitedPlayers {

    private final Set<UUID> players = new ConcurrentSet<UUID>();

    public Set<UUID> getPlayers() {
        return this.players;
    }
}
