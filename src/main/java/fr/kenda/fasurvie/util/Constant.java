package fr.kenda.fasurvie.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Constant {
    public static final String FRESH_COIN = Config.getString("fresh_coin.name");
    public static final ItemStack FRESH_COIN_ITEM = new ItemBuilder(Material.NETHER_STAR).displayName(FRESH_COIN).lore(Config.getList("fresh_coin.lore")).build();
}

