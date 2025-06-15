package fr.kenda.fasurvie.util;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final FASurvival INSTANCE = FASurvival.getInstance();
    private static final FileConfiguration CONFIG = INSTANCE.getConfig();

    public static int getInt(String path) {
        return INSTANCE.getConfig().getInt(path);
    }

    public static Material getMaterial(String path) {
        String str = CONFIG.getString(path);
        Material mat = Material.getMaterial(str);
        if (mat != null) {
            return mat;
        }
        return Material.BARRIER;
    }

    public static List<String> getList(String path) {
        List<String> lores = CONFIG.getStringList(path);
        ArrayList<String> colorLores = new ArrayList<>();
        lores.forEach(s -> colorLores.add(Config.transformColor(s)));
        return colorLores;
    }

    public static String transformColor(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static List<String> getList(String path, String... args2) {
        List lores = CONFIG.getStringList(path);
        ArrayList<String> colorLores = new ArrayList<String>();
        for (int i = 0; i < lores.size(); ++i) {
            String line = (String) lores.get(i);
            for (int y = 0; y < args2.length; y += 2) {
                line = line.replace(args2[y], args2[y + 1]);
            }
            colorLores.add(Config.transformColor(line));
        }
        return colorLores;
    }

    public static String getString(String path, String... args2) {
        String configStr = CONFIG.getString(path);
        if (configStr == null) {
            return "";
        }
        for (int i = 0; i < args2.length; i += 2) {
            configStr = configStr.replace(args2[i], args2[i + 1]);
        }
        return Config.transformColor(configStr);
    }

    public static boolean getBoolean(String path) {
        return CONFIG.getBoolean(path);
    }
}

