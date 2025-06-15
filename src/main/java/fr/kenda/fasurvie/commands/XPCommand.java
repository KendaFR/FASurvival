package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class XPCommand
        implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args2, boolean isForced) {
        int amount;
        if (args2.length != 3) {
            this.sendHelp(sender);
            return true;
        }
        Player target = Bukkit.getPlayer(args2[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        String type = args2[1].toLowerCase();
        try {
            amount = Integer.parseInt(args2[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Nombre invalide.");
            return true;
        }
        if (type.equals("level")) {
            target.giveExpLevels(amount);
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + target.getName() + " reçoit " + amount + " niveaux.");
        } else if (type.equals("xp")) {
            target.giveExp(amount);
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + target.getName() + " reçoit " + amount + " points d'XP brut.");
        } else {
            this.sendHelp(sender);
        }
        return true;
    }

    @Override
    public String getName() {
        return "xp";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " <player> level <amount>: " + ChatColor.RED + "Donne des niveaux au joueur");
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " <player> xp <amount>:" + ChatColor.RED + "Donne de l'xp au joueur");
    }
}

