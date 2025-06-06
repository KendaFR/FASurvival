package fr.kenda.fasurvie.managers;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;

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
}