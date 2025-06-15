package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class FileManager
        implements IManager {
    private final FASurvival instance = FASurvival.getInstance();
    private final HashMap<String, FileConfiguration> files = new HashMap();

    @Override
    public void register() {
        this.createFile("kits");
        this.createFile("whitelist");
    }

    @Override
    public void unregister() {
    }

    public void createFile(String fileName) {
        File file = new File(this.instance.getDataFolder(), fileName + ".yml");
        if (!file.exists()) {
            this.instance.saveResource(fileName + ".yml", false);
        }
        YamlConfiguration configFile = YamlConfiguration.loadConfiguration(file);
        this.files.put(fileName, configFile);
    }

    public FileConfiguration getConfigFrom(String fileName) {
        return this.files.get(fileName);
    }

    public boolean saveConfigFrom(String fileName) {
        File file = new File(this.instance.getDataFolder(), fileName + ".yml");
        try {
            this.files.get(fileName).save(file);
            return true;
        } catch (IOException e) {
            Logger.error(e.getMessage());
            return false;
        }
    }
}

