package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.data.PlayerData;
import fr.kenda.fasurvie.data.PlayerDatabase;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manager fusionné pour la gestion de la base de données et des sauvegardes automatiques
 * avec remise à zéro hebdomadaire utilisant des tables de base de données.
 */
public class DatabaseManager implements IManager {

    // Constants pour les sauvegardes
    private static final int MAX_BACKUPS = 30;
    private static final long TICKS_PER_SECOND = 20L;
    private static final long TICKS_PER_MINUTE = 60L * TICKS_PER_SECOND;
    private static final long TICKS_PER_HOUR = 60L * TICKS_PER_MINUTE;
    private static final long WEEKLY_CHECK_INTERVAL = 24L * TICKS_PER_HOUR;

    // Formatters de date
    private static final DateTimeFormatter BACKUP_FORMAT = DateTimeFormatter.ofPattern("dd_MM_yy_HH_mm_ss");

    // Variables d'instance pour la base de données
    private Connection connection;
    private final JavaPlugin plugin;
    private final java.util.logging.Logger logger;
    private final String databaseName;

    // Variables d'instance pour les sauvegardes
    private final AtomicBoolean isBackupRunning = new AtomicBoolean(false);
    private BukkitTask weeklyBackupTask;
    private BukkitTask testBackupTask;

    public DatabaseManager(FASurvival plugin, String databaseName) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.logger = plugin.getLogger();
        this.databaseName = Objects.requireNonNull(databaseName, "Database name cannot be null");
    }

    @Override
    public void register() {
        try {
            this.connect();
            this.createPlayerDataTable();
            this.initializeBackupTables();
            this.logger.info("Base de données connectée avec succès !");
            this.logger.info("[BackupManager] Registered backup manager with database tables");
        } catch (SQLException e) {
            this.logger.severe("Erreur lors de la connexion à la base de données: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database system", e);
        }
    }

    @Override
    public void unregister() {
        // Annuler les tâches programmées
        if (weeklyBackupTask != null && !weeklyBackupTask.isCancelled()) {
            weeklyBackupTask.cancel();
        }
        if (testBackupTask != null && !testBackupTask.isCancelled()) {
            testBackupTask.cancel();
        }

        this.disconnect();
        this.logger.info("Base de données déconnectée.");
        this.logger.info("[BackupManager] Unregistered backup manager");
    }

    // ================================
    // MÉTHODES DE BASE DE DONNÉES
    // ================================

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
            this.initializeBackupTables();
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
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
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

        // Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" + Config.getString("database.url") +":3306/" + databaseName, Config.getString("database.user"), Config.getString("database.password"));

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

    // ================================
    // MÉTHODES DE SAUVEGARDE
    // ================================

    /**
     * Initialise les tables de sauvegarde dans la base de données
     */
    private void initializeBackupTables() {
        try (var conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Créer la table des métadonnées de sauvegarde
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS backup_metadata (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    backup_name TEXT UNIQUE NOT NULL,
                    backup_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                    backup_type TEXT NOT NULL,
                    player_count INTEGER DEFAULT 0
                )
            """);

            // Créer la table des données de sauvegarde (copie de la structure player_data)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS backup_player_data (
                    backup_id INTEGER,
                    player_id INTEGER,
                    player_name TEXT NOT NULL,
                    coins INTEGER DEFAULT 0,
                    FOREIGN KEY(backup_id) REFERENCES backup_metadata(id) ON DELETE CASCADE
                )
            """);

            // Créer des index pour de meilleures performances
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_backup_player_data_backup_id ON backup_player_data(backup_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_backup_player_data_name ON backup_player_data(player_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_backup_metadata_date ON backup_metadata(backup_date)");

            plugin.getLogger().info("[BackupManager] Tables de sauvegarde initialisées avec succès");

        } catch (SQLException e) {
            plugin.getLogger().severe("[BackupManager] Échec de l'initialisation des tables de sauvegarde: " + e.getMessage());
            throw new RuntimeException("Failed to initialize backup system", e);
        }
    }

    /**
     * Démarre le planificateur de sauvegarde hebdomadaire
     */
    public void startBackupScheduler() {
        scheduleWeeklyBackup();
        plugin.getLogger().info("[BackupManager] Planificateur de sauvegarde hebdomadaire démarré");
    }

    /**
     * Démarre un planificateur de sauvegarde de test (s'exécute chaque minute pour les tests)
     */
    public void runTestScheduler() {
        if (testBackupTask != null && !testBackupTask.isCancelled()) {
            testBackupTask.cancel();
        }

        testBackupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::performTestBackup, 0L, TICKS_PER_MINUTE);

        plugin.getLogger().info("[BackupManager] Planificateur de sauvegarde de test démarré (chaque minute)");
    }

    /**
     * Effectue une sauvegarde de test
     */
    public void performTestBackup() {
        performWeeklyReset();
    }

    /**
     * Effectue la sauvegarde hebdomadaire et la remise à zéro de la base de données
     */
    public void performWeeklyReset() {
        if (!isBackupRunning.compareAndSet(false, true)) {
            plugin.getLogger().warning("[BackupManager] Sauvegarde hebdomadaire ignorée - une autre sauvegarde est en cours");
            return;
        }
        // Vérifier le gagnant hebdomadaire
        try {
            PlayerDatabase pd = FASurvival.getInstance().getManager()
                    .getManager(DatabaseManager.class).getLeaderboard(1).get(0);
            if(pd.getCoins() > 0) {
                FASurvival.getInstance().getManager()
                        .getManager(BotManager.class)
                        .sendWeeklyWinnerEmbed(pd.getPlayerName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[BackupManager] Échec de la vérification du gagnant hebdomadaire: " + e.getMessage());
        }

        CompletableFuture.runAsync(() -> {
            try {
                // Créer une sauvegarde avant la remise à zéro
                LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
                String backupName = "weekly_" + weekAgo.format(BACKUP_FORMAT);
                createBackup(backupName, "WEEKLY");

                // Nettoyer les anciennes sauvegardes
                cleanOldBackups();

                // Remettre à zéro la base de données
                resetPlayerData();

                plugin.getLogger().info("[BackupManager] Sauvegarde hebdomadaire et remise à zéro terminées: " + backupName);

            } catch (Exception e) {
                plugin.getLogger().severe("[BackupManager] Échec de la sauvegarde hebdomadaire: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isBackupRunning.set(false);
            }
        });
    }

    /**
     * Crée une sauvegarde des données actuelles des joueurs
     */
    private void createBackup(String backupName, String backupType) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Insérer les métadonnées de sauvegarde
                String insertMetaSql = "INSERT INTO backup_metadata (backup_name, backup_type, player_count) VALUES (?, ?, ?)";
                long backupId;

                try (PreparedStatement metaStmt = conn.prepareStatement(insertMetaSql, Statement.RETURN_GENERATED_KEYS)) {
                    int playerCount = getPlayerDataCount(conn);
                    metaStmt.setString(1, backupName);
                    metaStmt.setString(2, backupType);
                    metaStmt.setInt(3, playerCount);
                    metaStmt.executeUpdate();

                    try (ResultSet rs = metaStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            backupId = rs.getLong(1);
                        } else {
                            throw new SQLException("Échec de l'obtention de l'ID de sauvegarde");
                        }
                    }
                }

                // Copier les données des joueurs dans la table de sauvegarde
                String copyDataSql = """
                    INSERT INTO backup_player_data 
                    (backup_id, player_id, player_name, coins)
                    SELECT ?, id, player_name, coins
                    FROM player_data
                """;

                try (PreparedStatement copyStmt = conn.prepareStatement(copyDataSql)) {
                    copyStmt.setLong(1, backupId);
                    int copiedRows = copyStmt.executeUpdate();

                    plugin.getLogger().info("[BackupManager] Sauvegardé " + copiedRows + " enregistrements de joueurs");
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Obtient le nombre d'enregistrements de données de joueurs
     */
    private int getPlayerDataCount(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM player_data")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Supprime les anciennes sauvegardes lorsque la limite est dépassée
     */
    private void cleanOldBackups() throws SQLException {
        try (Connection conn = getConnection()) {

            // Compter les sauvegardes actuelles
            int backupCount;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM backup_metadata")) {
                backupCount = rs.next() ? rs.getInt(1) : 0;
            }

            if (backupCount <= MAX_BACKUPS) {
                return;
            }

            // Supprimer les sauvegardes les plus anciennes
            int toDelete = backupCount - MAX_BACKUPS;
            String deleteSql = """
                DELETE FROM backup_metadata 
                WHERE id IN (
                    SELECT id FROM backup_metadata 
                    ORDER BY backup_date ASC 
                    LIMIT ?
                )
            """;

            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, toDelete);
                int deleted = stmt.executeUpdate();

                plugin.getLogger().info("[BackupManager] Nettoyé " + deleted + " ancienne(s) sauvegarde(s)");
            }
        }
    }

    /**
     * Remet à zéro les données des joueurs dans la base de données et recharge les joueurs en ligne
     */
    private void resetPlayerData() {
        try (var conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM player_data");

            // Recharger les données pour les joueurs en ligne sur le thread principal
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            for (Player player : onlinePlayers) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        loadData(new PlayerData(player));
                    } catch (Exception e) {
                        plugin.getLogger().warning("[BackupManager] Échec du rechargement des données pour le joueur " + player.getName() + ": " + e.getMessage());
                    }
                });
            }
            FASurvival.getInstance().getManager().getManager(ScoreboardManager.class).refreshLeaderboard();

            plugin.getLogger().info("[BackupManager] Remise à zéro des données des joueurs terminée, rechargé " + onlinePlayers.size() + " joueurs en ligne");

        } catch (SQLException e) {
            plugin.getLogger().severe("[BackupManager] Échec de la remise à zéro des données des joueurs: " + e.getMessage());
            throw new RuntimeException("Database reset failed", e);
        }
    }

    /**
     * Planifie la tâche de sauvegarde hebdomadaire
     */
    private void scheduleWeeklyBackup() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // Si c'est déjà lundi à minuit, planifier pour la semaine suivante
        if (now.getDayOfWeek() == DayOfWeek.MONDAY && now.getHour() == 0 && now.getMinute() == 0) {
            nextMonday = nextMonday.plusWeeks(1);
        }

        long ticksUntilNextMonday = calculateTicksUntil(nextMonday);
        String waitTime = formatDuration(ticksUntilNextMonday);

        Logger.info("Prochaine sauvegarde programmée dans: " + waitTime);

        // Planifier la sauvegarde initiale
        weeklyBackupTask = new BukkitRunnable() {
            @Override
            public void run() {
                performWeeklyReset();

                // Planifier les sauvegardes hebdomadaires récurrentes
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        LocalDateTime current = LocalDateTime.now();
                        if (current.getDayOfWeek() == DayOfWeek.MONDAY &&
                                current.getHour() == 0 &&
                                current.getMinute() == 0) {

                            performWeeklyReset();
                        }
                    }
                }.runTaskTimerAsynchronously(plugin, WEEKLY_CHECK_INTERVAL, WEEKLY_CHECK_INTERVAL);
            }
        }.runTaskLaterAsynchronously(plugin, ticksUntilNextMonday);
    }

    /**
     * Calcule les ticks jusqu'à l'heure spécifiée
     */
    private long calculateTicksUntil(LocalDateTime target) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(now, target).getSeconds();
        return seconds * TICKS_PER_SECOND;
    }

    /**
     * Formate la durée en ticks en chaîne lisible par l'homme
     */
    private String formatDuration(long ticks) {
        long totalSeconds = ticks / TICKS_PER_SECOND;
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds);
    }
}