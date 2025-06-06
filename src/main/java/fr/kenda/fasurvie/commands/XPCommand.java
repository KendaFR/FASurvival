package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class XPCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sendHelp(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX +ChatColor.RED + "Joueur introuvable.");
            return true;
        }

        String type = args[1].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
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
            sendHelp(sender);
        }
        return true;
    }

    @Override
    public String getName() {
        return "xp";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() + " <player> level <amount>: " + ChatColor.RED + "Donne des niveaux au joueur");
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() + " <player> xp <amount>:" + ChatColor.RED + "Donne de l'xp au joueur");
    }
}
