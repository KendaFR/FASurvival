package fr.kenda.fasurvie;

import fr.kenda.fasurvie.updater.PluginUpdater;
import org.bukkit.plugin.java.JavaPlugin;

public final class FASurvival extends JavaPlugin {

    private static FASurvival instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        if (getConfig().getBoolean("check_update", false))
            new PluginUpdater().checkForUpdates();
    }

    @Override
    public void onDisable() {

    }

    public static FASurvival getInstance() {
        return instance;
    }
}