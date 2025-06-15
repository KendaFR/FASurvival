package fr.kenda.fasurvie;

import fr.kenda.fasurvie.managers.BackupDataManager;
import fr.kenda.fasurvie.managers.Managers;
import fr.kenda.fasurvie.updater.PluginUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class FASurvival
        extends JavaPlugin {
    public static final String PREFIX = ChatColor.WHITE + "[" + ChatColor.AQUA + "FreshAgency" + ChatColor.WHITE + "] ";
    private static FASurvival instance;
    private Managers manager;

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        if (this.getConfig().getBoolean("check_update", false)) {
            new PluginUpdater().checkForUpdates();
        }
        this.manager = new Managers();
        Bukkit.getScheduler().runTaskLater(this, () -> this.manager.getManager(BackupDataManager.class).start(), 60L);
    }

    public void onDisable() {
        this.manager.unregisterManagers();
        instance = null;
    }

    public static FASurvival getInstance() {
        return instance;
    }

    public Managers getManager() {
        return this.manager;
    }
}

