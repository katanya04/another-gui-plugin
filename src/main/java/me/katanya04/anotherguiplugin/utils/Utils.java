package me.katanya04.anotherguiplugin.utils;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A class containing static methods that server different purposes
 */
public class Utils {
    private Utils() {}
    public static ItemStack setName(ItemStack item, String name) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        item.setItemMeta(itemMeta);
        return item;
    }
    public static ItemStack setLore(ItemStack item, String lore) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(Collections.singletonList(lore));
        item.setItemMeta(itemMeta);
        return item;
    }
    public static int ceilToMultipleOfNine(int n) {
        return n <= 9 ? 9 : ((n - 1) / 9 + 1) * 9;
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static <T> T[] mapToArray(Map<Integer, T> map, boolean padWithNull) {
        if (!padWithNull)
            return (T[]) map.values().stream().toArray(Object[]::new);
        else {
            int maxValue = getHighestKey(map.keySet());
            T[] toret = (T[]) new Object[maxValue];
            for (int i = 0; i < maxValue; i++)
                toret[i] = map.get(i);
            return toret;
        }
    }

    public static <T extends Comparable<T>> T getHighestKey(Set<T> keys) {
        return keys.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList()).get(0);
    }

    public static List<List<TextComponent>> getLines(List<TextComponent> text) { //Thx to Swedz :)
        //Note that the only flaw with using MinecraftFont is that it can't account for some UTF-8 symbols, it will throw an IllegalArgumentException
        final MinecraftFont font = new MinecraftFont();
        final int maxLineWidth = font.getWidth("LLLLLLLLLLLLLLLLLLL");
        //Get all of our lines
        List<List<TextComponent>> lines = new ArrayList<>();
        List<TextComponent> line = new ArrayList<>();
        try {
            for (TextComponent textComponent : text) {
                String rawLine = ChatColor.stripColor(line.stream().map(TextComponent::getText).reduce("", String::concat));
                rawLine += ChatColor.stripColor(textComponent.getText());
                if (font.getWidth(rawLine) > maxLineWidth) {
                    lines.add(line);
                    line = new ArrayList<>();
                }
                line.add(textComponent);
                if (textComponent.getText().endsWith("\n")) {
                    lines.add(line);
                    line = new ArrayList<>();
                }
            }
            if (!line.isEmpty())
                lines.add(line);
        } catch (IllegalArgumentException ex) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Illegal characters in book menu");
            lines.clear();
        }
        return lines;
    }

    public static UUID getPlayerUUID(String playerName) {
        UUID toret = PlayerUUIDCache.getUUIDFromCache(playerName);
        if (toret != null)
            return toret;
        CompletableFuture<UUID> uuid = PlayerUUIDCache.getUUIDMojang(playerName).thenApply(id -> {
            if (id != null) {
                PlayerUUIDCache.addToCache(playerName, toret);
                return id;
            }
            else
                return PlayerUUIDCache.getUUIDLocal(playerName);
        });
        try {
            return uuid.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static UUID UUIDFromUnformattedString(String uuid) {
        return UUID.fromString(String.format("%s-%s-%s-%s-%s", uuid.substring(0,8), uuid.substring(8,12), uuid.substring(12,16), uuid.substring(16,20), uuid.substring(20,32)));
    }

    public static ItemStack setItemNBT(ItemStack item, String key, String value) {
        ItemStack craftItemStack = MinecraftReflection.getBukkitItemStack(item);
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(craftItemStack));
        compound.put(key, value);
        NbtFactory.setItemTag(craftItemStack, compound);
        return craftItemStack;
    }

    public static ItemStack setItemNBT(ItemStack item, Map<String, String> map) {
        ItemStack craftItemStack = MinecraftReflection.getBukkitItemStack(item);
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(craftItemStack));
        for (Map.Entry<String, String> entry : map.entrySet())
            compound.put(entry.getKey(), entry.getValue());
        NbtFactory.setItemTag(craftItemStack, compound);
        return craftItemStack;
    }

    public static String getNBT(ItemStack itemStack, String key) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;
        ItemStack craftItemStack = MinecraftReflection.getBukkitItemStack(itemStack);
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(craftItemStack));
        try {
            return compound.getString(key);
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean containsNBT(ItemStack itemStack, String key) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return false;
        ItemStack craftItemStack = MinecraftReflection.getBukkitItemStack(itemStack);
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(craftItemStack));
        try {
            return compound.containsKey(key);
        } catch (Exception ex) {
            return false;
        }
    }

    public static ItemStack[] getCollectionOfItems(Object obj) {
        try {
            if (obj instanceof Collection)
                return ((Collection<?>) obj).stream().map(o -> (ItemStack) o).toArray(ItemStack[]::new);
            if (obj instanceof ItemStack[])
                return (ItemStack[]) obj;
        } catch (ClassCastException ignored) {}
        return null;
    }
}