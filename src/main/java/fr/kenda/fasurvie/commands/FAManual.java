package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Config;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class FAManual implements CommandExecutor {

    private static final Pattern LINK_PATTERN = Pattern.compile("\\[(.*?)\\]\\((.*?)\\)");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCette commande ne peut être utilisée que par un joueur !");
            return true;
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        if (bookMeta == null) {
            return false;
        }

        bookMeta.setTitle(ChatColor.GOLD + "Livre des règles !");
        bookMeta.setAuthor("Server");

        FileConfiguration config = FASurvival.getInstance().getConfig();
        ConfigurationSection bookSection = config.getConfigurationSection("book");

        if (bookSection != null) {
            this.loadBookPages(bookSection, config, bookMeta);
        } else {
            System.out.println("Section 'book' non trouvée dans la configuration");
        }

        book.setItemMeta(bookMeta);
        player.openBook(book);

        return true;
    }

    private void loadBookPages(ConfigurationSection bookSection, FileConfiguration config, BookMeta bookMeta) {
        Set<String> keys = bookSection.getKeys(false);
        List<Integer> pageNumbers = keys.stream()
                .map(Integer::parseInt)
                .sorted()
                .toList();

        for (Integer pageNum : pageNumbers) {
            List<String> pageLines = config.getStringList("book." + pageNum);
            if (pageLines.isEmpty()) {
                continue;
            }

            List<BaseComponent> pageComponents = this.buildPageComponents(pageLines);
            BaseComponent[] pageArray = pageComponents.toArray(new BaseComponent[0]);
            bookMeta.spigot().addPage(new BaseComponent[][]{pageArray});
        }
    }

    private List<BaseComponent> buildPageComponents(List<String> pageLines) {
        List<BaseComponent> pageComponents = new ArrayList<>();

        for (int i = 0; i < pageLines.size(); i++) {
            String line = pageLines.get(i);
            // Remplacer les placeholders {X}, {Y}, {Z} par les valeurs de la config
            String processedLine = this.replacePlaceholders(line);
            List<BaseComponent> lineComponents = this.processLineWithLinks(processedLine);
            pageComponents.addAll(lineComponents);

            // Ajouter un saut de ligne si ce n'est pas la dernière ligne
            if (i < pageLines.size() - 1) {
                pageComponents.add(new TextComponent("\n"));
            }
        }

        return pageComponents;
    }

    private List<BaseComponent> processLineWithLinks(String line) {
        List<BaseComponent> components = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(line);
        int lastEnd = 0;

        while (matcher.find()) {
            // Ajouter le texte avant le lien
            String beforeLink = line.substring(lastEnd, matcher.start());
            if (!beforeLink.isEmpty()) {
                components.add(new TextComponent(beforeLink));
            }

            // Créer le composant lien
            String destination = matcher.group(1);
            String displayText = matcher.group(2);
            TextComponent linkComponent = this.createLinkComponent(destination, displayText);
            components.add(linkComponent);

            lastEnd = matcher.end();
        }

        // Ajouter le texte restant après le dernier lien
        if (lastEnd < line.length()) {
            components.add(new TextComponent(line.substring(lastEnd)));
        }

        // Si aucun lien n'a été trouvé, ajouter la ligne entière
        if (components.isEmpty()) {
            components.add(new TextComponent(line));
        }

        return components;
    }

    private TextComponent createLinkComponent(String destination, String displayText) {
        TextComponent linkComponent = new TextComponent(displayText);
        linkComponent.setColor(net.md_5.bungee.api.ChatColor.BLUE);
        linkComponent.setUnderlined(true);

        if (destination.startsWith("http://") || destination.startsWith("https://")) {
            this.setupUrlLink(linkComponent, destination);
        } else if (destination.startsWith("page ")) {
            this.setupPageLink(linkComponent, destination);
        } else {
            this.setupCommandLink(linkComponent, destination);
        }

        return linkComponent;
    }

    private void setupUrlLink(TextComponent linkComponent, String url) {
        linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        linkComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Cliquer pour ouvrir: " + url).create()
        ));
    }

    private void setupPageLink(TextComponent linkComponent, String destination) {
        String pageNum = destination.substring(5);
        linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, pageNum));
        linkComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Voir la page " + pageNum).create()
        ));
    }

    private void setupCommandLink(TextComponent linkComponent, String command) {
        linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        linkComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Exécuter: " + command).create()
        ));
    }

    private String replacePlaceholders(String line) {
        String result = line;

        // Remplacer {X} par la valeur de pnj.location.X
        if (result.contains("{X}")) {
            int xValue = Config.getInt("pnj.location.X");
            result = result.replace("{X}", String.valueOf(xValue));
        }

        // Remplacer {Y} par la valeur de pnj.location.Y
        if (result.contains("{Y}")) {
            int yValue = Config.getInt("pnj.location.Y");
            result = result.replace("{Y}", String.valueOf(yValue));
        }

        // Remplacer {Z} par la valeur de pnj.location.Z
        if (result.contains("{Z}")) {
            int zValue = Config.getInt("pnj.location.Z");
            result = result.replace("{Z}", String.valueOf(zValue));
        }

        return result;
    }
}