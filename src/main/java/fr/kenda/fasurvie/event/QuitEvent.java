package fr.kenda.fasurvie.event;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.data.PlayerData;
import fr.kenda.fasurvie.managers.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitEvent implements Listener {


    @EventHandler
    public void onJoin(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();
        PlayerData pd = FASurvival.getInstance().getManager().getManager(DataManager.class).getPlayerDataFromPlayer(p);
        pd.saveData(true);

        e.setQuitMessage(ChatColor.WHITE + "[" + ChatColor.RED + "-" + ChatColor.WHITE + "]" + ChatColor.YELLOW + p.getName() + " a quitter le serveur");
    }
}
