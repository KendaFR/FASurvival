package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnMob
        implements SubCommand {
    private final List<String> allowedMobs = Config.getList("allowed_mobs");

    @Override
    public boolean execute(CommandSender sender, String[] args2, boolean isForced) {
        if (args2.length < 3) {
            this.sendHelp(sender);
            return true;
        }
        Player target = Bukkit.getPlayer(args2[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        try {
            EntityType mobType = EntityType.valueOf(args2[1].toUpperCase());
            if (!isForced && !this.allowedMobs.contains(args2[1].toLowerCase())) {
                sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas faire apparaitre ce mob");
                return false;
            }
            Entity entity = target.getWorld().spawnEntity(target.getLocation().clone().add(new Vector(0, 1, 0)), mobType);
            entity.setCustomNameVisible(true);
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args2.length; ++i) {
                sb.append(args2[i]).append(" ");
            }
            entity.setCustomName(ChatColor.GOLD + sb.toString());
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
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " <player> <mob> <donator>: " + ChatColor.RED + "Fait spawn un mob sur le joueur");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args2) {
        if (args2.length == 2) {
            String prefix = args2[1].toLowerCase();
            ArrayList<String> mobNames = new ArrayList<String>();
            Arrays.stream(EntityType.values()).forEach(type -> mobNames.add(type.name().toLowerCase()));
            mobNames.removeIf(s -> !s.startsWith(prefix));
            return mobNames;
        }
        return List.of();
    }
}

