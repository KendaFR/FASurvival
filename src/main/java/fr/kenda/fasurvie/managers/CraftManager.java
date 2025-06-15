package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.Config;
import fr.kenda.fasurvie.util.Constant;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class CraftManager
        implements IManager {
    @Override
    public void register() {
        CraftManager.registerCraft(Constant.FRESH_COIN_ITEM, "fresh_coin");
    }

    @Override
    public void unregister() {
    }

    private static void registerCraft(ItemStack freshCoins, String key) {
        ShapedRecipe recipe = new ShapedRecipe(freshCoins);
        String[] craftingTable = Config.getList(key + ".craft").toArray(new String[2]);
        recipe.shape(craftingTable[0], craftingTable[1], craftingTable[2]);
        FASurvival.getInstance().getConfig().getConfigurationSection(key + ".reference").getKeys(true).forEach(s -> recipe.setIngredient(s.charAt(0), Config.getMaterial(key + ".reference." + s)));
        Bukkit.addRecipe(recipe);
    }
}

