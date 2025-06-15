package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.commands.FACommand;
import fr.kenda.fasurvie.commands.FAManual;

class CommandManager
        implements IManager {
    @Override
    public void register() {
        FASurvival instance = FASurvival.getInstance();
        instance.getCommand("FAsurvival").setExecutor(new FACommand());
        instance.getCommand("FAManual").setExecutor(new FAManual());
        instance.getCommand("fas").setTabCompleter(new FACommand());
    }

    @Override
    public void unregister() {
    }

    CommandManager() {
    }
}

