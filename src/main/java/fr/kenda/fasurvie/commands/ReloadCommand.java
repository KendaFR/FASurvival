package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand
        implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args2, boolean isForced) {
        FASurvival.getInstance().getManager().reloadManagers();
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Configuration rechargée !");
        return true;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void sendHelp(CommandSender sender) {
    }
}

