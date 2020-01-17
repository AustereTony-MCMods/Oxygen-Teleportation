package austeretony.oxygen_teleportation.server;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData;

public class PlayersDataContainerServer {

    private final Map<UUID, TeleportationPlayerData> players = new ConcurrentHashMap<>();

    protected PlayersDataContainerServer() {}

    public Collection<TeleportationPlayerData> getPlayersData() {
        return this.players.values();
    }

    public TeleportationPlayerData createPlayerData(UUID playerUUID) {     
        TeleportationPlayerData data = new TeleportationPlayerData();
        data.setPlayerUUID(playerUUID);
        data.setPath(OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/player_data.dat");
        this.players.put(playerUUID, data);
        return data;
    }

    public void removePlayerData(UUID playerUUID) {
        this.players.remove(playerUUID);
    }

    public TeleportationPlayerData getPlayerData(UUID playerUUID) {
        return this.players.get(playerUUID);
    }

    public void save() {
        OxygenHelperServer.addRoutineTask(()->{
            for (TeleportationPlayerData playerData : this.players.values()) {
                if (playerData.isChanged()) {
                    playerData.setChanged(false);
                    OxygenHelperServer.savePersistentDataAsync(playerData);
                }
            }   
        });
    }
}
