package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Config;

import java.util.HashMap;
import java.util.Map;

public class Managers {
    private final Map<Class<? extends IManager>, IManager> managers = new HashMap<Class<? extends IManager>, IManager>();

    public Managers() {
        this.registerManagers();
    }

    public void unregisterManagers() {
        this.managers.forEach((aClass, iManager) -> iManager.unregister());
    }

    public void reloadManagers() {
        this.managers.forEach((aClass, iManager) -> iManager.unregister());
        this.managers.clear();
        this.registerManagers();
    }

    public <T extends IManager> void registerManager(T managerInstance) {
        if (!this.managers.containsKey(managerInstance.getClass())) {
            this.managers.put(managerInstance.getClass(), managerInstance);
            managerInstance.register();
        }
    }

    public <T extends IManager> T getManager(Class<T> managerClass) {
        IManager instance = this.managers.get(managerClass);
        if (instance == null) {
            throw new IllegalStateException("Manager not registered: " + managerClass.getSimpleName());
        }
        return managerClass.cast(instance);
    }

    private void registerManagers() {
        FASurvival instance = FASurvival.getInstance();
        DatabaseManager db = new DatabaseManager(instance, "player_data");
        this.registerManager(new EventManager());
        this.registerManager(new MapManager());
        this.registerManager(new FileManager());
        this.registerManager(new CommandManager());
        this.registerManager(new TrackerManager());
        this.registerManager(new CraftManager());
        this.registerManager(db);
        this.registerManager(new DataManager());
        this.registerManager(new ScoreboardManager());
        this.registerManager(new PNJManager(instance));
        this.registerManager(new BackupDataManager(instance, db, "player_data"));
        this.registerManager(new BotManager(instance.getLogger(), Config.getString("bot.token"), Config.getString("bot.channel_id")));
    }
}

