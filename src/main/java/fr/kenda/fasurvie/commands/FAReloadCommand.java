package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
public class FAReloadCommand implements CommandExecutor {

    private static final String PERMISSION = "fasurvival";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de faire cette commande");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("map")) {
            return handleMapCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("reload")) {
            // Si besoin, tu peux appeler ici la logique de rechargement config
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Configuration rechargée !");
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private boolean handleMapCommand(CommandSender sender, String[] args) {
        // /fas map <size>
        if (args.length == 2 && isDigital(args[1])) {
            int size = Integer.parseInt(args[1]);
            World world = getWorldForSender(sender);

            setWorldBorder(world, 0, 0, size);
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Border centrée en (0, 0) avec taille " + size);
            return true;
        }

        // /fas map <centerX> <centerZ> <size>
        if (args.length == 4 && isDigital(args[1]) && isDigital(args[2]) && isDigital(args[3])) {
            int centerX = Integer.parseInt(args[1]);
            int centerZ = Integer.parseInt(args[2]);
            int size = Integer.parseInt(args[3]);
            World world = getWorldForSender(sender);

            setWorldBorder(world, centerX, centerZ, size);
            sender.sendMessage(
                    FASurvival.PREFIX + ChatColor.GREEN +
                            "Border centrée en (" + centerX + ", " + centerZ + ") avec taille " + size
            );
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void setWorldBorder(World world, int centerX, int centerZ, int size) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(size);
    }

    private World getWorldForSender(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getWorld();
        } else {
            return Bukkit.getWorlds().get(0); // Par défaut, premier monde
        }
    }

    private boolean isDigital(String arg) {
        try {
            Integer.parseInt(arg);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sendHelp(CommandSender sender) {
        List<String> helps = Arrays.asList(
                FASurvival.PREFIX + ChatColor.GRAY + "/fas reload: "
                        + ChatColor.RED + "Recharger la configuration",
                FASurvival.PREFIX + ChatColor.GRAY + "/fas map <size>: "
                        + ChatColor.RED + "Définir la taille de la carte",
                FASurvival.PREFIX + ChatColor.GRAY + "/fas map <centerX> <centerZ> <size>: "
                        + ChatColor.RED + "Définir la taille de la carte autour d'un point donné"
        );
        helps.forEach(sender::sendMessage);
    }
}

