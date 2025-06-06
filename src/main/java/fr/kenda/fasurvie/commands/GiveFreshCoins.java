package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.Constant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GiveFreshCoins implements SubCommand {

    private static final int MAX_STACK_SIZE = 64; // Ajustez selon votre configuration

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sendHelp(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Joueur introuvable.");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "La quantité doit être positive.");
                return false;
            }
            if(amount >= 10000)
            {
                sender.sendMessage(FASurvival.PREFIX + ChatColor.DARK_RED + "Attention, la quantité est trop élevée pour le serveur !.");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Merci de mettre un nombre valide.");
            return false;
        }

        giveFreshCoins(target, amount);

        if(Config.getBoolean("fresh_coin.send_message_to_player_receive")) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN +
                    String.format("Vous avez donné %d freshCoins à %s.", amount, target.getName()));
            target.sendMessage(FASurvival.PREFIX + ChatColor.GREEN +
                    String.format("Vous avez reçu %d freshCoins.", amount));
        }
        return true;
    }

    private void giveFreshCoins(Player player, int amount) {
        PlayerInventory inventory = player.getInventory();

        while (amount > 0) {
            ItemStack coinStack = Constant.FRESH_COIN_ITEM.clone();
            int stackAmount = Math.min(amount, MAX_STACK_SIZE);
            coinStack.setAmount(stackAmount);

            // Ajouter l'item et gérer les items qui ne rentrent pas
            HashMap<Integer, ItemStack> leftover = inventory.addItem(coinStack);

            if (!leftover.isEmpty()) {
                // Si l'inventaire est plein, drop les items au sol
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
                player.sendMessage(FASurvival.PREFIX + ChatColor.GRAY +
                        "Votre inventaire est plein ! Certains items ont été déposés au sol.");
            }

            amount -= stackAmount;
        }
    }

    @Override
    public String getName() {
        return "freshcoins";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() +
                " <player> <amount>: " + ChatColor.RED + "Donne des freshCoins au joueur");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Auto-complétion des noms de joueurs en ligne
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}