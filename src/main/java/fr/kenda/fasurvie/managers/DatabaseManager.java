package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.data.PlayerData;
import fr.kenda.fasurvie.data.PlayerDatabase;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
            this.connect();
            this.createPlayerDataTable();
            this.logger.info("Base de données SQLite connectée avec succès !");
        } catch (SQLException e) {
            this.logger.severe("Erreur lors de la connexion à la base de données: " + e.getMessage());
        }
    }

    @Override
    public void unregister() {
        this.disconnect();
        this.logger.info("Base de données SQLite déconnectée.");
    }

    public void clean() {
        try {
            this.disconnect();
            this.logger.info("Déconnexion de la base de données effectuée.");
            File databaseFolder = new File(this.plugin.getDataFolder(), "database");
            if (databaseFolder.exists()) {
                this.deleteFolder(databaseFolder);
                this.logger.info("Dossier 'database' supprimé avec succès.");
            } else {
                this.logger.info("Le dossier 'database' n'existe pas, rien à supprimer.");
            }
            this.connect();
            this.createPlayerDataTable();
            this.logger.info("Base de données nettoyée et reconnectée avec succès !");
        } catch (SQLException e) {
            this.logger.severe("Erreur lors du nettoyage de la base de données: " + e.getMessage());
        }
    }

    public void loadData(PlayerData playerData) {
        String query = "SELECT * FROM player_data WHERE player_name = ?";
        try (PreparedStatement pstmt = this.getConnection().prepareStatement(query)) {
            pstmt.setString(1, playerData.getPlayer().getName());
            try (ResultSet result = pstmt.executeQuery()) {
                if (result.next()) {
                    playerData.setCoins(result.getInt("coins"));
                    this.logger.info("Joueur trouvé: " + playerData.getPlayer().getName());
                } else {
                    this.createPlayerEntry(playerData.getPlayer().getName());
                    this.logger.info("Nouveau joueur créé: " + playerData.getPlayer().getName());
                    playerData.setCoins(0);
                }
            }
        } catch (SQLException e) {
            this.logger.severe("Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    public void saveData(PlayerData playerData) {
        String query = "UPDATE player_data SET coins = ? WHERE player_name = ?";
        try (PreparedStatement pstmt = this.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, playerData.getCoins());
            pstmt.setString(2, playerData.getPlayer().getName());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                this.logger.warning("Aucune donnée mise à jour pour le joueur: " + playerData.getPlayer().getName());
            }
        } catch (SQLException e) {
            this.logger.severe("Erreur lors de la sauvegarde des données: " + e.getMessage());
        }
    }

    public List<PlayerDatabase> getLeaderboard(int limit) {
        String query = "SELECT * FROM player_data ORDER BY coins DESC LIMIT ?";
        List<PlayerDatabase> leaderboard = new ArrayList<>();

        try (PreparedStatement pstmt = this.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, limit);
            try (ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    leaderboard.add(new PlayerDatabase(
                            result.getInt("id"),
                            result.getString("player_name"),
                            result.getInt("coins")
                    ));
                }
            }
        } catch (SQLException e) {
            this.logger.severe("Erreur lors de la récupération du classement: " + e.getMessage());
            return new ArrayList<>();
        }

        return leaderboard;
    }

    public void createPlayerDataTable() throws SQLException {
        String createTable = "CREATE TABLE IF NOT EXISTS player_data (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_name VARCHAR(36) NOT NULL UNIQUE, " +
                "coins BIGINT DEFAULT 0)";

        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(createTable);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_name ON player_data(player_name)");
            this.logger.info("Table 'player_data' créée avec succès.");
        }
    }

    public Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connect();
        }
        return this.connection;
    }

    private void deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        this.deleteFolder(file);
                    } else {
                        if (!file.delete()) {
                            this.logger.warning("Impossible de supprimer le fichier: " + file.getAbsolutePath());
                        }
                    }
                }
            }
            if (!folder.delete()) {
                this.logger.warning("Impossible de supprimer le dossier: " + folder.getAbsolutePath());
            }
        }
    }

    private void createPlayerEntry(String playerName) {
        String query = "INSERT INTO player_data (player_name, coins) VALUES (?, 0)";
        try (PreparedStatement pstmt = this.getConnection().prepareStatement(query)) {
            pstmt.setString(1, playerName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            this.logger.severe("Erreur lors de la création de l'entrée joueur: " + e.getMessage());
        }
    }

    private void connect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return;
        }

        File dataFolder = this.plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File databaseFolder = new File(dataFolder, "database");
        if (!databaseFolder.exists()) {
            databaseFolder.mkdirs();
        }

        String path = databaseFolder.getAbsolutePath() + File.separator + this.databaseName + ".db";

        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + path);

            // Configuration des paramètres SQLite pour de meilleures performances
            try (Statement stmt = this.connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA synchronous = NORMAL");
                stmt.execute("PRAGMA cache_size = 10000");
                stmt.execute("PRAGMA temp_store = MEMORY");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite non trouvé", e);
        }
    }

    private void disconnect() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
                this.connection = null;
            }
        } catch (SQLException e) {
            this.logger.warning("Erreur lors de la fermeture de la base de données: " + e.getMessage());
        }
    }
}