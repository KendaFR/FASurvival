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
        if (this.owner == null) {
            this.owner = player;
        }
        this.inventory = Bukkit.createInventory(this.owner, this.size, this.title);
        this.updateContents(this.getMainContents());
        player.openInventory(this.inventory);
        this.playerListener = new Listener() {

            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (!(event.getWhoClicked() instanceof Player)) {
                    return;
                }
                if (!event.getWhoClicked().equals(Gui.this.owner)) {
                    return;
                }
                Gui.this.handleInventoryClick(event);
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (!(event.getPlayer() instanceof Player)) {
                    return;
                }
                if (!event.getPlayer().equals(Gui.this.owner)) {
                    return;
                }
                Gui.this.handleInventoryClose(event);
                HandlerList.unregisterAll(Gui.this.playerListener);
            }

            @EventHandler
            public void onInventoryDrag(InventoryDragEvent event) {
                if (!(event.getWhoClicked() instanceof Player)) {
                    return;
                }
                if (!event.getWhoClicked().equals(Gui.this.owner)) {
                    return;
                }
                if (!event.getView().getTitle().equals(Gui.this.title)) {
                    return;
                }
                Gui.this.handleInventoryDrag(event);
            }
        };
        Bukkit.getPluginManager().registerEvents(this.playerListener, FASurvival.getInstance());
    }

    public void create() {
        this.create(this.owner);
    }

    public void updateContents(ItemStack[] contents) {
        this.inventory.setContents(contents);
    }

    public abstract void handleInventoryClick(InventoryClickEvent var1);

    public abstract void handleInventoryDrag(InventoryDragEvent var1);

    public abstract void handleInventoryClose(InventoryCloseEvent var1);

    public abstract ItemStack[] getMainContents();

    protected String title;
    protected int size;
    protected Player owner;
    protected Inventory inventory;
}

