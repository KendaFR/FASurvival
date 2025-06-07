package fr.kenda.fasurvie.util;

public class TimeUnit {

    public static String format(double milliseconds) {
        long totalSeconds = (long) (milliseconds / 1000);

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append("j ");
        }
        if (hours > 0) {
            result.append(hours).append("h ");
        }
        if (minutes > 0) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append("s");
        }

        return result.toString().trim();
    }

    public static String calculateDifference(double milliseconds, double objective)
    {
        return format(objective - milliseconds);
    }
}
