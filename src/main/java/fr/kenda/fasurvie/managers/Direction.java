package fr.kenda.fasurvie.managers;

enum Direction {
    NORTH("Nord", "↑"),
    NORTH_EAST("Nord-Est", "↗"),
    EAST("Est", "→"),
    SOUTH_EAST("Sud-Est", "↘"),
    SOUTH("Sud", "↓"),
    SOUTH_WEST("Sud-Ouest", "↙"),
    WEST("Ouest", "←"),
    NORTH_WEST("Nord-Ouest", "↖");

    private final String name;
    private final String emoji;

    public String toString() {
        return this.emoji + " " + this.name;
    }

    public String getName() {
        return this.name;
    }

    public String getEmoji() {
        return this.emoji;
    }

    Direction(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }
}

