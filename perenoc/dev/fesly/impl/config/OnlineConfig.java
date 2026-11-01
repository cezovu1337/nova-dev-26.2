package dev.fesly.impl.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.fesly.Nova;
import dev.fesly.client.theme.Themes;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class OnlineConfig {
    public static void loadConfig(final String name) {
        try {
            final HttpsURLConnection connection =
                    (HttpsURLConnection) new URL("https://raw.githubusercontent.com/Intave-dev/HeavlyConfigs/main/configs/" + name)
                            .openConnection();

            connection.setConnectTimeout(5000);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

            final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String current;
            final ArrayList<String> response = new ArrayList<>();

            while ((current = in.readLine()) != null) {
                response.add(current);
            }

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = (JsonObject) parser.parse(in.lines().collect(Collectors.joining()));
            // checks if there was data read
            if (jsonObject == null) {
                return;
            }

            Nova.getInstance().getDraggableManager().load(jsonObject.getAsJsonObject("draggables"));
            Nova.getInstance().getModuleManager().load(jsonObject.getAsJsonObject("modules"));
            Nova.getInstance().getClientUI().load(jsonObject.getAsJsonObject("clientUi"));

        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }

    public static List<String> getAvailableConfigs() {
        try {
            final HttpsURLConnection connection =
                    (HttpsURLConnection) new URL("https://raw.githubusercontent.com/Intave-dev/HeavlyConfigs/main/index")
                            .openConnection();

            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

            final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String current;
            final ArrayList<String> response = new ArrayList<>();

            while ((current = in.readLine()) != null) {
                response.add(current);
            }

            return response;
        } catch (final Throwable t) {
            t.printStackTrace();
        }

        return null;
    }
}
