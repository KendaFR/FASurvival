package fr.kenda.fasurvie.util;

public class TimeUnit {
    public static String format(double milliseconds) {
        long totalSeconds = (long) (milliseconds / 1000.0);
        long days = totalSeconds / 86400L;
        long hours = totalSeconds % 86400L / 3600L;
        long minutes = totalSeconds % 3600L / 60L;
        long seconds = totalSeconds % 60L;
        StringBuilder result = new StringBuilder();
        if (days > 0L) {
            result.append(days).append("j ");
        }
        if (hours > 0L) {
            result.append(hours).append("h ");
        }
        if (minutes > 0L) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0L || result.length() == 0) {
            result.append(seconds).append("s");
        }
        return result.toString().trim();
    }

    public static String calculateDifference(double current, double target) {
        return TimeUnit.format(target - current);
    }

    public static long millisUntilNextHour() {
        long now = System.currentTimeMillis();
        return now - now % 3600000L + 3600000L;
    }

    public static long millisUntilNext(long intervalMs) {
        long now = System.currentTimeMillis();
        long remainder = now % intervalMs;
        return intervalMs - remainder;
    }
}

