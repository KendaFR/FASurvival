package fr.kenda.fasurvie.updater;

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
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", REPO_OWNER, REPO_NAME);
            URL url = new URL(apiUrl);

            // Ouvrir la connexion
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            // Lire la réponse
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Fermer les flux
            in.close();
            connection.disconnect();

            // Analyser la réponse JSON
            JSONObject jsonResponse = new JSONObject(content.toString());
            String latestVersion = jsonResponse.getString("tag_name");

            // Comparer les versions
            if (!CURRENT_VERSION.equals(latestVersion)) {
                System.out.println("Une nouvelle version est disponible : " + latestVersion);
            } else {
                System.out.println("Vous utilisez la dernière version.");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification des mises à jour : " + e.getMessage());
        }
    }
}
