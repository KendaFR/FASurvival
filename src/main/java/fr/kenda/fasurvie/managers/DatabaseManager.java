package fr.kenda.fasurvie.managers;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.data.PlayerData;
import fr.kenda.fasurvie.data.PlayerDatabase;
import org.bukkit.plugin.java.JavaPlugin;
public class DatabaseManager implements IManager {

    private Connection connection;
    private final JavaPlugin plugin;
    private final Logger logger;
    private final String databaseName;

    public DatabaseManager(FASurvival plugin, String databaseName) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.databaseName = databaseName;
    }

    @Override
    public void register() {
        try {
            connect();
            createPlayerDataTable();
            logger.info("Base de données SQLite connectée avec succès !");
        } catch (SQLException e) {
            logger.severe("Erreur lors de la connexion à la base de données: " + e.getMessage());
        }
    }

    @Override
    public void unregister() {
        disconnect();
        logger.info("Base de données SQLite déconnectée.");
    }

    public void loadData(PlayerData playerData) {
        String query = "SELECT * FROM player_data WHERE player_name = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, playerData.getPlayer().getName());

            try (ResultSet result = pstmt.executeQuery()) {
                if (result.next()) {
                    playerData.setCoins(result.getInt("coins"));
                    logger.info("Found player !");
                } else {
                    createPlayerEntry(playerData.getPlayer().getName());
                    logger.info("Not player !");
                    playerData.setCoins(0);
                }
            }
        } catch (SQLException e) {
            logger.severe("Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    public void saveData(PlayerData playerData) {
        String query = "UPDATE player_data SET coins = ?  WHERE player_name = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(2, playerData.getPlayer().getName());
            pstmt.setInt(1, playerData.getCoins());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Erreur lors de la sauvegarde des données: " + e.getMessage());
        }
    }

    /**
     * Crée une entrée pour un nouveau joueur
     */
    private void createPlayerEntry(String playerName) {
        String query = "INSERT INTO player_data (player_name, coins) VALUES (?, 0)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, playerName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Erreur lors de la création de l'entrée joueur: " + e.getMessage());
        }
    }

    /**
     * Établit la connexion à la base de données SQLite
     */
    private void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File databaseFolder = new File(dataFolder, "database");
        if (!databaseFolder.exists()) {
            databaseFolder.mkdirs();
        }


        // Chemin vers le fichier de base de données
        String path = databaseFolder.getAbsolutePath() + File.separator + databaseName + ".db";

        try {
            // Charger le driver SQLite
            Class.forName("org.sqlite.JDBC");

            // Établir la connexion
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);

            // Optimisations pour SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA synchronous = NORMAL");
            }

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite non trouvé", e);
        }
    }

    public List<PlayerDatabase> getLeaderboard(int limit) {
        String query = "SELECT * FROM player_data  ORDER BY coins DESC LIMIT ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            List<PlayerDatabase> names = new ArrayList<>();
            pstmt.setInt(1, limit);
            ResultSet result = pstmt.executeQuery();
            while (result.next())
                names.add(new PlayerDatabase(result.getInt("id"), result.getString("player_name"), result.getInt("coins")));
            return names;
        } catch (SQLException e) {
            logger.severe("Erreur lors de la création de l'entrée joueur: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Ferme la connexion à la base de données
     */
    private void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.warning("Erreur lors de la fermeture de la base de données: " + e.getMessage());
        }
    }

    /**
     * Crée la table player_data si elle n'existe pas
     */
    public void createPlayerDataTable() throws SQLException {
        String createTable = "CREATE TABLE IF NOT EXISTS player_data (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_name VARCHAR(36) NOT NULL UNIQUE, " +
                "coins BIGINT DEFAULT 0" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_name ON player_data(player_name)");

            logger.info("Table 'player_data' créée avec succès.");
        }
    }

    /**
     * Obtient la connexion active
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }
}