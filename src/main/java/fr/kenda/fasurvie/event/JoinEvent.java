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

import java.util.ArrayList;
import java.util.Arrays;
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
        List<String> lines = getBorderedLeaderboard(leaderBoard);

        scoreboardManager.updateLinesForPlayer(p, lines.toArray(new String[0]));
    }

    private static List<String> getLeaderboard(List<String> leaderBoard) {
        // Gradient simple pour 1.8.8
        List<ChatColor> gradient = Arrays.asList(
                ChatColor.GOLD,          // 1er
                ChatColor.YELLOW,        // 2ème
                ChatColor.RED,           // 3ème
                ChatColor.LIGHT_PURPLE,  // 4ème
                ChatColor.AQUA           // 5ème
        );

        List<String> lines = new ArrayList<>();

        // Header compact (max 32 caractères par ligne)
        lines.add("");
        lines.add(ChatColor.GOLD + "" + ChatColor.BOLD + "   TOP JOUEURS");
        lines.add(ChatColor.GRAY + "---------------");
        lines.add("");

        // Affichage optimisé pour 1.8.8
        for (int i = 0; i < 5; i++) {
            if (i < leaderBoard.size()) {
                String name = leaderBoard.get(i);
                ChatColor color = gradient.get(i);

                // Tronquer le nom si trop long (max ~20 caractères total)
                if (name.length() > 12) {
                    name = name.substring(0, 12) + "..";
                }

                if (i == 0) {
                    // Style spécial 1er place
                    lines.add(ChatColor.GOLD + "" + ChatColor.BOLD + "#" + (i + 1) +
                            ChatColor.WHITE + " " + ChatColor.GOLD + name);
                } else {
                    lines.add(color + "#" + (i + 1) + ChatColor.WHITE + " " + color + name);
                }
            } else {
                // Position vide
                lines.add(ChatColor.GRAY + "#" + (i + 1) + " Libre");
            }
        }

        lines.add("");
        lines.add(ChatColor.GREEN + "Felicitations !");
        lines.add("");

        return lines;
    }

    // Version encore plus compacte
    private static List<String> getCompactLeaderboard(List<String> leaderBoard) {
        List<String> lines = new ArrayList<>();

        lines.add("");
        lines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "CLASSEMENT");
        lines.add("");

        // Symboles simples compatibles 1.8.8
        String[] symbols = {"*", "o", "+", "-", "."};
        ChatColor[] colors = {ChatColor.GOLD, ChatColor.YELLOW, ChatColor.RED,
                ChatColor.LIGHT_PURPLE, ChatColor.AQUA};

        for (int i = 0; i < Math.min(5, leaderBoard.size()); i++) {
            String name = leaderBoard.get(i);
            if (name.length() > 14) {
                name = name.substring(0, 14);
            }

            lines.add(colors[i] + symbols[i] + " " + name);
        }

        lines.add("");
        return lines;
    }

    // Version avec bordures simples
    private static List<String> getBorderedLeaderboard(List<PlayerDatabase> leaderBoard) {
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
                // Limite stricte pour 1.8.8
                if (name.length() > 10) {
                    name = name.substring(0, 10);
                }

                lines.add(rankColors[i] + "" + (i + 1) + ". " + name + ChatColor.ITALIC + ChatColor.GRAY +  " (" + leaderBoard.get(i).getCoins() + ")");
            } else {
                lines.add(ChatColor.GRAY + String.valueOf(i + 1) + ". ---");
            }
        }

        lines.add("");
        lines.add(ChatColor.DARK_GRAY + "================");
        lines.add("");

        return lines;
    }

    private static List<String> getMinimalLeaderboard(List<String> leaderBoard) {
        List<String> lines = new ArrayList<>();

        lines.add("");
        lines.add(ChatColor.BOLD + "TOP 5");
        lines.add("");

        for (int i = 0; i < Math.min(5, leaderBoard.size()); i++) {
            String name = leaderBoard.get(i);
            if (name.length() > 12) {
                name = name.substring(0, 12);
            }

            ChatColor color = i == 0 ? ChatColor.GOLD :
                    i == 1 ? ChatColor.YELLOW :
                            i == 2 ? ChatColor.RED : ChatColor.WHITE;

            lines.add(color + String.valueOf(i + 1) + ". " + name);
        }

        lines.add("");
        return lines;
    }
}

