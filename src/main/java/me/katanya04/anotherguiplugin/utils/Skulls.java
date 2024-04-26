package me.katanya04.anotherguiplugin.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Skulls {
    private static int MAX_SIZE;
    private static final LinkedHashMap<String, ItemStack> cache = new LinkedHashMap<String, ItemStack>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ItemStack> eldest) {
            return size() > MAX_SIZE;
        }
    };
    public static void initialize() {
        MAX_SIZE = AnotherGUIPlugin.getConfiguration().getInt("player-head-cache-settings.max-size-entries", 100);
        if (MAX_SIZE < 1) {
            AnotherGUIPlugin.getLog().log(Level.WARNING, "Invalid player head cache size, setting to default value (100)");
            MAX_SIZE = 100;
        }
    }
    public static ItemStack getSkullFromURL(String url) {
        if (!Utils.isValidURL(url))
            return new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        return getSkullFromBase64(Base64.getEncoder().encodeToString(toEncode.getBytes()));
    }
    public static ItemStack getSkullFromBase64(String base64EncodedString) {
        ItemStack head = cache.get(base64EncodedString);
        if (head == null)
            head = getSkullByTexture(base64EncodedString);
        cache.put(base64EncodedString, head);
        return head;
    }

    private static ItemStack getSkullByTexture(String b64) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Player");
        profile.getProperties().put("textures", new Property("textures", b64));
        ReflectionMethods.setProfileToMeta(meta, profile);
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack getPlayerHead(String playerName) {
        ItemStack head = cache.get(playerName);
        if (head != null)
            return head;
        head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        UUID uuid = Utils.getPlayerUUID(playerName);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (PlayerUUIDCache.isNotPremium(playerName)) {
            setProfile(head, meta, playerName);
            return head;
        }
        meta.setOwner(playerName);
        head.setItemMeta(meta);
        setTextureAndProfile(head, meta, uuid, playerName);
        cache.put(playerName, head);
        return head;
    }

    public static String getPlayerByHead(ItemStack head) {
        if (head == null || head.getType() != Material.SKULL_ITEM)
            return null;
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        String player = meta.getOwner();
        if (player != null && !player.isEmpty())
            return player;
        GameProfile gameProfile = ReflectionMethods.getProfileFromMeta(meta);
        return gameProfile == null ? null : gameProfile.getName();
    }

    private static void setProfile(ItemStack item, SkullMeta meta, String playerName) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), playerName);
        ReflectionMethods.setProfileToMeta(meta, profile);
        item.setItemMeta(meta);
    }

    private static void setTextureAndProfile(ItemStack item, SkullMeta meta, UUID uuid, String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(AnotherGUIPlugin.plugin, () -> {
            String texture, signature;
            URL url;
            try {
                url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString() + "?unsigned=false");
            } catch (MalformedURLException e) {
                AnotherGUIPlugin.getLog().log(Level.SEVERE, "URL exception while setting the texture to a player head", e);
                return;
            }
            try {
                InputStreamReader reader = new InputStreamReader(url.openStream());
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject().get("properties")
                        .getAsJsonArray().get(0).getAsJsonObject();
                texture = json.get("value").getAsString();
                signature = json.get("signature").getAsString();
            } catch (IOException e) {
                AnotherGUIPlugin.getLog().log(Level.SEVERE, "IOException while setting the texture to a player head", e);
                return;
            }
            Bukkit.getScheduler().runTask(AnotherGUIPlugin.plugin, () -> {
                GameProfile profile = new GameProfile(UUID.randomUUID(), playerName);
                profile.getProperties().put("textures", new Property("textures", texture, signature));
                ReflectionMethods.setProfileToMeta(meta, profile);
                item.setItemMeta(meta);
            });
        });
    }
    public static class PutOnCacheOnJoin implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            String playerName = e.getPlayer().getName();
            if (!Skulls.cache.containsKey(playerName))
                Skulls.getPlayerHead(playerName);
        }
    }
}
