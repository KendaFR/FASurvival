package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotManager extends ListenerAdapter implements IManager {
    private JDA jda;
    private final String token;
    private final String channelId;
    private final Logger logger;
    private boolean isConnected = false;
    private static final String CATEGORY = "whitelist";
    private static final String SERVER_IP = "srv01.uniheberg.fr:25512";

    public BotManager(Logger logger, String token, String channelId) {
        this.token = token;
        this.channelId = channelId;
        this.logger = logger;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getChannel().getId().equals(this.channelId)) {
            return;
        }

        String message = event.getMessage().getContentRaw().toLowerCase();
        if (message.startsWith("/whitelist add")) {
            String[] args = message.split(" ");
            if (args.length >= 3) {
                this.handleWhitelistCommand(event.getAuthor(), args[2]);
            }
        }
    }

    @Override
    public void register() {
        this.connect();
    }

    @Override
    public void unregister() {
        this.disconnect();
    }

    public void connect() {
        CompletableFuture.runAsync(() -> {
            try {
                this.logger.info("Connexion au bot Discord...");
                this.jda = JDABuilder.createDefault(this.token)
                        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                        .addEventListeners(this)
                        .build()
                        .awaitReady();

                this.isConnected = true;
                this.logger.info("Bot Discord connecté !");

                // Clear le channel avant d'envoyer l'embed de connexion
                this.clearChannel(() -> {
                    this.sendConnectionEmbed();
                    this.logger.info("Channel cleared et embed de connexion envoyé !");
                });

            } catch (Exception e) {
                this.logger.log(Level.SEVERE, "Erreur connexion Discord: " + e.getMessage(), e);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public void disconnect() {
        if (this.jda != null) {
            this.logger.info("Déconnexion bot Discord...");
            this.sendDisconnectionEmbed();
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.jda.shutdown();
            this.isConnected = false;
        }
    }

    public boolean isConnected() {
        return this.isConnected && this.jda != null && this.jda.getStatus() == JDA.Status.CONNECTED;
    }

    public JDA getJDA() {
        return this.jda;
    }

    public String getChannelId() {
        return this.channelId;
    }

    private void clearChannel(Runnable onComplete) {
        if (!this.isConnected()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = this.jda.getTextChannelById(this.channelId);
                if (channel != null) {
                    this.logger.info("Nettoyage du channel en cours...");

                    // Récupérer les messages et les supprimer
                    channel.getHistory().retrievePast(100).queue(messages -> {
                        if (!messages.isEmpty()) {
                            this.deleteMessagesWithFallback(channel, messages, onComplete);
                        } else {
                            this.logger.info("Aucun message à supprimer dans le channel.");
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        }
                    }, error -> {
                        this.logger.log(Level.WARNING, "Erreur lors de la récupération des messages: " + error.getMessage(), error);
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });
                } else {
                    this.logger.warning("Channel introuvable avec l'ID: " + this.channelId);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            } catch (Exception e) {
                this.logger.log(Level.WARNING, "Erreur lors du nettoyage du channel: " + e.getMessage(), e);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private void deleteMessagesWithFallback(TextChannel channel, List<Message> messages, Runnable onComplete) {
        List<Message> recentMessages = messages.stream()
                .filter(msg -> msg.getTimeCreated().isAfter(OffsetDateTime.now().minusDays(14)))
                .toList();

        if (recentMessages.size() == 1) {
            recentMessages.get(0).delete().queue(
                    success -> {
                        logger.info("Message récent supprimé.");
                        if (onComplete != null) onComplete.run();
                    },
                    error -> {
                        logger.log(Level.WARNING, "Erreur suppression: " + error.getMessage(), error);
                        if (onComplete != null) onComplete.run();
                    }
            );
        } else if (!recentMessages.isEmpty()) {
            channel.deleteMessages(recentMessages).queue(
                    success -> {
                        logger.info("Messages récents supprimés par lot.");
                        if (onComplete != null) onComplete.run();
                    },
                    error -> {
                        logger.warning("Erreur suppression par lot: " + error.getMessage());
                        // Fallback individuel
                        deleteMessagesIndividually(recentMessages, 0, onComplete);
                    }
            );
        } else {
            logger.info("Aucun message récent, suppression individuelle.");
            deleteMessagesIndividually(messages, 0, onComplete);
        }
    }


    private void deleteMessagesIndividually(List<net.dv8tion.jda.api.entities.Message> messages, int index, Runnable onComplete) {
        if (index >= messages.size()) {
            this.logger.info("Channel nettoyé avec succès ! (" + messages.size() + " messages supprimés individuellement)");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        messages.get(index).delete().queue(
                success -> {
                    // Petite pause pour éviter le rate limiting
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    // Supprimer le message suivant
                    this.deleteMessagesIndividually(messages, index + 1, onComplete);
                },
                error -> {
                    this.logger.log(Level.WARNING, "Impossible de supprimer le message " + (index + 1) + ": " + error.getMessage());
                    // Continuer avec le message suivant même en cas d'erreur
                    this.deleteMessagesIndividually(messages, index + 1, onComplete);
                }
        );
    }

    private void handleWhitelistCommand(User user, String playerName) {
        FileManager fileManager = FASurvival.getInstance().getManager().getManager(FileManager.class);
        FileConfiguration config = fileManager.getConfigFrom(CATEGORY);
        List<String> whitelisted = config.getStringList(CATEGORY);

        if (whitelisted.contains(playerName)) {
            this.sendPrivateEmbed(user, this.createAlreadyWhitelistEmbed(playerName));
        } else {
            whitelisted.add(playerName);
            config.set(CATEGORY, whitelisted);
            fileManager.saveConfigFrom(CATEGORY);
            this.sendPrivateEmbed(user, this.createWhitelistSuccessEmbed(playerName));
        }
    }

    private void sendConnectionEmbed() {
        this.sendChannelEmbed(new EmbedBuilder()
                .setTitle("🟢 Serveur FaSurvie • En ligne")
                .setDescription("**Le serveur est maintenant disponible !**\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "🎮 **Rejoignez-nous dès maintenant :**\n" +
                        "```/whitelist add <votre_pseudo>```\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(65416)
                .setThumbnail("https://cdn.discordapp.com/emojis/862726637089292329.png")
                .addField("📡 Statut", "✅ **Connecté**", true)
                .addField("🎯 Version", "**1.16.5+**", true)
                .addField("🌍 IP", "**" + SERVER_IP + "**", true)
                .setFooter("FaSurvie • Système automatique", "https://minotar.net/avatar/Steve/64")
                .setTimestamp(Instant.now()));
    }

    private EmbedBuilder createWhitelistSuccessEmbed(String playerName) {
        return new EmbedBuilder()
                .setTitle("🎉 Whitelist • Bienvenue !")
                .setDescription("**Félicitations !** Vous avez été ajouté à la whitelist\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "🎮 **Pseudo :** `" + playerName + "`\n" +
                        "✅ **Statut :** Approuvé\n" +
                        "🏠 **Serveur :** FreshAgency\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(54442)
                .setThumbnail("https://minotar.net/avatar/" + playerName + "/128")
                .addField("🌍 Connexion au serveur", "```" + SERVER_IP + "```", false)
                .addField("🎊 Message de bienvenue",
                        "Bienvenue sur **FaSurvie** ! 🎮\n" +
                                "Respectez les règles et amusez-vous bien !\n" +
                                "N'hésite pas à voir lire le règlement en jeu avec `/manuel`\n" +
                                "En cas de problème, contactez un Fresh, Loann ou Kenda.", false)
                .setFooter("FaSurvie • Whitelist automatique", "https://minotar.net/avatar/" + playerName + "/32")
                .setTimestamp(Instant.now());
    }

    private EmbedBuilder createAlreadyWhitelistEmbed(String playerName) {
        return new EmbedBuilder()
                .setTitle("⚠️ Whitelist • Déjà enregistré")
                .setDescription("**Information :** Ce joueur est déjà whitelisté\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "🎮 **Pseudo :** `" + playerName + "`\n" +
                        "✅ **Statut :** Déjà approuvé\n" +
                        "🏠 **Serveur :** FaSurvie\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0xFFAA00)
                .setThumbnail("https://minotar.net/avatar/" + playerName + "/128")
                .addField("🌍 Connexion au serveur", "```" + SERVER_IP + "```", false)
                .addField("💡 Information",
                        "Vous pouvez vous connecter directement au serveur.\n" +
                                "Aucune action supplémentaire n'est nécessaire.", false)
                .setFooter("FaSurvie • Whitelist automatique", "https://minotar.net/avatar/" + playerName + "/32")
                .setTimestamp(Instant.now());
    }

    private void sendPrivateEmbed(User user, EmbedBuilder embed) {
        user.openPrivateChannel().queue(
                channel -> channel.sendMessageEmbeds(embed.build()).queue(
                        success -> this.logger.info("Embed envoyé en MP à " + user.getName()),
                        error -> this.logger.log(Level.WARNING, "Erreur envoi MP: " + error.getMessage(), error)
                ),
                error -> this.logger.log(Level.WARNING, "Impossible d'ouvrir MP avec " + user.getName(), error)
        );
    }

    private void sendChannelEmbed(EmbedBuilder embed) {
        if (!this.isConnected()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = this.jda.getTextChannelById(this.channelId);
                if (channel != null) {
                    channel.sendMessageEmbeds(embed.build()).queue();
                }
            } catch (Exception e) {
                this.logger.log(Level.WARNING, "Erreur envoi embed: " + e.getMessage(), e);
            }
        });
    }

    private void sendDisconnectionEmbed() {
        this.sendChannelEmbed(new EmbedBuilder()
                .setTitle("🔴 Serveur FaSurvie • Hors ligne")
                .setDescription("**Le serveur est maintenant déconnecté**\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "⚠️ **Maintenance en cours**\n" +
                        "Le serveur sera disponible prochainement\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0xFF4444)
                .setThumbnail("https://cdn.discordapp.com/emojis/862726711199408148.png")
                .addField("📡 Statut", "❌ **Hors ligne**", true)
                .addField("⏰ Durée", "**Indéterminée**", true)
                .setFooter("FaSurvie • Système automatique", "https://minotar.net/avatar/Steve/64")
                .setTimestamp(Instant.now()));
    }
    /**
     * Envoie un embed annonçant le gagnant de la semaine
     * @param winnerName Le nom du joueur gagnant
     * @param achievement La réalisation/raison de la victoire
     * @param reward La récompense obtenue (optionnel)
     */
    public void sendWeeklyWinnerEmbed(String winnerName, String reward) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Gagnant de la Semaine • FaSurvie")
                .setDescription("**Félicitations à notre champion de la semaine !**\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "🎉 **Bravo à** `" + winnerName + "` **!**\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .setColor(0xFFD700) // Couleur or
                .setThumbnail("https://minotar.net/avatar/" + winnerName + "/128");

        // Ajouter la récompense si elle est fournie
        if (reward != null && !reward.trim().isEmpty()) {
            embed.addField("🎁 Récompense", reward, false);
        }

        embed.addField("🎊 Message de félicitations",
                        "Un grand bravo à **" + winnerName + "** pour cette performance exceptionnelle !\n" +
                                "Continue comme ça ! 🌟", false)
                .setFooter("FaSurvie • Gagnant de la semaine", "https://minotar.net/avatar/" + winnerName + "/32")
                .setTimestamp(Instant.now());

        this.sendChannelEmbed(embed);
        this.logger.info("Embed du gagnant de la semaine envoyé pour: " + winnerName);
    }

    /**
     * Version surchargée sans récompense
     * @param winnerName Le nom du joueur gagnant
     * @param achievement La réalisation/raison de la victoire
     */
    public void sendWeeklyWinnerEmbed(String winnerName) {
        this.sendWeeklyWinnerEmbed(winnerName, null);
    }
}