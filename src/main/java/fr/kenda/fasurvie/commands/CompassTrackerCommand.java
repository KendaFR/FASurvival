package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class CompassTrackerCommand
        implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args2, boolean isForced) {
        if (args2.length != 1) {
            this.sendHelp(sender);
            return false;
        }
        Player target = Bukkit.getPlayer(args2[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        int maxDurability = FASurvival.getInstance().getConfig().getInt("max_durability");
        target.getInventory().addItem(new ItemBuilder(Material.COMPASS).displayName(ChatColor.RED + "Tracker").enchant(Enchantment.ARROW_INFINITE, 1).lore(ChatColor.GRAY + "Faites un clic droit pour marquer le joueur", ChatColor.GRAY + "le plus proche.", ChatColor.RED + "" + ChatColor.BOLD + "Attention, chaque utilisation coûtera", ChatColor.RED + "" + ChatColor.BOLD + "de la durabilité !", "", ChatColor.GRAY + "(" + maxDurability + "/" + maxDurability + ")").build());
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Un tracker vous a été donné !");
        return true;
    }

    @Override
    public String getName() {
        return "tracker";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " <player>: " + ChatColor.RED + "Donne une boussole qui indique le joueur le plus proche.");
    }
}

