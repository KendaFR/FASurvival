package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SpawnMob implements SubCommand {
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
            EntityType mobType = EntityType.valueOf(args[1].toUpperCase());

            target.getWorld().spawnEntity(target.getLocation().clone().add(new Vector(0, 1, 0)), mobType);
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Mob apparu sur " + target.getName());
            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getName() {
        return "mob";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() + " <player> <mob>: " + ChatColor.RED + "Fait spawn un mob sur le joueur");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            List<String> mobNames = new ArrayList<>();
            Arrays.stream(EntityType.values())
                    .forEach(type -> mobNames.add(type.name().toLowerCase()));
            mobNames.removeIf(s -> !s.startsWith(prefix));
            return mobNames;
        }
        return List.of();
    }
}