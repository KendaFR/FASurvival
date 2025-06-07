package fr.kenda.fasurvie.scheduler;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;

public class ScoreboardRefreshScheduler extends BukkitRunnable {

    private int initialTimer;
    private int timer;
    private int delayToNext;

    public ScoreboardRefreshScheduler(int delayToNext, int initialTimer)
    {
        this.timer = delayToNext;
        this.delayToNext = delayToNext;
        this.initialTimer = initialTimer;
    }

    public ScoreboardRefreshScheduler(int initialTimer)
    {
        this(-1, initialTimer);
    }

    @Override
    public void run() {
        if(delayToNext > 0) {
            delayToNext--;
        }
        else
            timer--;
    }
}
