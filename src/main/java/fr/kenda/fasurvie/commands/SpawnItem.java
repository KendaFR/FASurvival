package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnItem implements SubCommand {

    private static final List<String> ALLOWED_ITEMS = Arrays.asList("speed", "test");

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sendHelp(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        try {
            Material material = Material.valueOf(args[1].toUpperCase());
            int number = Integer.parseInt(args[2]);

            if(!ALLOWED_ITEMS.contains(args[1].toLowerCase()))
            {
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
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() + " <player> <item> <number>: " + ChatColor.RED + "Donne un item au joueur");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            List<String> itemNames = new ArrayList<>();
            Arrays.stream(Material.values())
                    .forEach(type -> itemNames.add(type.name().toLowerCase()));
            itemNames.removeIf(s -> !s.startsWith(prefix));
            return itemNames;
        }
        return List.of();
    }
}