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

public class PNJManager
        implements IManager,
        Listener {
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
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        this.pnjName = ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("pnj.name",
                "&6&lMarchand de Scores"));
        Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(entity -> {
            Villager v;
            if (entity instanceof Villager && (v = (Villager) entity).getCustomName() != null) {
                v.remove();
            }
        }));
        this.createPNJ();
    }

    @Override
    public void unregister() {
        if (this.pnj != null && !this.pnj.isDead()) {
            this.pnj.remove();
        }
        this.pnjUUIDs.clear();
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!this.pnjUUIDs.contains(event.getRightClicked().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        this.handlePNJInteraction(event.getPlayer());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.pnjUUIDs.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (this.pnjUUIDs.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (this.pnjUUIDs.contains(event.getEntered().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (this.pnjUUIDs.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public void recreatePNJ() {
        this.unregister();
        this.register();
    }

    public Location getPNJLocation() {
        return this.pnj != null ? this.pnj.getLocation() : null;
    }

    private void createPNJ() {
        Location location = this.getLocationFromConfig();
        if (location == null) {
            this.plugin.getLogger().warning("Impossible de créer le PNJ : localisation invalide !");
            return;
        }
        this.pnj = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        this.pnj.setCustomName(this.pnjName);
        this.pnj.setCustomNameVisible(true);
        this.pnj.setCanPickupItems(false);
        this.pnj.setRemoveWhenFarAway(false);
        this.pnj.setProfession(Villager.Profession.LIBRARIAN);
        this.pnjUUIDs.add(this.pnj.getUniqueId());
        this.plugin.getLogger().info("PNJ créé avec succès à la position : " + location.getX() + ", " + location.getY() + ", " + location.getZ());
    }

    private Location getLocationFromConfig() {
        try {
            String worldName = this.plugin.getConfig().getString("pnj.location.world");
            double x = this.plugin.getConfig().getDouble("pnj.location.x");
            double y = this.plugin.getConfig().getDouble("pnj.location.y");
            double z = this.plugin.getConfig().getDouble("pnj.location.z");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                this.plugin.getLogger().warning("Monde '" + worldName + "' introuvable !");
                return null;
            }
            return new Location(world, x, y, z);
        } catch (Exception e) {
            this.plugin.getLogger().severe("Erreur lors du chargement de la localisation du PNJ : " + e.getMessage());
            return null;
        }
    }

    private void handlePNJInteraction(Player player) {
        new ScoreGUI(this.pnjName, 5).create(player);
    }
}

