package fr.kenda.fasurvie.gui;

import fr.kenda.fasurvie.util.ItemBuilder;
import fr.kenda.fasurvie.util.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitGui extends Gui {
    private Map<String, ItemStack[]> kits = new HashMap<>();
    private final Map<Integer, String> kitSlot = new HashMap<>();

    public KitGui(String title, int row) {
        super(title, row);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) {
            return;
        }
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inventory)) {
            return;
        }

        event.setCancelled(true);
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        if (current.getType() == Material.IRON_DOOR) {
            updateContents(getMainContents());
        } else {
            String kitName = kitSlot.get(event.getSlot());
            if (kitName != null) {
                updateContents(showKit(kitName));
            }
        }
    }

    @Override
    public void handleInventoryDrag(InventoryDragEvent event) {

    }

    @Override
    public void handleInventoryClose(InventoryCloseEvent event) {
        kitSlot.clear();
        kits.clear();
    }

    @Override
    public ItemStack[] getMainContents() {
        kitSlot.clear();
        ItemStack[] content = new ItemStack[size];
        ItemStack decorator = new ItemBuilder(Material.STAINED_GLASS_PANE).color((byte)14).build();
        Arrays.fill(content, decorator);

        int slot = 10;
        for (Map.Entry<String, ItemStack[]> kitEntry : kits.entrySet()) {
            if (slot >= size) break;

            String name = kitEntry.getKey();
            ItemStack icon = kitEntry.getValue()[0] != null
                    ? new ItemBuilder(kitEntry.getValue()[0].getType()).displayName(ChatColor.WHITE + "Kit: " + ChatColor.YELLOW + name).build()
                    : new ItemBuilder(Material.BARRIER).displayName(ChatColor.RED + "Kit Invalide").build();
            content[slot] = icon;
            kitSlot.put(slot, name);

            slot++;
            if ((slot - 1) % 9 == 7) {
                slot += 2;
            }
        }
        return content;
    }

    public ItemStack[] showKit(String kitName) {
        ItemStack[] content = new ItemStack[size];
        ItemStack back = new ItemBuilder(Material.IRON_DOOR).displayName(ChatColor.RED + "Retour").build();
        content[size - 1] = back;
        ItemStack[] inventory = kits.get(kitName);
        if (inventory == null) return content;

        for (int i = 0; i < inventory.length && i < content.length - 1; i++) {
            content[i] = inventory[i];
        }
        return content;
    }

    public void setKits(Map<String, ItemStack[]> allKits) {
        kits = allKits != null ? allKits : new HashMap<>();
    }
}