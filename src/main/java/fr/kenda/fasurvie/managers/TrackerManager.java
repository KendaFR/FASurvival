package fr.kenda.fasurvie.managers;

import fr.kenda.fasurvie.FASurvival;
import fr.kenda.fasurvie.util.TitleActionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackerManager
        implements IManager {
    private final Map<UUID, Long> trackerCooldowns = new HashMap<UUID, Long>();
    private int cooldown;

    @Override
    public void register() {
        this.cooldown = FASurvival.getInstance().getConfig().getInt("cooldown_between_use");
        Bukkit.getScheduler().runTaskTimer(FASurvival.getInstance(), () -> this.trackerCooldowns.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null), 0L, 12000L);
    }

    @Override
    public void unregister() {
        this.trackerCooldowns.clear();
    }

    public void useTracker(Player player) {
        long lastUse;
        long left;
        long now = System.currentTimeMillis();
        if (this.trackerCooldowns.containsKey(player.getUniqueId()) && (left = (lastUse = this.trackerCooldowns.get(player.getUniqueId()).longValue()) + (long) this.cooldown * 1000L - now) > 0L) {
            double remaining = (double) left / 1000.0;
            player.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Veuillez patienter " + String.format("%.1f", remaining) + "s avant de réutiliser le tracker.");
            return;
        }
        ItemStack tracker = player.getItemInHand();
        if (tracker == null) {
            return;
        }
        ItemMeta meta = tracker.getItemMeta();
        if (meta == null) {
            return;
        }
        List lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return;
        }
        int lastIndex = lore.size() - 1;
        String l = (String) lore.get(lastIndex);
        Pattern pattern = Pattern.compile("(\\d+)/(\\d+)");
        Matcher matcher = pattern.matcher(ChatColor.stripColor(l));
        if (matcher.find() && this.sendTrackerInformationValid(player)) {
            int value = Integer.parseInt(matcher.group(1));
            int max = Integer.parseInt(matcher.group(2));
            value = Math.max(0, value - 1);
            String color = ChatColor.GRAY.toString();
            if (value == 0) {
                color = ChatColor.RED.toString();
            }
            lore.set(lastIndex, color + "(" + value + "/" + max + ")");
            meta.setLore(lore);
            tracker.setItemMeta(meta);
            player.setItemInHand(value == 0 ? null : tracker);
            this.trackerCooldowns.put(player.getUniqueId(), now);
            if (value == 0) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                player.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Vous avez épuisé votre tracker.");
            }
        }
    }

    public TrackerResult getClosestPlayerInfo(Player player) {
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;
        Location playerLoc = player.getLocation();
        for (Player target : player.getWorld().getPlayers()) {
            double distance;
            if (target.equals(player) || !((distance = playerLoc.distance(target.getLocation())) < closestDistance))
                continue;
            closestDistance = distance;
            closest = target;
        }
        if (closest != null) {
            Location targetLoc = closest.getLocation();
            Direction direction = this.getDirection(playerLoc, targetLoc);
            return new TrackerResult(closest, closestDistance, targetLoc, direction);
        }
        return null;
    }

    private boolean sendTrackerInformationValid(Player player) {
        TrackerResult trackerResult = this.getClosestPlayerInfo(player);
        if (trackerResult == null) {
            TitleActionUtil.sendActionBar(player, ChatColor.RED + "Aucun joueur aux alentours.", 2);
            return false;
        }
        TitleActionUtil.sendActionBar(player, trackerResult.getDirection().getEmoji() + " " + ChatColor.WHITE + trackerResult.getTarget().getName() + " | (" + ChatColor.GOLD + String.format("%.1f", trackerResult.getDistance()) + ChatColor.WHITE + " blocs)", FASurvival.getInstance().getConfig().getInt("duration_tracker"));
        return true;
    }

    private Direction getDirection(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double absoluteAngle = Math.toDegrees(Math.atan2(-dx, dz));
        float playerYaw = from.getYaw();
        double yaw = (playerYaw + 360.0f) % 360.0f;
        double relativeAngle = ((absoluteAngle = (absoluteAngle + 360.0) % 360.0) - yaw + 360.0) % 360.0;
        if (relativeAngle >= 337.5 || relativeAngle < 22.5) {
            return Direction.NORTH;
        }
        if (relativeAngle < 67.5) {
            return Direction.NORTH_EAST;
        }
        if (relativeAngle < 112.5) {
            return Direction.EAST;
        }
        if (relativeAngle < 157.5) {
            return Direction.SOUTH_EAST;
        }
        if (relativeAngle < 202.5) {
            return Direction.SOUTH;
        }
        if (relativeAngle < 247.5) {
            return Direction.SOUTH_WEST;
        }
        if (relativeAngle < 292.5) {
            return Direction.WEST;
        }
        return Direction.NORTH_WEST;
    }
}

