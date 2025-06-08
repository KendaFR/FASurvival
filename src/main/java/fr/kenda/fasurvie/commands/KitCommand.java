package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.managers.FileManager;
import fr.kenda.fasurvie.gui.KitGui;
import fr.kenda.fasurvie.util.FileName;
import fr.kenda.fasurvie.util.InventorySerialize;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Stream;

public class KitCommand implements SubCommand {

    String pathKit = "kits.";

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Vous devez être un joueur pour utiliser cette commande.");
            return false;
        }

        Player player = (Player) sender;
        FileManager fileManager = FASurvival.getInstance().getManager().getManager(FileManager.class);

        if (args.length == 0) {
            Map<String, ItemStack[]> kits = getAllKits(fileManager.getConfigFrom(FileName.KIT_FILE));
            if(kits.isEmpty())
            {
                sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Aucun kits enregistré.");
                return false;
            }
            KitGui kitGui = new KitGui(ChatColor.BLACK + "Kits", 6);
            kitGui.setKits(getAllKits(fileManager.getConfigFrom(FileName.KIT_FILE)));
            kitGui.create(player);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            if (!hasAtLeastOneItem(player)) {
                player.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Vous ne pouvez pas crée de kit vide.");
                return false;
            }
            handleCreateKit(fileManager, player, args[1].toLowerCase());
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            handleDeleteKit(fileManager, player, args[1].toLowerCase());
            return true;
        }
        else if(args.length == 3)
        {
            handleGiveKit(fileManager, player, args[1], args[2].toLowerCase());
            return true;
        }
        return true;
    }

    private void handleGiveKit(FileManager fileManager, CommandSender sender, String target, String kit) {
        Player t = Bukkit.getPlayer(target);
        if (t == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Le joueur " + target + " n'est pas connecté.");
            return;
        }

        // Check if the kit exists before trying to deserialize
        FileConfiguration config = fileManager.getConfigFrom(FileName.KIT_FILE);
        String kitData = config.getString(pathKit + kit);
        if (kitData == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Le kit '" + kit + "' n'existe pas.");
            return;
        }

        ItemStack[] k;
        try {
            k = InventorySerialize.deserializeInventory(kitData);
            for (ItemStack itemStack : k) {
                if (itemStack == null) continue;
                HashMap<Integer, ItemStack> left = t.getInventory().addItem(itemStack);
                if (!left.isEmpty()) {
                    for (ItemStack rest : left.values()) {
                        t.getWorld().dropItemNaturally(t.getLocation(), rest);
                    }
                }
            }
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Kit " + kit + " donné au joueur " + target + ".");
            t.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Vous avez reçu le kit " + kit + ".");
        } catch (Exception e) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Erreur lors de la récupération du kit !");
            e.printStackTrace(); // Keep the stack trace for debugging
        }
    }

    private boolean hasAtLeastOneItem(Player player) {
        return Stream.concat(
                        Arrays.stream(player.getInventory().getContents()),
                        Arrays.stream(player.getInventory().getArmorContents())
                )
                .anyMatch(item -> item != null && item.getType() != Material.AIR);
    }

    private void handleDeleteKit(FileManager fileManager, Player player, String arg) {
        FileConfiguration config = fileManager.getConfigFrom(FileName.KIT_FILE);
        if (!config.contains(pathKit + arg)) {
            player.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Le kit '" + arg + "' n'existe pas.");
            return;
        }
        config.set(pathKit + arg, null);
        if (fileManager.saveConfigFrom(FileName.KIT_FILE)) {
            player.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Suppression du kit '" + arg + "' effectuée.");
        }
    }

    private void handleCreateKit(FileManager fileManager, Player player, String arg) {
        FileConfiguration config = fileManager.getConfigFrom(FileName.KIT_FILE);
        try {
            config.set(pathKit + arg, InventorySerialize.serializeInventory(player.getInventory()));
            if (fileManager.saveConfigFrom(FileName.KIT_FILE)) {
                player.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Sauvegarde du kit '" + arg + "' effectuée.");
                player.getInventory().clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "kits";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() + " : " + ChatColor.RED + "Afficher tous les kits disponibles");
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() + " create <name> : " + ChatColor.RED + "Créer un kit à partir de votre inventaire");
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() + " delete <name> : " + ChatColor.RED + "Supprimer un kit");
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + getName() + " give <player> <kit> : " + ChatColor.RED + "Donne un kit au joueur");
    }

    private Map<String, ItemStack[]> getAllKits(FileConfiguration configuration) {
        Map<String, ItemStack[]> kits = new HashMap<>();
        if(configuration.getConfigurationSection(pathKit) == null)
            return kits;

        configuration.getConfigurationSection(pathKit).getKeys(false).forEach(kit -> {
            try {
                String kitData = configuration.getString(pathKit + kit);
                if (kitData != null) { // Only process non-null kit data
                    ItemStack[] inventory = InventorySerialize.deserializeInventory(kitData);
                    kits.put(kit, inventory);
                } else {
                    // Log warning about corrupted kit data
                    System.out.println("Warning: Kit '" + kit + "' has null data and will be skipped.");
                }
            } catch (Exception e) {
                System.out.println("Error loading kit '" + kit + "': " + e.getMessage());
                e.printStackTrace();
            }
        });
        return kits;
    }
}