package fr.kenda.fasurvie.event;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.managers.TrackerManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TrackerEvent implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null)
            return;

        if (item.getType() == Material.COMPASS && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getEnchantments().containsKey(Enchantment.ARROW_INFINITE)) {
            Action action = e.getAction();
            Player player = e.getPlayer();
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                FASurvival.getInstance().getManager().getManager(TrackerManager.class).useTracker(player);

        }
    }
}