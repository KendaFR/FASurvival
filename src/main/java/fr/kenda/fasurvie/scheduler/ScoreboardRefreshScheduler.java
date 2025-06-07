package fr.kenda.fasurvie.scheduler;

import fr.kenda.fasurvie.managers.ScoreboardManager;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.TimeUnit;
import org.bukkit.ChatColor;

public class ScoreboardRefreshScheduler implements Runnable {

    private long timer;
    private int refreshInterval;
    private ScoreboardManager scoreboardManager;

    public ScoreboardRefreshScheduler(long delayUntilNext, int refreshInterval, ScoreboardManager scoreboardManager) {
        this.timer = delayUntilNext / 1000; // Convertir en secondes
        this.refreshInterval = refreshInterval;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public void run() {
        timer--;

        if (timer <= 0) {
            timer = refreshInterval;

            // Rafraîchir le leaderboard ici si nécessaire
            scoreboardManager.refreshLeaderboard();
        }

        // Mettre à jour la ligne du compte à rebours si le refresh est activé
        if (Config.getInt("refresh_time") > 0) {
            String timeLeft = TimeUnit.format(timer * 1000L);
            scoreboardManager.updateLineForAll(12, ChatColor.GOLD + timeLeft);
        }
    }
}
