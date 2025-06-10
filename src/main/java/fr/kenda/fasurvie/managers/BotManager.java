package fr.kenda.fasurvie.managers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotManager implements IManager {

    private JDA jda;
    private final String token;
    private final String channelId;
    private final Logger logger;
    private boolean isConnected = false;

    /**
     * Constructeur du BotManager
     * @param logger Logger du plugin
     * @param token Token du bot Discord
     * @param channelId ID du channel Discord où envoyer les messages
     */
    public BotManager(Logger logger, String token, String channelId) {
        this.token = token;
        this.channelId = channelId;
        this.logger = logger;
    }

    /**
     * Se connecte au bot Discord
     */
    public void connect() {
        CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Connexion au bot Discord en cours...");

                jda = JDABuilder.createDefault(token)
                        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                        .build();

                // Attendre que le bot soit prêt
                jda.awaitReady();

                isConnected = true;
                logger.info("Bot Discord connecté avec succès !");
                return true;

            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Connexion interrompue: " + e.getMessage(), e);
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors de la connexion au bot Discord: " + e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * Envoie un embed simple dans le channel configuré
     * @param title Titre de l'embed
     * @param description Description de l'embed
     * @param color Couleur de l'embed (peut être null)
     * @return CompletableFuture<Boolean> true si l'envoi réussit
     */
    public CompletableFuture<Boolean> sendEmbed(String title, String description, Color color) {
        if (!isConnected || jda == null) {
            logger.warning("Bot non connecté. Impossible d'envoyer l'embed.");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                TextChannel channel = jda.getTextChannelById(channelId);
                if (channel == null) {
                    logger.warning("Channel avec l'ID " + channelId + " introuvable.");
                    return false;
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description);

                if (color != null) {
                    embed.setColor(color);
                } else {
                    embed.setColor(Color.BLUE); // Couleur par défaut
                }

                // Ajouter timestamp
                embed.setTimestamp(java.time.Instant.now());

                channel.sendMessageEmbeds(embed.build()).queue(
                        success -> logger.info("Embed envoyé avec succès dans le channel " + channel.getName()),
                        error -> logger.log(Level.WARNING, "Erreur lors de l'envoi de l'embed: " + error.getMessage(), error)
                );

                return true;

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors de l'envoi de l'embed: " + e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * Envoie un embed avec des champs personnalisés
     * @param title Titre de l'embed
     * @param description Description de l'embed
     * @param color Couleur de l'embed
     * @param fields Tableau de champs [nom, valeur, inline]
     * @return CompletableFuture<Boolean> true si l'envoi réussit
     */
    public CompletableFuture<Boolean> sendEmbedWithFields(String title, String description, Color color, String[][] fields) {
        if (!isConnected || jda == null) {
            logger.warning("Bot non connecté. Impossible d'envoyer l'embed.");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                TextChannel channel = jda.getTextChannelById(channelId);
                if (channel == null) {
                    logger.warning("Channel avec l'ID " + channelId + " introuvable.");
                    return false;
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setColor(color != null ? color : Color.BLUE)
                        .setTimestamp(java.time.Instant.now());

                // Ajouter les champs
                if (fields != null) {
                    for (String[] field : fields) {
                        if (field.length >= 2) {
                            boolean inline = field.length >= 3 && Boolean.parseBoolean(field[2]);
                            embed.addField(field[0], field[1], inline);
                        }
                    }
                }

                channel.sendMessageEmbeds(embed.build()).queue(
                        success -> logger.info("Embed avec champs envoyé avec succès"),
                        error -> logger.log(Level.WARNING, "Erreur lors de l'envoi de l'embed: " + error.getMessage(), error)
                );

                return true;

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors de l'envoi de l'embed avec champs: " + e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * Déconnecte le bot Discord
     */
    public void disconnect() {
        if (jda != null) {
            logger.info("Déconnexion du bot Discord...");
            jda.shutdown();
            isConnected = false;
            logger.info("Bot Discord déconnecté.");
        }
    }

    /**
     * Vérifie si le bot est connecté
     * @return true si connecté
     */
    public boolean isConnected() {
        return isConnected && jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    /**
     * Obtient l'instance JDA (pour usage avancé)
     * @return Instance JDA ou null
     */
    public JDA getJDA() {
        return jda;
    }

    /**
     * Obtient l'ID du channel configuré
     * @return ID du channel
     */
    public String getChannelId() {
        return channelId;
    }

    @Override
    public void register() {
        connect();
    }

    @Override
    public void unregister() {
        disconnect();
    }
}