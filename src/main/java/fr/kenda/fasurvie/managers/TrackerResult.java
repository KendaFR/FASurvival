package fr.kenda.fasurvie.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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

    public Player getTarget() {
        return this.target;
    }

    public double getDistance() {
        return this.distance;
    }

    public Location getLocation() {
        return this.location;
    }

    public Direction getDirection() {
        return this.direction;
    }
}

