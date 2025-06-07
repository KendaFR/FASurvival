package fr.kenda.fasurvie.event;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.data.PlayerData;
import fr.kenda.fasurvie.data.PlayerDatabase;
import fr.kenda.fasurvie.managers.DatabaseManager;
import fr.kenda.fasurvie.managers.ScoreboardManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
public class JoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerData pd = new PlayerData(p);
        pd.loadData();

        e.setJoinMessage(ChatColor.WHITE + "[" + ChatColor.AQUA + "+" + ChatColor.WHITE + "]" + ChatColor.YELLOW + p.getName() + " a rejoint le serveur");

        ScoreboardManager scoreboardManager = FASurvival.getInstance().getManager().getManager(ScoreboardManager.class);
        scoreboardManager.addScoreboard(p);

        List<PlayerDatabase> leaderBoard = FASurvival.getInstance().getManager().getManager(DatabaseManager.class).getLeaderboard(5);
        List<String> lines = scoreboardManager.getBorderedLeaderboard(leaderBoard);

        scoreboardManager.updateLinesForPlayer(p, lines.toArray(new String[0]));
    }
}

