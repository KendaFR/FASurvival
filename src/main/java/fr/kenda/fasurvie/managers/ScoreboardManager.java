package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.data.PlayerDatabase;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.TimeUnit;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScoreboardManager implements IManager {

    private final HashMap<Player, FastBoard> boards = new HashMap<>();

    @Override
    public void register() {

    }

    @Override
    public void unregister() {
        boards.forEach((player, fastBoard) -> fastBoard.delete());
        boards.clear();
    }

    public void addScoreboard(Player p)
    {
        FastBoard board = new FastBoard(p);
        board.updateTitle("  " + ChatColor.WHITE + "[" + ChatColor.AQUA + "FreshAgency" + ChatColor.WHITE + "]  ");
        this.boards.put(p, board);
    }
    public void removeScoreboard(Player p)
    {
        FastBoard board = boards.get(p);
        board.delete();
        this.boards.remove(p);
    }
    public void updateLinesForAll(String lines)
    {
        boards.values().forEach(fastBoard -> fastBoard.updateLines(lines));
    }
    public void updateLineForAll(int index, String line)
    {
        boards.values().forEach(fastBoard -> fastBoard.updateLine(index, line));
    }
    public void updateLineForPlayer(Player p, int index, String line)
    {
        boards.get(p).updateLine(index, line);
    }
    public void updateLinesForPlayer(Player p, String... lines)
    {
        boards.get(p).updateLines(lines);
    }



    public List<String> getBorderedLeaderboard(List<PlayerDatabase> leaderBoard) {
        List<String> lines = new ArrayList<>();

        lines.add("");
        lines.add(ChatColor.DARK_GRAY + "================");
        lines.add(ChatColor.GOLD + "" + ChatColor.BOLD + "  LEADERBOARD");
        lines.add(ChatColor.DARK_GRAY + "================");
        lines.add("");

        ChatColor[] rankColors = {ChatColor.GOLD, ChatColor.YELLOW, ChatColor.RED,
                ChatColor.LIGHT_PURPLE, ChatColor.AQUA};

        for (int i = 0; i < 5; i++) {
            if (i < leaderBoard.size()) {
                String name = leaderBoard.get(i).getPlayerName();
                if (name.length() > 9) {
                    name = name.substring(0, 9) + ".";
                }

                lines.add(rankColors[i] + "" + (i + 1) + ". " + name + ChatColor.ITALIC + ChatColor.GRAY +  " (" +
                        leaderBoard.get(i).getCoins() + ")");
            } else {
                lines.add(ChatColor.GRAY + String.valueOf(i + 1) + ". ---");
            }
        }

        lines.add("");
        if(Config.getInt("refresh_time") > 0) {
            lines.add(ChatColor.DARK_GRAY + "Rafraichissement dans:");
        }
        lines.add("");
        lines.add(ChatColor.DARK_GRAY + "================");
        lines.add("");

        return lines;
    }
}