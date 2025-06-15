package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.data.PlayerDatabase;
import fr.kenda.fasurvie.scheduler.ScoreboardRefreshScheduler;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.Logger;
import fr.kenda.fasurvie.util.TimeUnit;
import fr.mrmicky.fastboard.FastBoard;
import fr.mrmicky.fastboard.FastBoardBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager
        implements IManager {
    private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();
    private static final ChatColor[] RANK_COLORS = new ChatColor[]{ChatColor.YELLOW, ChatColor.WHITE, ChatColor.WHITE, ChatColor.WHITE, ChatColor.WHITE};

    @Override
    public void register() {
        int refreshTime = Config.getInt("refresh_time");
        if (refreshTime <= 0) {
            return;
        }
        long delayUntilNext = TimeUnit.millisUntilNext((long) refreshTime * 1000L);
        ScoreboardRefreshScheduler scheduler = new ScoreboardRefreshScheduler(delayUntilNext, refreshTime, this);
        Bukkit.getScheduler().runTaskTimer(FASurvival.getInstance(), scheduler, 0L, 20L);
    }

    @Override
    public void unregister() {
        this.boards.values().forEach(FastBoardBase::delete);
        this.boards.clear();
    }

    public void addScoreboard(Player p) {
        this.boards.computeIfAbsent(p.getUniqueId(), uuid -> {
            FastBoard board = new FastBoard(p);
            board.updateTitle(ChatColor.AQUA + "FreshAgency");
            return board;
        });
    }

    public void removeScoreboard(Player p) {
        FastBoard board = this.boards.remove(p.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    public void updateAllBoards(List<String> lines) {
        this.boards.values().forEach(board -> board.updateLines(lines));
    }

    public void updateLineForAll(int index, String line) {
        this.boards.values().forEach(board -> board.updateLine(index, line));
    }

    public void updateLinesForPlayer(Player p, List<String> lines) {
        FastBoard board = this.boards.get(p.getUniqueId());
        if (board != null) {
            board.updateLines(lines);
        }
    }

    public List<String> getBorderedLeaderboard(List<PlayerDatabase> leaderBoard) {
        ArrayList<String> lines = new ArrayList<String>(10);
        lines.add("");
        lines.add(ChatColor.GOLD + "Leaderboard");
        lines.add("");
        for (int i = 0; i < 5; ++i) {
            if (i < leaderBoard.size()) {
                String name = leaderBoard.get(i).getPlayerName();
                if (name.length() > 10) {
                    name = name.substring(0, 10) + "..";
                }
                lines.add(RANK_COLORS[i] + "" + (i + 1) + ". " + name + " " + ChatColor.GRAY + "(" + ScoreboardManager.formatNumber(leaderBoard.get(i).getCoins()) + ")");
                continue;
            }
            lines.add(ChatColor.GRAY + "" + (i + 1) + ". ---");
        }
        int refreshTime = Config.getInt("refresh_time");
        if (refreshTime > 0) {
            lines.add("");
            long nextRefresh = TimeUnit.millisUntilNext((long) refreshTime * 1000L);
            lines.add(ChatColor.GRAY + "Refresh: " + ChatColor.WHITE + TimeUnit.format(nextRefresh));
            lines.add("");
        }
        return lines;
    }

    public void refreshLeaderboard() {
        List<PlayerDatabase> leaderBoard = FASurvival.getInstance().getManager().getManager(DatabaseManager.class).getLeaderboard(5);
        Logger.info("Refresh");
        for (int i = 0; i < 5; ++i) {
            String newLine;
            if (i < leaderBoard.size()) {
                String name = leaderBoard.get(i).getPlayerName();
                if (name.length() > 10) {
                    name = name.substring(0, 10) + "..";
                }
                newLine = RANK_COLORS[i] + "" + (i + 1) + ". " + name + " " + ChatColor.GRAY + "(" + ScoreboardManager.formatNumber(leaderBoard.get(i).getCoins()) + ")";
            } else {
                newLine = ChatColor.GRAY + "" + (i + 1) + ". ---";
            }
            this.updateLineForAll(i + 3, newLine);
        }
    }

    public static String formatNumber(int number) {
        if (number >= 1000000000) {
            return String.format("%.1fG", (double) number / 1.0E9).replace(",", ".");
        }
        if (number >= 1000000) {
            return String.format("%.1fM", (double) number / 1000000.0).replace(",", ".");
        }
        if (number >= 1000) {
            return String.format("%.1fk", (double) number / 1000.0).replace(",", ".");
        }
        return String.valueOf(number);
    }
}