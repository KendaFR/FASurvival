package fr.kenda.fasurvie.data;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.managers.DataManager;
import fr.kenda.fasurvie.managers.DatabaseManager;
import fr.kenda.fasurvie.managers.Managers;
import fr.kenda.fasurvie.util.Logger;
import org.bukkit.entity.Player;

public class PlayerData {

    private final Player player;
    private int coins;

    public PlayerData(Player player) {
        this.player = player;
        this.coins = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int coins) {
        this.coins += coins;
    }

    public void removeCoins(int coins) {
        this.coins -= coins;
    }
    public void loadData()
    {
        Managers managers = FASurvival.getInstance().getManager();
        managers.getManager(DatabaseManager.class).loadData(this);
        managers.getManager(DataManager.class).addPlayerData(this);
    }

    public void saveData(boolean removeDataAfter) {
        Managers managers = FASurvival.getInstance().getManager();
        managers.getManager(DatabaseManager.class).saveData(this);
        if (removeDataAfter)
            managers.getManager(DataManager.class).removePlayerData(this);
        Logger.success("Data of " + player.getName() + " saved !");
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}