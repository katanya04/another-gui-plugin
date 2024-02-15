package me.katanya04.anotherguiplugin.Utils;

import me.katanya04.anotherguiplugin.InventoryMenuPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ReflectionMethods {
    private ReflectionMethods() {}
    private static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException var3) {
            InventoryMenuPlugin.getLog().log(Level.SEVERE, "NMS class not found \"" + name + "\"");
            return null;
        }
    }

    private static Class<?> getBukkitClass(String name, String packet) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + packet + "." + name);
        } catch (ClassNotFoundException var3) {
            InventoryMenuPlugin.getLog().log(Level.SEVERE, "Bukkit class not found \"" + name + "\"");
            return null;
        }
    }
    private static Class<?> CraftPlayer;
    private static Method getHandle;
    private static Class<?> EntityPlayer;
    private static Class<?> CraftItemStack;
    private static Method asNMSCopy;
    private static Method openBook;
    private static Field pages;
    private static Method a;
    private static Class<?> NetItemStack;
    private static Class<?> EntityHuman;
    private static Class<?> Container;
    private static Field windowId;
    private static Class<?> PacketPlayOutSetSlot;
    private static Class<?> ItemStack;
    private static Constructor<?> PacketPlayOutSetSlotConstructor;
    private static Field playerConnection;
    private static Method sendPacket;
    public static void cacheObjects() throws NoSuchMethodException, NoSuchFieldException {
        CraftPlayer = getBukkitClass("CraftPlayer", "entity");
        getHandle = Objects.requireNonNull(CraftPlayer).getDeclaredMethod("getHandle");
        EntityPlayer = getNMSClass("EntityPlayer");
        EntityHuman = getNMSClass("EntityHuman");
        Container = getNMSClass("Container");
        windowId = Objects.requireNonNull(Container).getField("windowId");
        CraftItemStack = getBukkitClass("CraftItemStack", "inventory");
        asNMSCopy = Objects.requireNonNull(CraftItemStack).getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
        openBook = EntityPlayer.getMethod("openBook", NetItemStack);
        pages = Objects.requireNonNull(getBukkitClass("CraftMetaBook", "inventory")).getDeclaredField("pages");
        a = Objects.requireNonNull(getNMSClass("IChatBaseComponent$ChatSerializer")).getMethod("a", String.class);
        NetItemStack = getNMSClass("ItemStack");
        PacketPlayOutSetSlot = getNMSClass("PacketPlayOutSetSlot");
        ItemStack = getNMSClass("ItemStack");
        PacketPlayOutSetSlotConstructor = PacketPlayOutSetSlot.getConstructor(int.class, int.class, ItemStack);
        playerConnection = EntityPlayer.getField("playerConnection");
        sendPacket = Objects.requireNonNull(getNMSClass("PlayerConnection")).getMethod("sendPacket", getNMSClass("Packet"));
    }

    public static void openBook(Player p, ItemStack book) { //thx to Juancomaster1998 :)
        p.closeInventory();
        ItemStack hand = p.getItemInHand();
        p.setItemInHand(book);
        try {
            openBook.invoke(getHandle.invoke(CraftPlayer.cast(p)), asNMSCopy.invoke(null, book));
        } catch (Exception ex) {
            InventoryMenuPlugin.getLog().log(Level.SEVERE, "Error while trying to open book menu");
        } finally {
            p.setItemInHand(hand);
        }
    }

    @SuppressWarnings("unchecked")
    public static ItemStack getBook(List<TextComponent> pageTextComponents) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        List<List<TextComponent>> lines = Utils.getLines(pageTextComponents);
        List<TextComponent> page = new ArrayList<>();
        int index = 0;
        try {
            List<Object> bookPages = (List<Object>) pages.get(meta);
            for (List<TextComponent> line : lines) {
                if (index++ < 14)
                    page.addAll(line);
                else {
                    bookPages.add(a.invoke(null, ComponentSerializer.toString(page.toArray(new TextComponent[0]))));
                    page = new ArrayList<>(line);
                    index = 0;
                }
            }
            if (!page.isEmpty())
                bookPages.add(a.invoke(null, ComponentSerializer.toString(page.toArray(new TextComponent[0]))));
        } catch (Exception ex) {
            InventoryMenuPlugin.getLog().log(Level.SEVERE, "Error while trying to create a book menu");
        }
        book.setItemMeta(meta);
        return book;
    }

    public static void setContents(Player player, Inventory inv) {
        try {
            Object entityPlayer = getHandle.invoke(CraftPlayer.cast(player));
            Object container = EntityHuman.getField("activeContainer").get(entityPlayer);
            Object pConnection = playerConnection.get(entityPlayer);
            int slot = 0;
            for (org.bukkit.inventory.ItemStack item : inv.getContents()) {
                Object packet = PacketPlayOutSetSlotConstructor.newInstance(windowId.get(container), slot++, asNMSCopy.invoke(null, item));
                sendPacket.invoke(pConnection, packet);
            }
        } catch (Exception ex) {
            InventoryMenuPlugin.getLog().log(Level.SEVERE, "Error while trying to set the contents in a inventory menu");
        }
    }
}