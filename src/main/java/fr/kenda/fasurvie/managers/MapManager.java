package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapManager
        implements IManager {
    @Override
    public void register() {
    }

    @Override
    public void unregister() {
    }

    public void setBorder(CommandSender sender, int centerX, int centerZ, int size) {
        WorldBorder border = this.getWorldFromSender(sender).getWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(size);
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Border centrée en (" + centerX + ", " + centerZ + ") avec taille " + size);
    }

    private World getWorldFromSender(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getWorld();
        }
        return Bukkit.getWorlds().get(0);
    }
}

