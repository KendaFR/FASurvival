package fr.kenda.fasurvie.commands;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public interface SubCommand {

    boolean execute(CommandSender sender, String[] args);

    String getName();

    void sendHelp(CommandSender sender);

    default List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}

