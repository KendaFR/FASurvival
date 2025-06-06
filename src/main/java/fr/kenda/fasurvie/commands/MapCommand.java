package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.managers.MapManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MapCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 1 && isDigital(args[0])) {
            int size = Integer.parseInt(args[0]);
            FASurvival.getInstance().getManager().getManager(MapManager.class).setBorder(sender, 0, 0, size);
            return true;
        }

        if (args.length == 3) {
            if (isDigital(args[0]) && isDigital(args[1]) && isDigital(args[2])) {
                int centerX = Integer.parseInt(args[0]);
                int centerZ = Integer.parseInt(args[1]);
                int size = Integer.parseInt(args[2]);
                FASurvival.getInstance().getManager().getManager(MapManager.class).setBorder(sender, centerX, centerZ, size);
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Merci de mettre des nombres valides.");
                return false;
            }
        }

        sendHelp(sender);
        return true;
    }

    @Override
    public String getName() {
        return "map";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "/fas " + getName() + " <size>: " + ChatColor.RED + "Définir la taille de la carte");
        sender.sendMessage(ChatColor.GRAY + "/fas " + getName() + " <centerX> <centerZ> <size>: " + ChatColor.RED + "Définir la taille de la carte autour d'un point donné");
    }

    private boolean isDigital(String arg) {
        try {
            Integer.parseInt(arg);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

