package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FACommand
        implements CommandExecutor,
        TabCompleter {
    private static final String PERMISSION = "fasurvival";
    private final List<SubCommand> subCommands = Arrays.asList(
            new MapCommand(),
            new ReloadCommand(),
            new KitCommand(),
            new EffectCommand(),
            new XPCommand(),
            new CompassTrackerCommand(),
            new SpawnMob(),
            new GiveFreshCoins(),
            new SpawnItem(),
            new CleanDB());

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args2) {
        if (!sender.isOp() && !sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de faire cette commande.");
            return true;
        }
        if (args2.length == 0) {
            this.sendHelp(sender);
            return true;
        }

        for (SubCommand subCmd : this.subCommands) {
            if (!args2[0].equalsIgnoreCase(subCmd.getName())) continue;
            String[] newArgs = Arrays.copyOfRange(args2, 1, args2.length);

            return subCmd.execute(sender, newArgs, label.equalsIgnoreCase("fasf"));
        }
        this.sendHelp(sender);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args2) {
        if (!sender.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }
        if (args2.length == 1) {
            return this.subCommands.stream().map(SubCommand::getName).filter(name -> name.startsWith(args2[0].toLowerCase())).collect(Collectors.toList());
        }
        for (SubCommand subCmd : this.subCommands) {
            if (!args2[0].equalsIgnoreCase(subCmd.getName())) continue;
            String[] subArgs = Arrays.copyOfRange(args2, 1, args2.length);
            return subCmd.onTabComplete(sender, subArgs);
        }
        return Collections.emptyList();
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas reload: " + ChatColor.RED + "Recharger la configuration");
        this.subCommands.forEach(subCommand -> subCommand.sendHelp(sender));
    }
}

