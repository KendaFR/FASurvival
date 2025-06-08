package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FACommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "fasurvival";

    // Collection des sous-commandes
    private final List<SubCommand> subCommands = Arrays.asList(
            new MapCommand(),
            new ReloadCommand(),
            new KitCommand(),
            new EffectCommand(),
            new XPCommand(),
            new CompassTrackerCommand(),
            new SpawnMob(),
            new GiveFreshCoins(),
            new SpawnItem()
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp() &&  !sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de faire cette commande.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        for (SubCommand subCmd : subCommands) {
            if (args[0].equalsIgnoreCase(subCmd.getName())) {
                String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCmd.execute(sender, newArgs);
            }
        }
        sendHelp(sender);
        return true;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas reload: " + ChatColor.RED + "Recharger la configuration");
        subCommands.forEach(subCommand -> subCommand.sendHelp(sender));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) return Collections.emptyList();

        if (args.length == 1) {
            // Première tab: nom de sous-commande
            return subCommands.stream()
                    .map(SubCommand::getName)
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        // Sous-commande concernée
        for (SubCommand subCmd : subCommands) {
            if (args[0].equalsIgnoreCase(subCmd.getName())) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCmd.onTabComplete(sender, subArgs);
            }
        }
        return Collections.emptyList();
    }
}