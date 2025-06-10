package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DataManager implements IManager {

    private List<PlayerData> playerDataList;

    @Override
    public void register() {
        playerDataList = new ArrayList<>();
    }

    @Override
    public void unregister() {
        playerDataList.clear();
    }

    public void addPlayerData(PlayerData playerData) {
        playerDataList.add(playerData);
    }

    public void removePlayerData(PlayerData playerData) {
        playerDataList.remove(playerData);
    }

    public PlayerData getPlayerDataFromPlayer(Player player) {
        return playerDataList.stream().filter(playerData -> playerData.getPlayer() == player).findFirst().orElse(null);
    }

    public void recreate() {
        playerDataList.forEach(playerData ->
        {
            playerData.setCoins(0);
            playerData.saveData(false);
        });
    }

    public List<PlayerData> getPlayerDataList() {
        return playerDataList;
    }
}