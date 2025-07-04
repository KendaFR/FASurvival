package fr.kenda.fasurvie.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemBuilder {
    private final ItemStack item;
    private Material material;
    private int amount = 1;
    private short damage = 0;
    private Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
    private String displayName;
    private List<String> lore = new ArrayList<String>();
    private List<ItemFlag> flags = new ArrayList<ItemFlag>();
    private boolean andSymbol = true;
    private boolean unsafeStackSize = false;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        if (material == null) {
            material = Material.AIR;
        }
        if (amount > material.getMaxStackSize() || amount <= 0) {
            amount = 1;
        }
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
            this.lore = meta.getLore() != null ? meta.getLore() : new ArrayList();
            this.flags.addAll(meta.getItemFlags());
            this.enchantments.putAll(item.getEnchantments());
        }
    }

    public ItemBuilder(FileConfiguration cfg, String path) {
        this(cfg.getItemStack(path));
    }

    public ItemBuilder amount(int amount) {
        if (!(amount <= this.material.getMaxStackSize() && amount > 0 || this.unsafeStackSize)) {
            amount = 1;
        }
        this.amount = amount;
        return this;
    }

    public ItemBuilder color(byte colorData) {
        this.damage = colorData;
        return this;
    }

    public ItemBuilder color(int colorData) {
        this.damage = (short) (colorData & 0xF);
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
        this.displayName = this.andSymbol ? ChatColor.translateAlternateColorCodes('&', displayName) : displayName;
        return this;
    }

    public ItemBuilder lore(String line) {
        Objects.requireNonNull(line, "La ligne du lore ne peut pas être nulle.");
        this.lore.add(this.andSymbol ? ChatColor.translateAlternateColorCodes('&', line) : line);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        Objects.requireNonNull(lore, "Le lore ne peut pas être null.");
        this.lore = new ArrayList<String>();
        for (String line : lore) {
            this.lore.add(this.andSymbol ? ChatColor.translateAlternateColorCodes('&', line) : line);
        }
        return this;
    }

    public ItemBuilder lore(String... lines) {
        for (String line : lines) {
            this.lore(line);
        }
        return this;
    }

    public ItemBuilder lore(String line, int index) {
        while (this.lore.size() <= index) {
            this.lore.add("");
        }
        this.lore.set(index, this.andSymbol ? ChatColor.translateAlternateColorCodes('&', line) : line);
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
        this.enchant(Enchantment.ARROW_INFINITE, 1);
        this.flag(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder replaceAndSymbol(boolean replace) {
        this.andSymbol = replace;
        return this;
    }

    public ItemBuilder toggleReplaceAndSymbol() {
        this.andSymbol = !this.andSymbol;
        return this;
    }

    public ItemBuilder unsafeStackSize(boolean allow) {
        this.unsafeStackSize = allow;
        return this;
    }

    public ItemBuilder toggleUnsafeStackSize() {
        this.unsafeStackSize = !this.unsafeStackSize;
        return this;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getAmount() {
        return this.amount;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return this.enchantments;
    }

    public short getDurability() {
        return this.damage;
    }

    public List<String> getLore() {
        return this.lore;
    }

    public boolean isAndSymbol() {
        return this.andSymbol;
    }

    public List<ItemFlag> getFlags() {
        return this.flags;
    }

    public Material getMaterial() {
        return this.material;
    }

    public ItemStack build() {
        ItemMeta meta;
        this.item.setType(this.material);
        this.item.setAmount(this.amount);
        this.item.setDurability(this.damage);
        ItemMeta itemMeta = meta = this.item.hasItemMeta() ? this.item.getItemMeta() : null;
        if (meta == null) {
            meta = this.item.getItemMeta();
        }
        if (this.displayName != null) {
            meta.setDisplayName(this.displayName);
        }
        if (!this.lore.isEmpty()) {
            meta.setLore(this.lore);
        }
        for (ItemFlag f : this.flags) {
            meta.addItemFlags(f);
        }
        this.item.setItemMeta(meta);
        if (!this.enchantments.isEmpty()) {
            this.item.addUnsafeEnchantments(this.enchantments);
        }
        return this.item;
    }
}

