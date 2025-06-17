package fr.kenda.fasurvie.commands;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.gui.KitGui;
import fr.kenda.fasurvie.managers.FileManager;
import fr.kenda.fasurvie.util.InventorySerialize;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class KitCommand
        implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args2, boolean isForced) {
        FileManager fileManager = FASurvival.getInstance().getManager().getManager(FileManager.class);
        if (args2.length == 0 && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Vous devez être un joueur pour fire cette commande");
            return false;
        }
        if (args2.length == 0) {
            Player player = (Player) sender;
            Map<String, ItemStack[]> kits = this.getAllKits(fileManager.getConfigFrom("kits"));
            if (kits.isEmpty()) {
                sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Aucun kits enregistré.");
                return false;
            }
            KitGui kitGui = new KitGui(ChatColor.BLACK + "Kits", 6);
            kitGui.setKits(this.getAllKits(fileManager.getConfigFrom("kits")));
            kitGui.create(player);
            return true;
        }
        if (args2.length == 2 && args2[0].equalsIgnoreCase("create") && sender instanceof Player player) {
            if (!this.hasAtLeastOneItem(player)) {
                player.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Vous ne pouvez pas crée de kit vide.");
                return false;
            }
            this.handleCreateKit(fileManager, player, args2[1].toLowerCase());
            return true;
        }
        if (args2.length == 2 && args2[0].equalsIgnoreCase("delete")) {
            this.handleDeleteKit(fileManager, sender, args2[1].toLowerCase());
            return true;
        }
        if (args2.length == 3) {
            this.handleGiveKit(fileManager, sender, args2[1], args2[2].toLowerCase());
            return true;
        }
        return true;
    }

    @Override
    public String getName() {
        return "kits";
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " : " + ChatColor.RED + "Afficher tous les kits disponibles");
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " create <name> : " + ChatColor.RED + "Créer un kit à partir de votre inventaire");
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " delete <name> : " + ChatColor.RED + "Supprimer un kit");
        sender.sendMessage(FASurvival.PREFIX + ChatColor.GRAY + "/fas " + this.getName() + " give <player> <kit> : " + ChatColor.RED + "Donne un kit au joueur");
    }

    private void handleGiveKit(FileManager fileManager, CommandSender sender, String target, String kit) {
        Player t = Bukkit.getPlayer(target);
        if (t == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Le joueur " + target + " n'est pas connecté.");
            return;
        }
        FileConfiguration config = fileManager.getConfigFrom("kits");
        String kitData = config.getString(this.pathKit + kit);
        if (kitData == null) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Le kit '" + kit + "' n'existe pas.");
            return;
        }
        try {
            for (ItemStack itemStack : InventorySerialize.deserializeInventory(kitData)) {
                if (itemStack == null) continue;
                HashMap<Integer, ItemStack> left = t.getInventory().addItem(itemStack);
                if (left.isEmpty()) continue;
                for (ItemStack rest : left.values()) {
                    t.getWorld().dropItemNaturally(t.getLocation(), rest);
                }
            }
            sender.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Kit " + kit + " donné au joueur " + target + ".");
            t.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Vous avez reçu le kit " + kit + ".");
        } catch (Exception e) {
            sender.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Erreur lors de la récupération du kit !");
            e.printStackTrace();
        }
    }

    private boolean hasAtLeastOneItem(Player player) {
        return Stream.concat(Arrays.stream(player.getInventory().getContents()), Arrays.stream(player.getInventory().getArmorContents())).anyMatch(item -> item != null && item.getType() != Material.AIR);
    }

    private void handleDeleteKit(FileManager fileManager, CommandSender player, String arg) {
        FileConfiguration config = fileManager.getConfigFrom("kits");
        if (!config.contains(this.pathKit + arg)) {
            player.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Le kit '" + arg + "' n'existe pas.");
            return;
        }
        config.set(this.pathKit + arg, null);
        if (fileManager.saveConfigFrom("kits")) {
            player.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Suppression du kit '" + arg + "' effectuée.");
        }
    }

    private void handleCreateKit(FileManager fileManager, Player player, String arg) {
        FileConfiguration config = fileManager.getConfigFrom("kits");
        try {
            config.set(this.pathKit + arg, InventorySerialize.serializeInventory(player.getInventory()));
            if (fileManager.saveConfigFrom("kits")) {
                player.sendMessage(FASurvival.PREFIX + ChatColor.GREEN + "Sauvegarde du kit '" + arg + "' effectuée.");
                player.getInventory().clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, ItemStack[]> getAllKits(FileConfiguration configuration) {
        HashMap<String, ItemStack[]> kits = new HashMap<String, ItemStack[]>();
        if (configuration.getConfigurationSection(this.pathKit) == null) {
            return kits;
        }
        configuration.getConfigurationSection(this.pathKit).getKeys(false).forEach(kit -> {
            try {
                String kitData = configuration.getString(this.pathKit + kit);
                if (kitData != null) {
                    ItemStack[] inventory = InventorySerialize.deserializeInventory(kitData);
                    kits.put(kit, inventory);
                } else {
                    System.out.println("Warning: Kit '" + kit + "' has null data and will be skipped.");
                }
            } catch (Exception e) {
                System.out.println("Error loading kit '" + kit + "': " + e.getMessage());
                e.printStackTrace();
            }
        });
        return kits;
    }

    String pathKit = "kits.";
}

