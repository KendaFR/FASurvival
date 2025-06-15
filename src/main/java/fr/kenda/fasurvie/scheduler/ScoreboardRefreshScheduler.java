package fr.kenda.fasurvie.scheduler;

import fr.kenda.fasurvie.managers.ScoreboardManager;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.TimeUnit;
import org.bukkit.ChatColor;

public class ScoreboardRefreshScheduler
        implements Runnable {
    private long timer;
    private final int refreshInterval;
    private final ScoreboardManager scoreboardManager;

    public ScoreboardRefreshScheduler(long delayUntilNext, int refreshInterval, ScoreboardManager scoreboardManager) {
        this.timer = delayUntilNext / 1000L;
        this.refreshInterval = refreshInterval;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public void run() {
        --this.timer;
        if (this.timer <= 0L) {
            this.timer = this.refreshInterval;
            this.scoreboardManager.refreshLeaderboard();
        }
        if (Config.getInt("refresh_time") > 0) {
            String timeLeft = TimeUnit.format(this.timer * 1000L);
            this.scoreboardManager.updateLineForAll(12, ChatColor.GOLD + timeLeft);
        }
    }
}

