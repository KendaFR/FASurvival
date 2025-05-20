package fr.kenda.fasurvie.service.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.commands.FAReloadCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class Managers {
    private final Map<Class<? extends IManager>, IManager> managers = new HashMap<>();

    public Managers() {
       registerManagers();
    }

    private void registerManagers()
    {
        registerManager(new CommandManager());
        registerManager(new EventManager());
        registerManager(new MapManager());
    }
    public void unregisterManagers()
    {
        managers.forEach((aClass, iManager) -> iManager.unregister());
    }
    public void reloadManagers()
    {
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
}


interface IManager {
    void register();
    void unregister();
}

class CommandManager implements IManager {
    @Override
    public void register() {
        FASurvival instance = FASurvival.getInstance();

        instance.getCommand("FAsurvival").setExecutor(new FAReloadCommand());
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
    }

    @Override
    public void unregister() {

    }
}
