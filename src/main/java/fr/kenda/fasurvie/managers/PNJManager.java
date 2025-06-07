package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.gui.ScoreGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PNJManager implements IManager, Listener {

    private final JavaPlugin plugin;
    private Villager pnj;
    private final Set<UUID> pnjUUIDs;
    private String pnjName;

    public PNJManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pnjUUIDs = new HashSet<>();
    }

    @Override
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.pnjName = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("pnj.name", "&6&lMarchand de Scores"));


        Bukkit.getWorlds().forEach(world ->
                world.getEntities().forEach(entity -> {
                    if (entity instanceof Villager) {
                        Villager v = (Villager) entity;
                        if (v.getCustomName() != null) {
                            v.remove();
                        }
                    }
                })
        );

        createPNJ();
    }

    @Override
    public void unregister() {
        if (pnj != null && !pnj.isDead()) {
            pnj.remove();
        }
        pnjUUIDs.clear();
    }

    private void createPNJ() {
        Location location = getLocationFromConfig();

        if (location == null) {
            plugin.getLogger().warning("Impossible de créer le PNJ : localisation invalide !");
            return;
        }

        pnj = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);

        pnj.setCustomName(pnjName);
        pnj.setCustomNameVisible(true);
        pnj.setCanPickupItems(false);
        pnj.setRemoveWhenFarAway(false);
        pnj.setProfession(Villager.Profession.LIBRARIAN);
        pnjUUIDs.add(pnj.getUniqueId());

        plugin.getLogger().info("PNJ créé avec succès à la position : " +
                location.getX() + ", " + location.getY() + ", " + location.getZ());
    }

    private Location getLocationFromConfig() {
        try {
            String worldName = plugin.getConfig().getString("pnj.location.world");
            double x = plugin.getConfig().getDouble("pnj.location.x");
            double y = plugin.getConfig().getDouble("pnj.location.y");
            double z = plugin.getConfig().getDouble("pnj.location.z");

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("Monde '" + worldName + "' introuvable !");
                return null;
            }

            Location loc = new Location(world, x, y, z);
            loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));
            return loc;
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors du chargement de la localisation du PNJ : " + e.getMessage());
            return null;
        }
    }

    /**
     * Bloque toutes les interactions avec le PNJ.
     */

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!pnjUUIDs.contains(event.getRightClicked().getUniqueId())) return;
        event.setCancelled(true);
        handlePNJInteraction(event.getPlayer());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (pnjUUIDs.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (pnjUUIDs.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (pnjUUIDs.contains(event.getEntered().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (pnjUUIDs.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private void handlePNJInteraction(Player player) {
        new ScoreGUI(pnjName, 5).create(player);
    }

    public void recreatePNJ() {
        unregister();
        register();
    }

    public Location getPNJLocation() {
        return pnj != null ? pnj.getLocation() : null;
    }
}
