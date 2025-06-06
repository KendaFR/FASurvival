package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.commands.FACommand;
import fr.kenda.fasurvie.event.CancelCraft;
import fr.kenda.fasurvie.event.JoinEvent;
import fr.kenda.fasurvie.event.QuitEvent;
import fr.kenda.fasurvie.event.TrackerEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class Managers {
    private final Map<Class<? extends IManager>, IManager> managers = new HashMap<>();

    public Managers() {
        registerManagers();
    }

    public void unregisterManagers() {
        managers.forEach((aClass, iManager) -> iManager.unregister());
    }

    public void reloadManagers() {
        managers.forEach((aClass, iManager) ->
                iManager.unregister());
        managers.clear();
        registerManagers();
    }

    public <T extends IManager> void registerManager(T managerInstance) {
        // On ajoute et on register si pas déjà présent
        if (!managers.containsKey(managerInstance.getClass())) {
            managers.put(managerInstance.getClass(), managerInstance);
            managerInstance.register();
        }
    }

    public <T extends IManager> T getManager(Class<T> managerClass) {
        // On cast de façon sécurisée
        IManager instance = managers.get(managerClass);
        if (instance == null) {
            throw new IllegalStateException("Manager not registered: " + managerClass.getSimpleName());
        }
        return managerClass.cast(instance);
    }

    private void registerManagers() {
        registerManager(new EventManager());
        registerManager(new MapManager());
        registerManager(new FileManager());
        registerManager(new CommandManager());
        registerManager(new TrackerManager());
        registerManager(new CraftManager());
        registerManager(new DatabaseManager(FASurvival.getInstance(), "player_data"));
        registerManager(new DataManager());
        registerManager(new ScoreboardManager());
    }
}


class CommandManager implements IManager {
    @Override
    public void register() {
        FASurvival instance = FASurvival.getInstance();

        instance.getCommand("FAsurvival").setExecutor(new FACommand());
        instance.getCommand("fas").setTabCompleter(new FACommand());
    }

    @Override
    public void unregister() {

    }
}

class EventManager implements IManager {
    @Override
    public void register() {
        PluginManager pm = Bukkit.getPluginManager();
        FASurvival instance = FASurvival.getInstance();

        pm.registerEvents(new TrackerEvent(), instance);
        pm.registerEvents(new CancelCraft(), instance);
        pm.registerEvents(new JoinEvent(), instance);
        pm.registerEvents(new QuitEvent(), instance);
    }

    @Override
    public void unregister() {

    }
}
