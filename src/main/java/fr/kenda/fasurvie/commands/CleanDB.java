package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.managers.DatabaseManager;
import fr.kenda.fasurvie.managers.Managers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CleanDB
        implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args2, boolean isForced) {
        if (args2.length > 0) {
            this.sendHelp(sender);
            return false;
        }
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Cleaning database. Please reconnect."));
        Managers managers = FASurvival.getInstance().getManager();
        managers.getManager(DatabaseManager.class).clean();
        return false;
    }

    @Override
    public String getName() {
        return "clean";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + ": " + ChatColor.RED + "Supprime et recrée la base de donnée.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args2) {
        return SubCommand.super.onTabComplete(sender, args2);
    }
}

