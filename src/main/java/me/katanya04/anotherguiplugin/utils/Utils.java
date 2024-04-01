package me.katanya04.anotherguiplugin.utils;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A class containing static methods that serve different purposes
 */
public class Utils {
    private Utils() {}
    private static final Class<?> ItemStack;
    private static final Class<?> CraftItemStack;
    private static final Method asNMSCopy;
    private static final Class<?> NBTTagCompound;
    private static final Method hasTag;
    private static final Method getTag;
    private static final Constructor<?> NBTTagCompoundConstructor;
    private static final Method setString;
    private static final Method setTag;
    private static final Method asBukkitCopy;
    private static final Method hasKey;
    private static final Method get;
    private static final Class<?> NBTTagString;
    private static final Method asString;
    private static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException var3) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "NMS class not found \"" + name + "\"");
            return null;
        }
    }

    private static Class<?> getBukkitClass(String name, String packet) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + packet + "." + name);
        } catch (ClassNotFoundException var3) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Bukkit class not found \"" + name + "\"");
            return null;
        }
    }
    static {
        try {
            ItemStack = getNMSClass("ItemStack");
            CraftItemStack = getBukkitClass("CraftItemStack", "inventory");
            asNMSCopy = CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
            NBTTagCompound = getNMSClass("NBTTagCompound");
            hasTag = ItemStack.getMethod("hasTag");
            getTag = ItemStack.getMethod("getTag");
            NBTTagCompoundConstructor = NBTTagCompound.getConstructor();
            setString = NBTTagCompound.getMethod("setString", String.class, String.class);
            setTag = ItemStack.getMethod("setTag", NBTTagCompound);
            asBukkitCopy = CraftItemStack.getMethod("asBukkitCopy", ItemStack);
            hasKey = NBTTagCompound.getMethod("hasKey", String.class);
            get = NBTTagCompound.getMethod("get", String.class);
            NBTTagString = getNMSClass("NBTTagString");
            Method temp;
            try {
                temp = NBTTagString.getMethod("asString");
            } catch (NoSuchMethodException ex) {
                temp = NBTTagString.getMethod("a_");
            }
            asString = temp;
        } catch (Exception ex) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Reflection error: " + ex.getClass().getSimpleName());
            throw new RuntimeException();
        }
    }
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
        if (PlayerUUIDCache.isNotPremium(playerName))
            return PlayerUUIDCache.getUUIDLocal(playerName);
        CompletableFuture<UUID> uuid = PlayerUUIDCache.getUUIDMojang(playerName)
                .thenApply(id -> id != null ? id : PlayerUUIDCache.getUUIDLocal(playerName));
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
        try {
            Object NMSItemVersion = asNMSCopy.invoke(null, item);
            Object nbt = ((boolean) hasTag.invoke(NMSItemVersion)) ? getTag.invoke(NMSItemVersion) : NBTTagCompoundConstructor.newInstance();
            setString.invoke(nbt, key, value);
            setTag.invoke(NMSItemVersion, nbt);
            return (ItemStack) asBukkitCopy.invoke(null, NMSItemVersion);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getNBT(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR)
            return null;
        try {
            Object NMSItemVersion = asNMSCopy.invoke(null, item);
            if (!(boolean) hasTag.invoke(NMSItemVersion))
                return null;
            Object nbt = getTag.invoke(NMSItemVersion);
            if (!(boolean) hasKey.invoke(nbt, key))
                return null;
            Object value = get.invoke(nbt, key);
            return (String) asString.invoke(value);
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean containsNBT(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR)
            return false;
        try {
            Object NMSItemVersion = asNMSCopy.invoke(null, item);
            if (!(boolean) hasTag.invoke(NMSItemVersion))
                return false;
            Object nbt = getTag.invoke(NMSItemVersion);
            return (boolean) hasKey.invoke(nbt, key);
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

    public static Set<Integer> findSlots(Inventory inv, ItemStack itemStack, int amountOfItems) {
        Set<Integer> toret = new HashSet<>();
        int i = 0;
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                toret.add(i);
                amountOfItems -= itemStack.getMaxStackSize();
            } else if (itemStack.isSimilar(item) && item.getAmount() < item.getMaxStackSize()) {
                toret.add(i);
                amountOfItems -= item.getMaxStackSize() - item.getAmount();
            }
            if (amountOfItems < 1)
                break;
            i++;
        }
        return toret;
    }

    public static <T> boolean shareRepeatedValue(Collection<T> collection1, Collection<T> collection2) {
        HashSet<T> set = new HashSet<>(collection1);
        List<T> list = new LinkedList<>(collection2);
        list.retainAll(set);
        return !list.isEmpty();
    }

    public static void clearConfSection(ConfigurationSection confSection) {
        for (String key : confSection.getKeys(false))
            confSection.set(key, null);
    }
}