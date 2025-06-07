package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.data.PlayerData;
import fr.kenda.fasurvie.util.Logger;
import fr.kenda.fasurvie.util.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupDataManager implements IManager {
    private static final int BUFFER_SIZE = 16 * 1024;
    private static final int MAX_BACKUPS = 30;
    private static final String ZIP_ARCHIVE = "archives_backup.zip";
    private final JavaPlugin plugin;
    private final DatabaseManager dbManager;
    private final File databaseFolder;
    private final String dbName;
    private final AtomicBoolean runningBackup = new AtomicBoolean(false);

    public BackupDataManager(JavaPlugin plugin, DatabaseManager dbManager, String dbName) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.databaseFolder = new File(plugin.getDataFolder(), "database");
        this.dbName = dbName;
    }

    public void start() {
        Calendar now = Calendar.getInstance();
        Calendar nextMonday = (Calendar) now.clone();

        // On met à lundi de la semaine prochaine à 00h00
        int daysUntilMonday = (Calendar.MONDAY - now.get(Calendar.DAY_OF_WEEK) + 7) % 7;
        if (daysUntilMonday == 0 && (
                now.get(Calendar.HOUR_OF_DAY) > 0 ||
                        now.get(Calendar.MINUTE) > 0 ||
                        now.get(Calendar.SECOND) > 0 ||
                        now.get(Calendar.MILLISECOND) > 0)) {
            // Si c'est déjà lundi dans la journée, prend LUNDI PROCHAIN
            daysUntilMonday = 7;
        }
        nextMonday.add(Calendar.DAY_OF_YEAR, daysUntilMonday);
        nextMonday.set(Calendar.HOUR_OF_DAY, 0);
        nextMonday.set(Calendar.MINUTE, 0);
        nextMonday.set(Calendar.SECOND, 0);
        nextMonday.set(Calendar.MILLISECOND, 0);

        long millisUntilNextMonday = nextMonday.getTimeInMillis() - now.getTimeInMillis();
        long seconds = millisUntilNextMonday / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        String waitStr = String.format("%sd %02dh %02dm %02ds",
                days,
                hours % 24,
                minutes % 60,
                seconds % 60);

        long ticksUntilNextMonday = millisUntilNextMonday / 50; // 1 tick = 50ms
        Logger.info("Lancement de la backup dans " + waitStr);

        // 2. Lance un runTaskLater pour le premier backup
        new BukkitRunnable() {
            @Override
            public void run() {
                asyncWeeklyReset();

                // 3. Puis toutes les 24h
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Calendar n = Calendar.getInstance();
                        if (n.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY &&
                                n.get(Calendar.HOUR_OF_DAY) == 0 &&
                                n.get(Calendar.MINUTE) == 0) {
                            asyncWeeklyReset();
                        }
                    }
                }.runTaskTimerAsynchronously(plugin, 20 * 60 * 60 * 24, 20 * 60 * 60 * 24); // 24h en ticks
            }
        }.runTaskLaterAsynchronously(plugin, ticksUntilNextMonday);
    }

    public void runTestScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                backupNowTest();
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    public void backupNowTest() {
        if (!runningBackup.compareAndSet(false, true)) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File currentDbFile = new File(databaseFolder, dbName + ".db");
                if (!currentDbFile.exists()) {
                    plugin.getLogger().warning("[Scheduler] Fichier DB absent pour backup test.");
                    return;
                }
                File backupFile = new File(databaseFolder, getTestBackupName());
                Files.copy(currentDbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                checkAndArchiveOldBackups();

                plugin.getLogger().info("[Backup/Test] Sauvegarde test réalisée: " + backupFile.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("[Backup/Test] Backup échouée: " + e.getMessage());
                e.printStackTrace();
            } finally {
                runningBackup.set(false);
            }
        });
    }

    public void asyncWeeklyReset() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File currentDbFile = new File(databaseFolder, dbName + ".db");
                if (!currentDbFile.exists()) return;

                File backupFile = new File(databaseFolder, getHebdoBackupName());
                Files.copy(currentDbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                checkAndArchiveOldBackups();

                dbManager.getConnection().createStatement().execute("DELETE FROM player_data");
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                for (Player player : players) {
                    Bukkit.getScheduler().runTask(plugin, () -> dbManager.loadData(new PlayerData(player)));
                }

                plugin.getLogger().info("[Scheduler] Reset BDD & backup hebdomadaire réalisé: " + backupFile.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("[Scheduler] Erreur reset hebdomadaire: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Archive (ajoute dans un unique .zip) toutes les .db plus vieilles que les 30 dernières,
     * puis les supprime. Le zip est incrémental.
     */
    private void checkAndArchiveOldBackups() {
        File[] saves = databaseFolder.listFiles((dir, name) ->
                name.endsWith(".db") && !name.equals(dbName + ".db"));
        if (saves == null || saves.length <= MAX_BACKUPS) return;

        Arrays.sort(saves, Comparator.comparingLong(File::lastModified));
        int toArchive = saves.length - MAX_BACKUPS;

        List<File> toZip = Arrays.asList(Arrays.copyOfRange(saves, 0, toArchive));
        if (toZip.isEmpty()) return;

        File zipFile = new File(databaseFolder, ZIP_ARCHIVE);

        // Ajout incrémental au zip
        try (FileOutputStream fos = new FileOutputStream(zipFile, true);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            // ATTENTION : on utiliser zipFs pour append proprement, car Java ne permet pas de "rajouter" à un zip natif facilement
            // Solution simple : recopier l'existant + les nouveaux (un peu plus lent, mais sûr, et on garde compatibilité universelle)
            File tmpZip = File.createTempFile("tempzip", ".zip", databaseFolder);
            if (zipFile.exists()) {
                Files.copy(zipFile.toPath(), tmpZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
                zipFile.delete();
            }
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile, false))) {
                // Re-copy old entries
                if (tmpZip.exists()) {
                    try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(tmpZip))) {
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
                    tmpZip.delete();
                }

                // Add new .db files!
                byte[] buffer = new byte[BUFFER_SIZE];
                for (File dbFile : toZip) {
                    try (FileInputStream fis = new FileInputStream(dbFile)) {
                        zos.putNextEntry(new ZipEntry(dbFile.getName()));
                        int len;
                        while ((len = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                    }
                    dbFile.delete();
                }
            }
            plugin.getLogger().info("[Backup] Archivé " + toZip.size() + " save(s) dans " + zipFile.getName());
        } catch (Exception ex) {
            plugin.getLogger().warning("[Backup] Erreur d'archivage .zip: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void register() {
    }

    @Override
    public void unregister() {
    }

    private String getHebdoBackupName() {
        Calendar cal = Calendar.getInstance();
        String pattern = "%02d_%02d_%02d_00_00_00_.db";
        return String.format(pattern,
                cal.get(Calendar.DAY_OF_MONTH) - 7,
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR) % 100
        );
    }

    private String getTestBackupName() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1);
        String pattern = "%02d_%02d_%02d_%02d_%02d_%02d.db";
        return String.format(pattern,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR) % 100,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
    }
}
