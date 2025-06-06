package fr.kenda.fasurvie.data;

public class PlayerDatabase {

    private final int id;
    private final String playerName;
    private final int coins;

    public PlayerDatabase(int id, String playerName, int coins) {
        this.id = id;
        this.playerName = playerName;
        this.coins = coins;
    }

    public int getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCoins() {
        return coins;
    }
}
