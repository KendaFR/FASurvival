package fr.kenda.fasurvie.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    boolean execute(CommandSender var1, String[] var2, boolean var3);

    String getName();

    void sendHelp(CommandSender var1);

    default List<String> onTabComplete(CommandSender sender, String[] args2) {
        return List.of();
    }
}

