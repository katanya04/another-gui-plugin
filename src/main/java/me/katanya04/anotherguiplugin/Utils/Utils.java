package me.katanya04.anotherguiplugin.Utils;

import me.katanya04.anotherguiplugin.InventoryMenuPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
        try {
            List<TextComponent> line = new ArrayList<>();
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
        } catch (IllegalArgumentException ex) {
            InventoryMenuPlugin.getLog().log(Level.SEVERE, "Illegal characters in book menu");
            lines.clear();
        }
        return lines;
    }
}
