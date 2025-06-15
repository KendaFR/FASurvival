package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.Constant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GiveFreshCoins
        implements SubCommand {
    private static final int MAX_STACK_SIZE = 64;

    @Override
    public boolean execute(CommandSender sender, String[] args2, boolean isForced) {
        int amount;
        if (args2.length != 2) {
            this.sendHelp(sender);
            return true;
        }
        Player target = Bukkit.getPlayer(args2[0]);
        if (target == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        try {
            amount = Integer.parseInt(args2[1]);
            if (amount <= 0) {
                sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "La quantité doit être positive.");
                return false;
            }
            if (amount >= 10000) {
                sender.sendMessage(FASurvival.PREFIX + ChatColor.DARK_RED + "Attention, la quantité est trop élevée pour le serveur !.");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Merci de mettre un nombre valide.");
            return false;
        }
        this.giveFreshCoins(target, amount);
        if (Config.getBoolean("fresh_coin.send_message_to_player_receive")) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + String.format("Vous avez donné %d freshCoins à %s.", amount, target.getName()));
            target.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + String.format("Vous avez reçu %d freshCoins.", amount));
        }
        return true;
    }

    @Override
    public String getName() {
        return "freshcoins";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " <player> <amount>: " + ChatColor.RED + "Donne des freshCoins au joueur");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args2) {
        if (args2.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName).filter(name -> name.toLowerCase().startsWith(args2[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void giveFreshCoins(Player player, int amount) {
        PlayerInventory inventory = player.getInventory();
        while (amount > 0) {
            ItemStack coinStack = Constant.FRESH_COIN_ITEM.clone();
            int stackAmount = Math.min(amount, 64);
            coinStack.setAmount(stackAmount);
            HashMap<Integer, ItemStack> leftover = inventory.addItem(coinStack);
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
                player.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "Votre inventaire est plein ! Certains items ont été déposés au sol.");
            }
            amount -= stackAmount;
        }
    }
}

