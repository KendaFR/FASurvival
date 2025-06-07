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

    public static String calculateDifference(double current, double target) {
        return format(target - current);
    }

    public static long millisUntilNextHour() {
        long now = System.currentTimeMillis();
        return now - (now % (60 * 60 * 1000)) + (60 * 60 * 1000);
    }

    /**
     * Calcule le temps en millisecondes jusqu'au prochain intervalle
     * @param intervalMs Intervalle en millisecondes (ex: 60000 pour 1 minute, 3600000 pour 1 heure)
     * @return Millisecondes jusqu'au prochain intervalle complet
     */
    public static long millisUntilNext(long intervalMs) {
        long now = System.currentTimeMillis();
        long remainder = now % intervalMs;
        return intervalMs - remainder;
    }
}