package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnItem
        implements SubCommand {
    private final List<String> allowedItems = Config.getList("allowed_items");

    @Override
    public boolean execute(CommandSender sender, String[] args2, boolean isForced) {
        if (args2.length != 3) {
            this.sendHelp(sender);
            return true;
        }
        Player target = Bukkit.getPlayer(args2[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        try {
            Material material = Material.valueOf(args2[1].toUpperCase());
            int number = Integer.parseInt(args2[2]);
            if (!isForced && !this.allowedItems.contains(args2[1].toLowerCase())) {
                sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas give cet item au joueur");
                return false;
            }
            target.getInventory().addItem(new ItemBuilder(material, number).build());
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Item donné à " + target.getName());
            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getName() {
        return "item";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " <player> <item> <number>: " + ChatColor.RED + "Donne un item au joueur");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args2) {
        if (args2.length == 2) {
            String prefix = args2[1].toLowerCase();
            ArrayList<String> itemNames = new ArrayList<String>();
            Arrays.stream(Material.values()).forEach(type -> itemNames.add(type.name().toLowerCase()));
            itemNames.removeIf(s -> !s.startsWith(prefix));
            return itemNames;
        }
        return List.of();
    }
}

