package fr.kenda.fasurvie.updater;

import fr.kenda.fasurvie.util.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PluginUpdater {
    private final String REPO_OWNER = "KendaFR";
    private final String REPO_NAME = "FASurvival";
    private final String CURRENT_VERSION = "1.0";

    public void checkForUpdates() {
        try {
            String inputLine;
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", "KendaFR", "FASurvival");
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();
            JSONObject jsonResponse = new JSONObject(content.toString());
            String latestVersion = jsonResponse.getString("tag_name");
            if (!"1.0".equals(latestVersion)) {
                Logger.info("Une nouvelle version est disponible : " + latestVersion);
            } else {
                Logger.success("Vous utilisez la dernière version.");
            }
        } catch (Exception e) {
            Logger.error("Erreur lors de la vérification des mises à jour : " + e.getMessage());
        }
    }
}

