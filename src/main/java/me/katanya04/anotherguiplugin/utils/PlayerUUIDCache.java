package me.katanya04.anotherguiplugin.utils;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PlayerUUIDCache {
    private static final String API_URL = "https://api.minecraftservices.com/minecraft/profile/lookup/name/%s";
    private static final LinkedHashMap<String, UUID> cache = new LinkedHashMap<String, UUID>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, UUID> eldest) {
            return size() > MAX_SIZE;
        }
    };
    private static final LinkedHashSet<String> noPremium = new LinkedHashSet<String>() {
        @Override
        public boolean add(String e) {
            if (this.size() >= MAX_SIZE)
                this.remove(this.iterator().next());
            return super.add(e);
        }
    };
    private static int MAX_SIZE;
    public static void initialize() {
        MAX_SIZE = AnotherGUIPlugin.getConfiguration().getInt("player-uuid-cache-settings.max-size-entries", 100);
        if (MAX_SIZE < 1) {
            AnotherGUIPlugin.getLog().log(Level.WARNING, "Invalid player uuid cache size, setting to default value (100)");
            MAX_SIZE = 100;
        }
    }
    static UUID getUUIDFromCache(String playerName) {
        return cache.get(playerName);
    }
    static void addToCache(String playerName, UUID uuid) {
        cache.put(playerName, uuid);
    }
    static CompletableFuture<UUID> getUUIDMojang(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(String.format(API_URL, playerName));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5 * 1000); //5 seconds
                connection.setRequestProperty("Accept", "application/json");

                int statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    JSONObject json = readJSON(connection);
                    UUID uuid = Utils.UUIDFromUnformattedString((String) json.get("id"));
                    addToCache(playerName, uuid);
                    return uuid;
                }
                if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    noPremium.add(playerName);
                    return null;
                }
                AnotherGUIPlugin.getLog().log(Level.SEVERE, "HTTP response code was not OK: " + statusCode);
                return null;
            } catch (MalformedURLException ex) {
                AnotherGUIPlugin.getLog().log(Level.SEVERE, "Incorrect URL when trying to connect to Mojang API");
            } catch (IOException ex) {
                AnotherGUIPlugin.getLog().log(Level.SEVERE, "IOException when trying to retrieve player's UUID");
            }
            return null;
        });
    }
    static UUID getUUIDLocal(String playerName) {
        return UUID.nameUUIDFromBytes(playerName.getBytes(StandardCharsets.UTF_8));
    }
    static boolean isNotPremium(String playerName) {
        return noPremium.contains(playerName);
    }
    private static JSONObject readJSON(HttpURLConnection connection) {
        try (
        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Object json = new JSONParser().parse(inputStreamReader);
            if (json instanceof JSONObject)
                return (JSONObject) json;
            else
                throw new RuntimeException("Expected response to be represented as a JSON Object, instead we got: " + json);
        } catch (IOException ex) {
            throw new RuntimeException("Could not read http response body", ex);
        } catch (ParseException ex) {
            throw new RuntimeException("Invalid JSON from api", ex);
        }
    }
    private PlayerUUIDCache() {}
    public static class PutOnCacheOnJoin implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            String playerName = e.getPlayer().getName();
            if (!PlayerUUIDCache.cache.containsKey(playerName) && PlayerUUIDCache.noPremium.contains(playerName))
                PlayerUUIDCache.getUUIDMojang(playerName);
        }
    }
}