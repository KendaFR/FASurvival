package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DataManager
        implements IManager {
    private List<PlayerData> playerDataList;

    @Override
    public void register() {
        this.playerDataList = new ArrayList<PlayerData>();
    }

    @Override
    public void unregister() {
        this.playerDataList.clear();
    }

    public void addPlayerData(PlayerData playerData) {
        this.playerDataList.add(playerData);
    }

    public void removePlayerData(PlayerData playerData) {
        this.playerDataList.remove(playerData);
    }

    public PlayerData getPlayerDataFromPlayer(Player player) {
        return this.playerDataList.stream().filter(playerData -> playerData.getPlayer() == player).findFirst().orElse(null);
    }

    public void recreate() {
        this.playerDataList.forEach(playerData -> {
            playerData.setCoins(0);
            playerData.saveData(false);
        });
    }

    public List<PlayerData> getPlayerDataList() {
        return this.playerDataList;
    }
}

