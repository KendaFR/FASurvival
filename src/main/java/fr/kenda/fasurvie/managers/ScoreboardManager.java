package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.data.PlayerDatabase;
import fr.kenda.fasurvie.scheduler.ScoreboardRefreshScheduler;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.Logger;
import fr.kenda.fasurvie.util.TimeUnit;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager implements IManager {

    private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();
    private static final ChatColor[] RANK_COLORS = {
            ChatColor.GOLD, ChatColor.YELLOW, ChatColor.RED,
            ChatColor.LIGHT_PURPLE, ChatColor.AQUA
    };
    private static final String SEP = ChatColor.DARK_GRAY + "================";

    @Override
    public void register() {
        int refreshTime = Config.getInt("refresh_time");
        if (refreshTime <= 0) return;

        long delayUntilNext = TimeUnit.millisUntilNext(refreshTime * 1000L);
        ScoreboardRefreshScheduler scheduler = new ScoreboardRefreshScheduler(delayUntilNext, refreshTime, this);
        Bukkit.getScheduler().runTaskTimer(FASurvival.getInstance(), scheduler, 0L, 20L);
    }

    @Override
    public void unregister() {
        boards.values().forEach(FastBoard::delete);
        boards.clear();
    }

    public void addScoreboard(Player p) {
        boards.computeIfAbsent(p.getUniqueId(), uuid -> {
            FastBoard board = new FastBoard(p);
            board.updateTitle("  " + ChatColor.WHITE + "[" + ChatColor.AQUA + "FreshAgency" + ChatColor.WHITE + "]  ");
            return board;
        });
    }

    public void removeScoreboard(Player p) {
        FastBoard board = boards.remove(p.getUniqueId());
        if (board != null)
            board.delete();
    }

    public void updateAllBoards(List<String> lines) {
        boards.values().forEach(board -> board.updateLines(lines));
    }

    public void updateLineForAll(int index, String line) {
        boards.values().forEach(board -> board.updateLine(index, line));
    }

    public void updateLinesForPlayer(Player p, List<String> lines) {
        FastBoard board = boards.get(p.getUniqueId());
        if (board != null)
            board.updateLines(lines);
    }

    public List<String> getBorderedLeaderboard(List<PlayerDatabase> leaderBoard) {
        List<String> lines = new ArrayList<>(12);
        lines.add("");
        lines.add(SEP);
        lines.add(ChatColor.GOLD + "" + ChatColor.BOLD + "  LEADERBOARD");
        lines.add(SEP);
        lines.add("");

        for (int i = 0; i < 5; i++) {
            if (i < leaderBoard.size()) {
                String name = leaderBoard.get(i).getPlayerName();
                if (name.length() > 9) name = name.substring(0, 9) + ".";
                lines.add(RANK_COLORS[i] + "" + (i + 1) + ". " + name
                        + ChatColor.ITALIC + ChatColor.GRAY + " (" + formatNumber(leaderBoard.get(i).getCoins()) + ")");
            } else {
                lines.add(ChatColor.GRAY + "" + (i + 1) + ". ---");
            }
        }
        lines.add("");
        int refreshTime = Config.getInt("refresh_time");
        if (refreshTime > 0) {
            lines.add(ChatColor.DARK_GRAY + "Rafraichissement dans:");
            long nextRefresh = TimeUnit.millisUntilNext(refreshTime * 1000L);
            lines.add(ChatColor.GOLD + TimeUnit.format(nextRefresh));
        }
        lines.add("");
        lines.add(SEP);
        lines.add("");
        return lines;
    }

    public void refreshLeaderboard() {
        List<PlayerDatabase> leaderBoard = FASurvival.getInstance().getManager()
                .getManager(DatabaseManager.class).getLeaderboard(5);

        Logger.info("Refresh");

        for (int i = 0; i < 5; i++) {
            String newLine;
            if (i < leaderBoard.size()) {
                String name = leaderBoard.get(i).getPlayerName();
                if (name.length() > 9) name = name.substring(0, 9) + ".";
                newLine = RANK_COLORS[i] + "" + (i + 1) + ". " + name
                        + ChatColor.ITALIC + ChatColor.GRAY + " (" + formatNumber(leaderBoard.get(i).getCoins()) + ")";
            } else {
                newLine = ChatColor.GRAY + "" + (i + 1) + ". ---";
            }
            updateLineForAll(i + 5, newLine);
        }
    }
    public static String formatNumber(int number) {
        if (number >= 1_000_000_000) {
            return String.format("%.2fG", number / 1_000_000_000.0).replace(",", ".");
        } else if (number >= 1_000_000) {
            return String.format("%.2fM", number / 1_000_000.0).replace(",", ".");
        } else if (number >= 1_000) {
            return String.format("%.2fk", number / 1_000.0).replace(",", ".");
        }
        return String.valueOf(number);
    }

}