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

enum Direction {
    NORTH("Nord", "↑"),              // U+2191
    NORTH_EAST("Nord-Est", "↗"),     // U+2197
    EAST("Est", "→"),                // U+2192
    SOUTH_EAST("Sud-Est", "↘"),      // U+2198
    SOUTH("Sud", "↓"),               // U+2193
    SOUTH_WEST("Sud-Ouest", "↙"),    // U+2199
    WEST("Ouest", "←"),              // U+2190
    NORTH_WEST("Nord-Ouest", "↖");   // U+2196

    private final String name;
    private final String emoji;

    Direction(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public String getName() { return name; }
    public String getEmoji() { return emoji; }

    @Override
    public String toString() {
        return emoji + " " + name;
    }
}

class TrackerResult {
    private final Player target;
    private final double distance;
    private final Location location;
    private final Direction direction;

    public TrackerResult(Player target, double distance, Location location, Direction direction) {
        this.target = target;
        this.distance = distance;
        this.location = location;
        this.direction = direction;
    }

    public Player getTarget() { return target; }
    public double getDistance() { return distance; }
    public Location getLocation() { return location; }
    public Direction getDirection() { return direction; }
}

public class TrackerManager implements IManager {

    private final Map<UUID, Long> trackerCooldowns = new HashMap<>();
    private int cooldown;


    public void register() {
        cooldown = FASurvival.getInstance().getConfig().getInt("cooldown_between_use");

        Bukkit.getScheduler().runTaskTimer(FASurvival.getInstance(), () ->
                trackerCooldowns.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null), 0L, 600 * 20L);
    }


    @Override
    public void unregister() {
        trackerCooldowns.clear();
    }

    public void useTracker(Player player) {
        long now = System.currentTimeMillis();
        if (trackerCooldowns.containsKey(player.getUniqueId())) {
            long lastUse = trackerCooldowns.get(player.getUniqueId());
            long left = lastUse + cooldown * 1000L - now;
            if (left > 0) {
                double remaining = left / 1000.0;
                player.sendMessage(FASurvival.PREFIX + ChatColor.RED +
                        "Veuillez patienter " + String.format("%.1f", remaining) + "s avant de réutiliser le tracker.");
                return;
            }
        }

        ItemStack tracker = player.getItemInHand();
        if (tracker == null) return;

        ItemMeta meta = tracker.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return;

        int lastIndex = lore.size() - 1;
        String l = lore.get(lastIndex);

        Pattern pattern = Pattern.compile("(\\d+)/(\\d+)");
        Matcher matcher = pattern.matcher(ChatColor.stripColor(l));

        if (matcher.find() && sendTrackerInformationValid(player)) {

            int value = Integer.parseInt(matcher.group(1));
            int max = Integer.parseInt(matcher.group(2));

            value = Math.max(0, value - 1);

            String color = ChatColor.GRAY.toString();
            if (value == 0) color = ChatColor.RED.toString();

            lore.set(lastIndex, color + "(" + value + "/" + max + ")");
            meta.setLore(lore);
            tracker.setItemMeta(meta);

            player.setItemInHand(value == 0 ? null : tracker);
            trackerCooldowns.put(player.getUniqueId(), now);

            if (value == 0) {
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
                player.sendMessage(FASurvival.PREFIX + ChatColor.RED + "Vous avez épuisé votre tracker.");
            }
        }
    }


    private boolean sendTrackerInformationValid(Player player) {
        TrackerResult trackerResult = getClosestPlayerInfo(player);
        if (trackerResult == null) {
            TitleActionUtil.sendActionBar(player, ChatColor.RED + "Aucun joueur aux alentours.", 2);
            return false;
        }
        TitleActionUtil.sendActionBar(player,
                trackerResult.getDirection().getEmoji() + " " +
                        ChatColor.WHITE + trackerResult.getTarget().getName() +
                        " | (" + ChatColor.GOLD + String.format("%.1f", trackerResult.getDistance()) + ChatColor.WHITE + " blocs)",
                FASurvival.getInstance().getConfig().getInt("duration_tracker"));
        return true;
    }

    public TrackerResult getClosestPlayerInfo(Player player) {
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;
        Location playerLoc = player.getLocation();

        for (Player target : player.getWorld().getPlayers()) {
            if (target.equals(player)) continue;

            double distance = playerLoc.distance(target.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = target;
            }
        }

        if (closest != null) {
            Location targetLoc = closest.getLocation();
            Direction direction = getDirection(playerLoc, targetLoc);
            return new TrackerResult(closest, closestDistance, targetLoc, direction);
        } else {
            return null;
        }
    }

    private Direction getDirection(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        double absoluteAngle = Math.toDegrees(Math.atan2(-dx, dz));

        absoluteAngle = (absoluteAngle + 360) % 360;

        float playerYaw = from.getYaw();

        double yaw = (playerYaw + 360) % 360;

        double relativeAngle = (absoluteAngle - yaw + 360) % 360;

        if (relativeAngle >= 337.5 || relativeAngle < 22.5) return Direction.NORTH;
        if (relativeAngle < 67.5) return Direction.NORTH_EAST;
        if (relativeAngle < 112.5) return Direction.EAST;
        if (relativeAngle < 157.5) return Direction.SOUTH_EAST;
        if (relativeAngle < 202.5) return Direction.SOUTH;
        if (relativeAngle < 247.5) return Direction.SOUTH_WEST;
        if (relativeAngle < 292.5) return Direction.WEST;
        return Direction.NORTH_WEST;
    }
}