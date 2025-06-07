package fr.kenda.fasurvie.gui;


import fr.kenda.fasurvie.FASurvival;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Gui {

    private Listener playerListener;
    public Gui(String title, int row) {
        this.title = title;
        this.size = row * 9;
    }

    public void create(Player player) {
        if (owner == null) {
            owner = player;
        }

        inventory = Bukkit.createInventory(owner, size, title);
        updateContents(getMainContents());
        player.openInventory(inventory);

        this.playerListener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (!(event.getWhoClicked() instanceof Player)) {
                    return;
                }
                if (!event.getWhoClicked().equals(owner)) {
                    return;
                }
                handleInventoryClick(event);
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (!(event.getPlayer() instanceof Player)) {
                    return;
                }
                if (!event.getPlayer().equals(owner)) {
                    return;
                }

                handleInventoryClose(event);
                HandlerList.unregisterAll(playerListener);
            }

            @EventHandler
            public void onInventoryDrag(InventoryDragEvent event) {
                if (!(event.getWhoClicked() instanceof Player)) {
                    return;
                }
                if (!event.getWhoClicked().equals(owner)) {
                    return;
                }
                if (!event.getView().getTitle().equals(title)) {
                    return;
                }

                handleInventoryDrag(event);
            }
        };

        Bukkit.getPluginManager().registerEvents(playerListener, FASurvival.getInstance());
    }

    public void create() {
        create(owner);
    }

    public void updateContents(ItemStack[] contents) {
        inventory.setContents(contents);
    }

    public abstract void handleInventoryClick(InventoryClickEvent event);

    public abstract void handleInventoryDrag(InventoryDragEvent event);

    public abstract void handleInventoryClose(InventoryCloseEvent event);

    public abstract ItemStack[] getMainContents();
    protected String title;
    protected int size;
    protected Player owner;
    protected Inventory inventory;
}
