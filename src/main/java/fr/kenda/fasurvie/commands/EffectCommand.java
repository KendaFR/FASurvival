package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectCommand implements SubCommand {

    private static final List<String> ALLOWED_EFFECT = Arrays.asList("speed", "test");

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3 || args.length > 4) {
            sendHelp(sender);
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Joueur introuvable.");
            return true;
        }

        PotionEffectType effectType = PotionEffectType.getByName(args[1].toUpperCase());
        if (effectType == null) {
            sender.sendMessage(FASurvival.PREFIX +ChatColor.RED + "Effet introuvable.");
            return true;
        }

        int duration;
        try {
            duration = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Durée invalide.");
            return true;
        }

        int amplifier = 1;
        if (args.length == 4) {
            try {
                amplifier = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {}
        }

        if(!ALLOWED_EFFECT.contains(effectType.getName())) {
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
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() + " <player> <effect> <duration> [power]: " +  ChatColor.RED + "Donne des effets au joueur");
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            List<String> effectNames = new ArrayList<>();
            Arrays.stream(PotionEffectType.values())
                    .filter(type -> type != null && type.getName() != null)
                    .forEach(type -> effectNames.add(type.getName().toLowerCase()));
            effectNames.removeIf(s -> !s.startsWith(prefix));
            return effectNames;
        }
        return List.of();
    }
}
