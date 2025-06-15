package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectCommand
        implements SubCommand {
    private final List<String> allowedEffects = Config.getList("allowed_effects");

    @Override
    public boolean execute(CommandSender sender, String[] args2, boolean isForced) {
        int duration;
        if (args2.length < 3 || args2.length > 4) {
            this.sendHelp(sender);
            return true;
        }
        Player target = Bukkit.getPlayer(args2[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        PotionEffectType effectType = PotionEffectType.getByName(args2[1].toUpperCase());
        if (effectType == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Effet introuvable.");
            return true;
        }
        try {
            duration = Integer.parseInt(args2[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Durée invalide.");
            return true;
        }
        int amplifier = 1;
        if (args2.length == 4) {
            try {
                amplifier = Integer.parseInt(args2[3]);
            } catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (!isForced && !this.allowedEffects.contains(args2[1].toLowerCase())) {
            sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas appliquer cet effect au joueur");
            return false;
        }
        target.addPotionEffect(new PotionEffect(effectType, duration * 20, amplifier - 1));
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Effet appliqué à " + target.getName());
        return true;
    }

    @Override
    public String getName() {
        return "effect";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " <player> <effect> <duration> [power]: " + ChatColor.RED + "Donne des effets au joueur");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args2) {
        if (args2.length == 2) {
            String prefix = args2[1].toLowerCase();
            ArrayList<String> effectNames = new ArrayList<String>();
            Arrays.stream(PotionEffectType.values()).filter(type -> type != null && type.getName() != null).forEach(type -> effectNames.add(type.getName().toLowerCase()));
            effectNames.removeIf(s -> !s.startsWith(prefix));
            return effectNames;
        }
        return List.of();
    }
}

