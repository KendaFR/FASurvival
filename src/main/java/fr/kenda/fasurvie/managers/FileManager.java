package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.FileName;
import fr.kenda.fasurvie.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class FileManager implements IManager {

    private final FASurvival instance = FASurvival.getInstance();
    private final HashMap<String, FileConfiguration> files = new HashMap<>();

    /**
     * Register and create all files
     */
    @Override
    public void register() {
        createFile(FileName.KIT_FILE);
    }

    @Override
    public void unregister() {

    }

    /**
     * Create file
     *
     * @param fileName String
     */
    public void createFile(String fileName) {
        final File file = new File(instance.getDataFolder(), fileName + ".yml");

        if (!file.exists()) {
            instance.saveResource(fileName + ".yml", false);
        }

        FileConfiguration configFile = YamlConfiguration.loadConfiguration(file);
        files.put(fileName, configFile);
    }

    /**
     * Get configuration from file
     *
     * @param fileName String
     * @return FileConfiguration
     */
    public FileConfiguration getConfigFrom(String fileName) {
        return files.get(fileName);
    }

    /**
     * Save configuration from file
     *
     * @param fileName String
     */
    public boolean saveConfigFrom(String fileName) {
        final File file = new File(instance.getDataFolder(), fileName + ".yml");
        try {
            files.get(fileName).save(file);
            return true;
        } catch (IOException e) {
            Logger.error(e.getMessage());
            return false;
        }
    }
}
