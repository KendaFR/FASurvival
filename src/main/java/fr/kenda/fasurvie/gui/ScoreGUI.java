package fr.kenda.fasurvie.gui;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.managers.DataManager;
import fr.kenda.fasurvie.managers.ScoreboardManager;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.Constant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreGUI
        extends Gui {
    private final Map<UUID, List<ItemStack>> tempStorage = new HashMap<UUID, List<ItemStack>>();

    public ScoreGUI(String title, int row) {
        super(title, row);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != this.inventory) {
            return;
        }
        ItemStack itemToPlace = null;
        switch (event.getAction()) {
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
            case SWAP_WITH_CURSOR: {
                itemToPlace = event.getCursor();
                break;
            }
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD: {
                itemToPlace = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                break;
            }
            case MOVE_TO_OTHER_INVENTORY: {
                itemToPlace = event.getCurrentItem();
                break;
            }
        }
        if (itemToPlace != null && !this.isFreshCoins(itemToPlace)) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(FASurvival.PREFIX + ChatColor.RED + "Seuls les FreshCoins sont autorisés !");
        }
    }

    @Override
    public void handleInventoryDrag(InventoryDragEvent event) {
        boolean dragToCustomInventory = event.getRawSlots().stream().anyMatch(slot -> slot < this.inventory.getSize());
        if (dragToCustomInventory && !this.isFreshCoins(event.getCursor())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(FASurvival.PREFIX + ChatColor.RED + "Seuls les FreshCoins sont autorisés !");
        }
    }

    @Override
    public void handleInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(Config.getString("pnj.name"))) {
            return;
        }
        Player player = (Player) event.getPlayer();
        List<ItemStack> items = Arrays.stream(this.inventory.getContents()).filter(item -> item != null && item.getType() != Material.AIR).map(ItemStack::clone).collect(Collectors.toList());
        if (!items.isEmpty()) {
            this.tempStorage.put(player.getUniqueId(), items);
            Bukkit.getScheduler().runTaskAsynchronously(FASurvival.getInstance(), () -> this.processCoinsAsync(player.getUniqueId()));
        }
    }

    @Override
    public ItemStack[] getMainContents() {
        return new ItemStack[this.size];
    }

    private void processCoinsAsync(UUID playerId) {
        List<ItemStack> items = this.tempStorage.get(playerId);
        if (items == null) {
            return;
        }
        int totalCoins = items.stream().filter(this::isFreshCoins).mapToInt(ItemStack::getAmount).sum();
        Bukkit.getScheduler().runTask(FASurvival.getInstance(), () -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && totalCoins > 0) {
                this.saveCoinsPlayer(player, totalCoins);
            }
            this.tempStorage.remove(playerId);
        });
    }

    private boolean isFreshCoins(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        Objects.requireNonNull(item.getItemMeta()).getDisplayName();
        return item.getItemMeta().getDisplayName().equalsIgnoreCase(Constant.FRESH_COIN);
    }

    private void saveCoinsPlayer(Player player, int coins) {
        player.sendMessage(FASurvival.PREFIX + ChatColor.GOLD + "Vous avez déposé " + ChatColor.AQUA + coins + ChatColor.GOLD + " coins.");
        FASurvival.getInstance().getManager().getManager(DataManager.class).getPlayerDataFromPlayer(player).addCoins(coins);
        FASurvival.getInstance().getManager().getManager(DataManager.class).getPlayerDataFromPlayer(player).saveData(false);
        Bukkit.getScheduler().runTaskLater(FASurvival.getInstance(), () -> {
            if (Config.getInt("refresh_time") <= 0) {
                FASurvival.getInstance().getManager().getManager(ScoreboardManager.class).refreshLeaderboard();
            }
        }, 20L);
    }
}