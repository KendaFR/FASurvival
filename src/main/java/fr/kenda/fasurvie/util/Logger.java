package fr.kenda.fasurvie.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {

    public static void error(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + msg);
    }

    public static void info(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + msg);
    }

    public static void success(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + msg);
    }
}
