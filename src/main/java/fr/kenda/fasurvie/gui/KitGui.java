package fr.kenda.fasurvie.gui;

import fr.kenda.fasurvie.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KitGui
        extends Gui {
    private Map<String, ItemStack[]> kits = new HashMap<String, ItemStack[]>();
    private final Map<Integer, String> kitSlot = new HashMap<Integer, String>();

    public KitGui(String title, int row) {
        super(title, row);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(this.title)) {
            return;
        }
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(this.inventory)) {
            return;
        }
        event.setCancelled(true);
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }
        if (current.getType() == Material.IRON_DOOR) {
            this.updateContents(this.getMainContents());
        } else {
            String kitName = this.kitSlot.get(event.getSlot());
            if (kitName != null) {
                this.updateContents(this.showKit(kitName));
            }
        }
    }

    @Override
    public void handleInventoryDrag(InventoryDragEvent event) {
    }

    @Override
    public void handleInventoryClose(InventoryCloseEvent event) {
        this.kitSlot.clear();
        this.kits.clear();
    }

    @Override
    public ItemStack[] getMainContents() {
        this.kitSlot.clear();
        ItemStack[] content = new ItemStack[this.size];
        ItemStack decorator = new ItemBuilder(Material.RED_STAINED_GLASS).build();
        Arrays.fill(content, decorator);
        int slot = 10;
        for (Map.Entry<String, ItemStack[]> kitEntry : this.kits.entrySet()) {
            if (slot >= this.size) break;
            String name = kitEntry.getKey();
            ItemStack icon = kitEntry.getValue()[0] != null ? new ItemBuilder(kitEntry.getValue()[0].getType()).displayName(ChatColor.WHITE + "Kit: " + ChatColor.YELLOW + name).build() : new ItemBuilder(Material.BARRIER).displayName(ChatColor.RED + "Kit Invalide").build();
            content[slot] = icon;
            this.kitSlot.put(slot, name);
            if ((++slot - 1) % 9 != 7) continue;
            slot += 2;
        }
        return content;
    }

    public ItemStack[] showKit(String kitName) {
        ItemStack back;
        ItemStack[] content = new ItemStack[this.size];
        content[this.size - 1] = back = new ItemBuilder(Material.IRON_DOOR).displayName(ChatColor.RED + "Retour").build();
        ItemStack[] inventory = this.kits.get(kitName);
        if (inventory == null) {
            return content;
        }
        for (int i = 0; i < inventory.length && i < content.length - 1; ++i) {
            content[i] = inventory[i];
        }
        return content;
    }

    public void setKits(Map<String, ItemStack[]> allKits) {
        this.kits = allKits != null ? allKits : new HashMap();
    }
}

