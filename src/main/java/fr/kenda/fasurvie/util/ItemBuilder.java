package fr.kenda.fasurvie.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import com.google.gson.Gson;

public class ItemBuilder {

    private ItemStack item;
    private Material material;
    private int amount = 1;
    private short damage = 0;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private String displayName;
    private List<String> lore = new ArrayList<>();
    private List<ItemFlag> flags = new ArrayList<>();
    private boolean andSymbol = true;
    private boolean unsafeStackSize = false;

    // Constructeurs

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        if (material == null) material = Material.AIR;
        if (amount > material.getMaxStackSize() || amount <= 0) amount = 1;
        this.material = material;
        this.amount = amount;
        this.item = new ItemStack(material, amount);
    }

    public ItemBuilder(Material material, int amount, String displayName) {
        this(material, amount);
        this.displayName = displayName;
    }

    public ItemBuilder(ItemStack item) {
        Objects.requireNonNull(item, "ItemStack ne peut pas être null.");
        this.item = item.clone();
        this.material = item.getType();
        this.amount = item.getAmount();
        this.damage = item.getDurability();

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            this.displayName = meta.getDisplayName();
            this.lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            this.flags.addAll(meta.getItemFlags());
            this.enchantments.putAll(item.getEnchantments());
        }
    }

    public ItemBuilder(FileConfiguration cfg, String path) {
        this(cfg.getItemStack(path));
    }

    // Setters fluents

    public ItemBuilder amount(int amount) {
        if ((amount > material.getMaxStackSize() || amount <= 0) && !unsafeStackSize) amount = 1;
        this.amount = amount;
        return this;
    }

    /** Pour wool/terracotta/etc (avant 1.13) -> set le data value = color */
    public ItemBuilder color(byte colorData) {
        this.damage = colorData;
        return this;
    }

    /** Pour wool/terracotta/etc (avant 1.13) -> set le data value = color (shortcut) */
    public ItemBuilder color(int colorData) {
        this.damage = (short)(colorData & 0xF);
        return this;
    }

    public ItemBuilder durability(short durability) {
        this.damage = durability;
        return this;
    }

    public ItemBuilder material(Material material) {
        Objects.requireNonNull(material, "Le material ne peut pas être null.");
        this.material = material;
        return this;
    }

    public ItemBuilder enchant(Enchantment enchant, int level) {
        Objects.requireNonNull(enchant, "Enchantement ne peut pas être null.");
        this.enchantments.put(enchant, level);
        return this;
    }

    public ItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        Objects.requireNonNull(enchantments, "Enchantements ne peut pas être null.");
        this.enchantments = enchantments;
        return this;
    }

    public ItemBuilder displayName(String displayName) {
        Objects.requireNonNull(displayName, "Le displayName ne peut pas être null.");
        this.displayName = andSymbol ? ChatColor.translateAlternateColorCodes('&', displayName) : displayName;
        return this;
    }

    public ItemBuilder lore(String line) {
        Objects.requireNonNull(line, "La ligne du lore ne peut pas être nulle.");
        this.lore.add(andSymbol ? ChatColor.translateAlternateColorCodes('&', line) : line);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        Objects.requireNonNull(lore, "Le lore ne peut pas être null.");
        this.lore = new ArrayList<>();
        for (String line : lore) {
            this.lore.add(andSymbol ? ChatColor.translateAlternateColorCodes('&', line) : line);
        }
        return this;
    }

    public ItemBuilder lore(String... lines) {
        for (String line : lines) {
            lore(line);
        }
        return this;
    }

    public ItemBuilder lore(String line, int index) {
        while (lore.size() <= index) {
            lore.add("");
        }
        this.lore.set(index, andSymbol ? ChatColor.translateAlternateColorCodes('&', line) : line);
        return this;
    }

    public ItemBuilder flag(ItemFlag flag) {
        Objects.requireNonNull(flag, "Le flag ne peut pas être null.");
        this.flags.add(flag);
        return this;
    }

    public ItemBuilder flag(List<ItemFlag> flags) {
        this.flags = flags;
        return this;
    }

    public ItemBuilder glow() {
        // Fait briller l'item sans afficher les enchantements
        enchant(Enchantment.ARROW_INFINITE, 1);
        flag(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder replaceAndSymbol(boolean replace) {
        this.andSymbol = replace;
        return this;
    }

    public ItemBuilder toggleReplaceAndSymbol() {
        this.andSymbol = !andSymbol;
        return this;
    }

    public ItemBuilder unsafeStackSize(boolean allow) {
        this.unsafeStackSize = allow;
        return this;
    }

    public ItemBuilder toggleUnsafeStackSize() {
        this.unsafeStackSize = !unsafeStackSize;
        return this;
    }

    // Getters

    public String getDisplayName() {
        return displayName;
    }

    public int getAmount() {
        return amount;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public short getDurability() {
        return damage;
    }

    public List<String> getLore() {
        return lore;
    }

    public boolean isAndSymbol() {
        return andSymbol;
    }

    public List<ItemFlag> getFlags() {
        return flags;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemStack build() {
        item.setType(material);
        item.setAmount(amount);
        item.setDurability(damage);

        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
        if (meta == null) meta = item.getItemMeta();
        if (displayName != null) meta.setDisplayName(displayName);
        if (!lore.isEmpty()) meta.setLore(lore);
        for (ItemFlag f : flags) meta.addItemFlags(f);
        item.setItemMeta(meta);

        if (!enchantments.isEmpty()) item.addUnsafeEnchantments(enchantments);

        return item;
    }
}