package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.data.PlayerData;
import fr.kenda.fasurvie.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Manager for handling database backups with weekly automatic resets
 * and archiving of old backup files.
 */
public class BackupDataManager implements IManager {

    // Constants
    private static final int BUFFER_SIZE = 16384;
    private static final int MAX_BACKUPS = 30;
    private static final String ZIP_ARCHIVE = "archives_backup.zip";
    private static final String DB_EXTENSION = ".db";
    private static final long TICKS_PER_SECOND = 20L;
    private static final long TICKS_PER_MINUTE = 60L * TICKS_PER_SECOND;
    private static final long TICKS_PER_HOUR = 60L * TICKS_PER_MINUTE;
    private static final long WEEKLY_CHECK_INTERVAL = 24L * TICKS_PER_HOUR; // Check every 24 hours

    // Date formatters
    private static final DateTimeFormatter WEEKLY_BACKUP_FORMAT = DateTimeFormatter.ofPattern("dd_MM_yy_00_00_00");
    private static final DateTimeFormatter TEST_BACKUP_FORMAT = DateTimeFormatter.ofPattern("dd_MM_yy_HH_mm_ss");

    // Instance variables
    private final JavaPlugin plugin;
    private final DatabaseManager dbManager;
    private final File databaseFolder;
    private final String dbName;
    private final AtomicBoolean isBackupRunning = new AtomicBoolean(false);

    // Scheduled tasks
    private BukkitTask weeklyBackupTask;
    private BukkitTask testBackupTask;

    public BackupDataManager(JavaPlugin plugin, DatabaseManager dbManager, String dbName) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.dbManager = Objects.requireNonNull(dbManager, "DatabaseManager cannot be null");
        this.dbName = Objects.requireNonNull(dbName, "Database name cannot be null");
        this.databaseFolder = new File(plugin.getDataFolder(), "database");

        // Ensure database folder exists
        if (!databaseFolder.exists() && !databaseFolder.mkdirs()) {
            throw new IllegalStateException("Cannot create database folder: " + databaseFolder.getAbsolutePath());
        }
    }

    @Override
    public void register() {
        // Implementation depends on IManager interface requirements
        plugin.getLogger().info("[BackupManager] Registered backup manager");
    }

    @Override
    public void unregister() {
        // Cancel scheduled tasks
        if (weeklyBackupTask != null && !weeklyBackupTask.isCancelled()) {
            weeklyBackupTask.cancel();
        }
        if (testBackupTask != null && !testBackupTask.isCancelled()) {
            testBackupTask.cancel();
        }

        plugin.getLogger().info("[BackupManager] Unregistered backup manager");
    }

    /**
     * Starts the weekly backup scheduler
     */
    public void start() {
        scheduleWeeklyBackup();
        plugin.getLogger().info("[BackupManager] Weekly backup scheduler started");
    }

    /**
     * Starts a test backup scheduler (runs every second for testing)
     */
    public void runTestScheduler() {
        if (testBackupTask != null && !testBackupTask.isCancelled()) {
            testBackupTask.cancel();
        }

        testBackupTask = new BukkitRunnable() {
            @Override
            public void run() {
                performTestBackup();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, TICKS_PER_SECOND);

        plugin.getLogger().info("[BackupManager] Test backup scheduler started");
    }

    /**
     * Performs a test backup
     */
    public void performTestBackup() {
        if (!isBackupRunning.compareAndSet(false, true)) {
            return; // Backup already running
        }

        CompletableFuture.runAsync(() -> {
            try {
                File currentDbFile = getCurrentDatabaseFile();
                if (!currentDbFile.exists()) {
                    plugin.getLogger().warning("[BackupManager] Database file not found for test backup: " + currentDbFile.getPath());
                    return;
                }

                String backupName = generateTestBackupName();
                File backupFile = new File(databaseFolder, backupName);

                Files.copy(currentDbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                archiveOldBackups();

                plugin.getLogger().info("[BackupManager] Test backup completed: " + backupFile.getName());

            } catch (Exception e) {
                plugin.getLogger().severe("[BackupManager] Test backup failed: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isBackupRunning.set(false);
            }
        });
    }

    /**
     * Performs weekly backup and database reset
     */
    public void performWeeklyReset() {
        if (!isBackupRunning.compareAndSet(false, true)) {
            plugin.getLogger().warning("[BackupManager] Weekly backup skipped - another backup is running");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                // Create backup
                File currentDbFile = getCurrentDatabaseFile();
                if (!currentDbFile.exists()) {
                    plugin.getLogger().warning("[BackupManager] Database file not found for weekly backup");
                    return;
                }

                String backupName = generateWeeklyBackupName();
                File backupFile = new File(databaseFolder, backupName);

                Files.copy(currentDbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                archiveOldBackups();

                // Reset database
                resetPlayerData();

                plugin.getLogger().info("[BackupManager] Weekly backup and reset completed: " + backupFile.getName());

            } catch (Exception e) {
                plugin.getLogger().severe("[BackupManager] Weekly backup failed: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isBackupRunning.set(false);
            }
        });
    }

    /**
     * Schedules the weekly backup task
     */
    private void scheduleWeeklyBackup() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // If it's already Monday at midnight, schedule for next week
        if (now.getDayOfWeek() == DayOfWeek.MONDAY && now.getHour() == 0 && now.getMinute() == 0) {
            nextMonday = nextMonday.plusWeeks(1);
        }

        long ticksUntilNextMonday = calculateTicksUntil(nextMonday);
        String waitTime = formatDuration(ticksUntilNextMonday);

        Logger.info("Next backup scheduled in: " + waitTime);

        // Schedule initial backup
        weeklyBackupTask = new BukkitRunnable() {
            @Override
            public void run() {
                performWeeklyReset();

                // Schedule recurring weekly backups
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        LocalDateTime current = LocalDateTime.now();
                        if (current.getDayOfWeek() == DayOfWeek.MONDAY &&
                                current.getHour() == 0 &&
                                current.getMinute() == 0) {
                            FASurvival.getInstance().getManager().getManager(BotManager.class).
                                    sendWeeklyWinnerEmbed(
                                            FASurvival.getInstance().getManager().getManager(DatabaseManager.class).getLeaderboard(1).get(0).getPlayerName());
                            performWeeklyReset();
                        }
                    }
                }.runTaskTimerAsynchronously(plugin, WEEKLY_CHECK_INTERVAL, WEEKLY_CHECK_INTERVAL);
            }
        }.runTaskLaterAsynchronously(plugin, ticksUntilNextMonday);
    }

    /**
     * Archives old backup files when the limit is exceeded
     */
    private void archiveOldBackups() {
        try {
            File[] backupFiles = getBackupFiles();
            if (backupFiles.length <= MAX_BACKUPS) {
                return;
            }

            // Sort by modification time (oldest first)
            Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

            int filesToArchive = backupFiles.length - MAX_BACKUPS;
            List<File> filesToZip = Arrays.stream(backupFiles)
                    .limit(filesToArchive)
                    .collect(Collectors.toList());

            if (filesToZip.isEmpty()) {
                return;
            }

            File zipFile = new File(databaseFolder, ZIP_ARCHIVE);
            archiveFiles(filesToZip, zipFile);

            // Delete archived files
            filesToZip.forEach(File::delete);

            plugin.getLogger().info("[BackupManager] Archived " + filesToZip.size() + " backup(s) to " + zipFile.getName());

        } catch (Exception e) {
            plugin.getLogger().warning("[BackupManager] Failed to archive old backups: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Archives files to a ZIP archive
     */
    private void archiveFiles(List<File> filesToArchive, File zipFile) throws IOException {
        File tempZip = null;

        try {
            // Create temporary file for existing archive content
            if (zipFile.exists()) {
                tempZip = File.createTempFile("backup_temp", ".zip", databaseFolder);
                Files.copy(zipFile.toPath(), tempZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
                zipFile.delete();
            }

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {

                // Copy existing archive content
                if (tempZip != null && tempZip.exists()) {
                    copyExistingZipContent(tempZip, zos);
                }

                // Add new files to archive
                addFilesToZip(filesToArchive, zos);
            }

        } finally {
            if (tempZip != null && tempZip.exists()) {
                tempZip.delete();
            }
        }
    }

    /**
     * Copies existing ZIP content to new ZIP output stream
     */
    private void copyExistingZipContent(File existingZip, ZipOutputStream zos) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(existingZip))) {
            ZipEntry entry;
            byte[] buffer = new byte[BUFFER_SIZE];

            while ((entry = zis.getNextEntry()) != null) {
                zos.putNextEntry(new ZipEntry(entry.getName()));

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                zos.closeEntry();
                zis.closeEntry();
            }
        }
    }

    /**
     * Adds files to ZIP output stream
     */
    private void addFilesToZip(List<File> files, ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];

        for (File file : files) {
            try (FileInputStream fis = new FileInputStream(file)) {
                zos.putNextEntry(new ZipEntry(file.getName()));

                int len;
                while ((len = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }

                zos.closeEntry();
            }
        }
    }

    /**
     * Resets player data in the database and reloads online players
     */
    private void resetPlayerData() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM player_data");

            // Reload data for online players on main thread
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            for (Player player : onlinePlayers) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        dbManager.loadData(new PlayerData(player));
                    } catch (Exception e) {
                        plugin.getLogger().warning("[BackupManager] Failed to reload data for player " + player.getName() + ": " + e.getMessage());
                    }
                });
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("[BackupManager] Failed to reset player data: " + e.getMessage());
            throw new RuntimeException("Database reset failed", e);
        }
    }

    /**
     * Gets all backup files in the database folder
     */
    private File[] getBackupFiles() {
        File[] files = databaseFolder.listFiles((dir, name) ->
                name.endsWith(DB_EXTENSION) && !name.equals(dbName + DB_EXTENSION));
        return files != null ? files : new File[0];
    }

    /**
     * Gets the current database file
     */
    private File getCurrentDatabaseFile() {
        return new File(databaseFolder, dbName + DB_EXTENSION);
    }

    /**
     * Generates a filename for weekly backup
     */
    private String generateWeeklyBackupName() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        return weekAgo.format(WEEKLY_BACKUP_FORMAT) + DB_EXTENSION;
    }

    /**
     * Generates a filename for test backup
     */
    private String generateTestBackupName() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        return now.format(TEST_BACKUP_FORMAT) + DB_EXTENSION;
    }

    /**
     * Calculates ticks until specified time
     */
    private long calculateTicksUntil(LocalDateTime target) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(now, target).getSeconds();
        return seconds * TICKS_PER_SECOND;
    }

    /**
     * Formats duration in ticks to human-readable string
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