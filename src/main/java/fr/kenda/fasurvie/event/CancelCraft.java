package fr.kenda.fasurvie.event;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Constant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class CancelCraft implements Listener {

    private boolean isFreshCoin(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("FreshCoins");
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (isFreshCoin(item)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                event.getView().getPlayer().sendMessage(ChatColor.RED + "Tu ne peux pas crafter cet item avec un " + Constant.FRESH_COIN);
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (event.isShiftClick() && isFreshCoin(clickedItem)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu ne peux pas déplacer un " + Constant.FRESH_COIN + ChatColor.RED + " dans une enclume !");
            return;
        }

        if (event.getSlot() <= 2 && isFreshCoin(cursorItem)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu ne peux pas placer un " + Constant.FRESH_COIN + ChatColor.RED + " dans une enclume !");
            return;
        }

        if (event.isShiftClick() && event.getSlot() > 2 && isFreshCoin(clickedItem)) {
            AnvilInventory anvil = (AnvilInventory) event.getInventory();
            if (anvil.getItem(0) == null || anvil.getItem(1) == null) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Tu ne peux pas placer un " + Constant.FRESH_COIN + ChatColor.RED + " dans une enclume !");
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL) {
            final Player player = (Player) event.getPlayer();
            final AnvilInventory anvil = (AnvilInventory) event.getInventory();

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.getOpenInventory().getType().equals(InventoryType.ANVIL)) {
                        this.cancel();
                        return;
                    }

                    if (isFreshCoin(anvil.getItem(0)) || isFreshCoin(anvil.getItem(1))) {
                        anvil.setItem(2, new ItemStack(Material.AIR));
                    }
                }
            }.runTaskTimer(FASurvival.getInstance(), 1L, 1L);
        }
    }
}