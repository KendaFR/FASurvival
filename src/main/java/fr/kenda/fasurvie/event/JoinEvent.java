package fr.kenda.fasurvie.event;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.data.PlayerData;
import fr.kenda.fasurvie.data.PlayerDatabase;
import fr.kenda.fasurvie.managers.DatabaseManager;
import fr.kenda.fasurvie.managers.FileManager;
import fr.kenda.fasurvie.managers.ScoreboardManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Handles player join events including whitelist verification and player initialization
 */
public class JoinEvent implements Listener {

    private static final String WHITELIST_CONFIG = "whitelist";
    private static final String WHITELIST_KEY = "whitelist";
    private static final int LEADERBOARD_SIZE = 5;

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        try {
            if (!isPlayerWhitelisted(event.getName())) {
                String kickMessage = createWhitelistKickMessage();
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, kickMessage);
                logConnectionDenied(event);
            }
        } catch (Exception ex) {
            handleWhitelistError(event, ex);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            Player player = event.getPlayer();

            // Load player data
            initializePlayerData(player);

            // Set join message
            event.setJoinMessage(createJoinMessage(player));

            // Setup scoreboard
            initializePlayerScoreboard(player);

        } catch (Exception ex) {
            FASurvival.getInstance().getLogger().log(Level.SEVERE,
                    "Erreur lors de l'initialisation du joueur " + event.getPlayer().getName(), ex);
        }
    }

    /**
     * Checks if a player is whitelisted
     */
    private boolean isPlayerWhitelisted(String playerName) {
        FileConfiguration whitelistConfig = getWhitelistConfig();
        if (whitelistConfig == null) {
            return false;
        }

        List<String> whitelistedPlayers = whitelistConfig.getStringList(WHITELIST_KEY);
        String normalizedPlayerName = playerName.toLowerCase(Locale.ROOT);

        return whitelistedPlayers.stream()
                .anyMatch(name -> name.toLowerCase(Locale.ROOT).equals(normalizedPlayerName));
    }

    /**
     * Gets the whitelist configuration
     */
    private FileConfiguration getWhitelistConfig() {
        try {
            FileManager fileManager = FASurvival.getInstance().getManager().getManager(FileManager.class);
            return fileManager.getConfigFrom(WHITELIST_CONFIG);
        } catch (Exception ex) {
            FASurvival.getInstance().getLogger().log(Level.WARNING,
                    "Impossible de charger la configuration whitelist", ex);
            return null;
        }
    }

    /**
     * Creates the whitelist kick message with proper formatting
     */
    private String createWhitelistKickMessage() {
        return ChatColor.WHITE + "╔══════════════════════════════════════╗\n" +
                ChatColor.WHITE + "║ " + ChatColor.AQUA + ChatColor.BOLD +
                "        FreshAgency        " + ChatColor.WHITE + " ║\n" +
                ChatColor.WHITE + "╚══════════════════════════════════════╝\n\n" +
                ChatColor.RED + ChatColor.BOLD + "⚠ Accès Refusé ⚠\n\n" +
                ChatColor.GRAY + "Vous n'êtes pas autorisé à rejoindre ce serveur.\n" +
                ChatColor.GRAY + "Pour obtenir l'accès, utilisez la commande suivante " +
                ChatColor.ITALIC + "(retire les < >)\n" +
                ChatColor.GRAY + "sur notre serveur Discord dans le channel #commande-bot:\n\n" +
                ChatColor.WHITE + "┌─────────────────────────────────────┐\n" +
                ChatColor.WHITE + "│ " + ChatColor.GREEN + ChatColor.BOLD +
                "/whitelist add <votre_pseudo_minecraft>" + ChatColor.WHITE + "        │\n" +
                ChatColor.WHITE + "└─────────────────────────────────────┘\n\n" +
                ChatColor.YELLOW + "💬 Besoin d'aide ?\n" +
                ChatColor.GRAY + "Contactez un administrateur sur Discord\n\n" +
                ChatColor.DARK_GRAY + "────────────────────────────────────────";
    }

    /**
     * Logs connection denied attempt
     */
    private void logConnectionDenied(AsyncPlayerPreLoginEvent event) {
        String message = String.format(
                "Tentative de connexion refusée - Joueur: %s, IP: %s",
                event.getName(),
                event.getAddress().getHostAddress()
        );
        FASurvival.getInstance().getLogger().info(message);
    }

    /**
     * Handles whitelist verification errors
     */
    private void handleWhitelistError(AsyncPlayerPreLoginEvent event, Exception ex) {
        String errorMessage = "Erreur lors de la vérification de la whitelist pour " + event.getName();
        FASurvival.getInstance().getLogger().log(Level.WARNING, errorMessage, ex);

        String kickMessage = ChatColor.RED + "Une erreur s'est produite. Veuillez réessayer plus tard.";
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
    }

    /**
     * Initializes player data
     */
    private void initializePlayerData(Player player) {
        try {
            PlayerData playerData = new PlayerData(player);
            playerData.loadData();
        } catch (Exception ex) {
            FASurvival.getInstance().getLogger().log(Level.WARNING,
                    "Erreur lors du chargement des données pour " + player.getName(), ex);
        }
    }

    /**
     * Creates the join message
     */
    private String createJoinMessage(Player player) {
        return ChatColor.WHITE + "[" +
                ChatColor.AQUA + "+" +
                ChatColor.WHITE + "]" +
                ChatColor.YELLOW + player.getName() +
                " a rejoint le serveur";
    }

    /**
     * Initializes player scoreboard
     */
    private void initializePlayerScoreboard(Player player) {
        try {
            ScoreboardManager scoreboardManager = getScoreboardManager();
            if (scoreboardManager == null) {
                return;
            }

            // Add scoreboard for player
            scoreboardManager.addScoreboard(player);

            // Get and update leaderboard
            List<PlayerDatabase> leaderboard = getLeaderboard();
            if (leaderboard != null && !leaderboard.isEmpty()) {
                List<String> leaderboardLines = scoreboardManager.getBorderedLeaderboard(leaderboard);
                if (leaderboardLines != null && !leaderboardLines.isEmpty()) {
                    String[] linesArray = leaderboardLines.toArray(new String[0]);
                    scoreboardManager.updateLinesForPlayer(player, List.of(linesArray));
                }
            }

        } catch (Exception ex) {
            FASurvival.getInstance().getLogger().log(Level.WARNING,
                    "Erreur lors de l'initialisation du scoreboard pour " + player.getName(), ex);
        }
    }

    /**
     * Gets the scoreboard manager
     */
    private ScoreboardManager getScoreboardManager() {
        try {
            return FASurvival.getInstance().getManager().getManager(ScoreboardManager.class);
        } catch (Exception ex) {
            FASurvival.getInstance().getLogger().log(Level.WARNING,
                    "Impossible de récupérer le ScoreboardManager", ex);
            return null;
        }
    }

    /**
     * Gets the leaderboard data
     */
    private List<PlayerDatabase> getLeaderboard() {
        try {
            DatabaseManager databaseManager = FASurvival.getInstance().getManager().getManager(DatabaseManager.class);
            return databaseManager.getLeaderboard(LEADERBOARD_SIZE);
        } catch (Exception ex) {
            FASurvival.getInstance().getLogger().log(Level.WARNING,
                    "Erreur lors de la récupération du leaderboard", ex);
            return null;
        }
    }
}
