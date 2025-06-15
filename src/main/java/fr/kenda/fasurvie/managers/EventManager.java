package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.event.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

class EventManager
        implements IManager {
    @Override
    public void register() {
        PluginManager pm = Bukkit.getPluginManager();
        FASurvival instance = FASurvival.getInstance();
        pm.registerEvents(new TrackerEvent(), instance);
        pm.registerEvents(new CancelCraft(), instance);
        pm.registerEvents(new JoinEvent(), instance);
        pm.registerEvents(new QuitEvent(), instance);
        pm.registerEvents(new WeatherLockEvent(), instance);
        pm.registerEvents(new WitherLockRegen(), instance);
    }

    @Override
    public void unregister() {
    }

    EventManager() {
    }
}

