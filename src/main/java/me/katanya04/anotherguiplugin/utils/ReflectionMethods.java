package me.katanya04.anotherguiplugin.utils;

import com.mojang.authlib.GameProfile;
import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

/**
 * A class containing static methods that work with CraftBukkit and NMS classes and objects through reflection
 */
public class ReflectionMethods {
    private ReflectionMethods() {}
    private static final String version;
    static {
        String[] versionSplit = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        version = versionSplit[versionSplit.length - 1];
    }
    private static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException var3) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "NMS class not found \"" + name + "\"");
            return null;
        }
    }

    private static Class<?> getBukkitClass(String name, String packet) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + packet + "." + name);
        } catch (ClassNotFoundException var3) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Bukkit class not found \"" + name + "\"");
            return null;
        }
    }

    private static void setField(Object change, String name, Object to) {
        try {
            Field field = change.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(change, to);
            field.setAccessible(false);
        } catch (Exception e) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Error while changing the value of the field " + name + " in " + change.getClass(), e);
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
    private static Class<?> ContainerAnvil;
    private static Constructor<?> ContainerAnvilConstructor;
    private static Field resultSlot;
    private static Method getOwner;
    private static Field anvilSubcontainer;
    private static Field bukkitOwner;
    private static Field inventory;
    private static Field world;
    private static Constructor<?> BlockPositionConstructor;
    private static Field checkReachable;
    private static Method nextContainerCounter;
    private static Class<?> IChatBaseComponent;
    private static Class<?> IChatBaseComponent$ChatSerializer;
    private static Method a1;
    private static Constructor<?> PacketPlayOutOpenWindowConstructor;
    private static Field activeContainer;
    private static Method addSlotListener;
    private static Method getBukkitView;
    private static Class<?> CraftMetaSkull;
    private static Field profile;
    private static Class<?> NBTTagCompound;
    private static Method hasTag;
    private static Method getTag;
    private static Constructor<?> NBTTagCompoundConstructor;
    private static Method setString;
    private static Method setTag;
    private static Method asBukkitCopy;
    private static Method hasKey;
    private static Method get;
    private static Class<?> NBTTagString;
    private static Method asString;

    public static void cacheObjects() throws NoSuchMethodException, NoSuchFieldException {
        CraftPlayer = getBukkitClass("CraftPlayer", "entity");
        getHandle = Objects.requireNonNull(CraftPlayer).getDeclaredMethod("getHandle");
        EntityPlayer = getNMSClass("EntityPlayer");
        EntityHuman = getNMSClass("EntityHuman");
        Container = getNMSClass("Container");
        windowId = Objects.requireNonNull(Container).getField("windowId");
        CraftItemStack = getBukkitClass("CraftItemStack", "inventory");
        asNMSCopy = Objects.requireNonNull(CraftItemStack).getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
        NetItemStack = getNMSClass("ItemStack");
        openBook = EntityPlayer.getMethod("openBook", NetItemStack);
        pages = Objects.requireNonNull(getBukkitClass("CraftMetaBook", "inventory")).getDeclaredField("pages");
        a = Objects.requireNonNull(getNMSClass("IChatBaseComponent$ChatSerializer")).getMethod("a", String.class);
        PacketPlayOutSetSlot = getNMSClass("PacketPlayOutSetSlot");
        ItemStack = getNMSClass("ItemStack");
        PacketPlayOutSetSlotConstructor = PacketPlayOutSetSlot.getConstructor(int.class, int.class, ItemStack);
        playerConnection = EntityPlayer.getField("playerConnection");
        sendPacket = Objects.requireNonNull(getNMSClass("PlayerConnection")).getMethod("sendPacket", getNMSClass("Packet"));
        ContainerAnvil = getNMSClass("ContainerAnvil");
        ContainerAnvilConstructor = Objects.requireNonNull(ContainerAnvil).getConstructor(getNMSClass("PlayerInventory"),
                getNMSClass("World"), getNMSClass("BlockPosition"), EntityHuman);
        resultSlot = ContainerAnvil.getDeclaredField("g");
        resultSlot.setAccessible(true);
        getOwner = Objects.requireNonNull(getNMSClass("InventoryCraftResult")).getMethod("getOwner");
        anvilSubcontainer = ContainerAnvil.getDeclaredField("h");
        anvilSubcontainer.setAccessible(true);
        bukkitOwner = Objects.requireNonNull(getNMSClass("InventorySubcontainer")).getDeclaredField("bukkitOwner");
        bukkitOwner.setAccessible(true);
        inventory = EntityHuman.getField("inventory");
        world = EntityHuman.getField("world");
        BlockPositionConstructor = Objects.requireNonNull(getNMSClass("BlockPosition")).getConstructor(int.class, int.class, int.class);
        checkReachable = Objects.requireNonNull(ContainerAnvil).getField("checkReachable");
        nextContainerCounter = EntityPlayer.getMethod("nextContainerCounter");
        IChatBaseComponent = getNMSClass("IChatBaseComponent");
        IChatBaseComponent$ChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
        a1 = Objects.requireNonNull(IChatBaseComponent$ChatSerializer).getMethod("a", String.class);
        PacketPlayOutOpenWindowConstructor = Objects.requireNonNull(getNMSClass("PacketPlayOutOpenWindow")).
                getConstructor(int.class, String.class, IChatBaseComponent, int.class);
        activeContainer = EntityPlayer.getField("activeContainer");
        addSlotListener = Container.getMethod("addSlotListener", getNMSClass("ICrafting"));
        getBukkitView = ContainerAnvil.getMethod("getBukkitView");
        CraftMetaSkull = getBukkitClass("CraftMetaSkull", "inventory");
        profile = CraftMetaSkull.getDeclaredField("profile");
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
    }

    public static void openBook(Player p, ItemStack book) { //thx to Juancomaster1998 :)
        p.closeInventory();
        ItemStack hand = p.getItemInHand();
        p.setItemInHand(book);
        try {
            openBook.invoke(getHandle.invoke(CraftPlayer.cast(p)), asNMSCopy.invoke(null, book));
        } catch (Exception ex) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Error while trying to open book menu");
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
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Error while trying to create a book menu");
        }
        book.setItemMeta(meta);
        return book;
    }

    public static void setContents(Player player, Inventory inv) {
        try {
            Object entityPlayer = getHandle.invoke(CraftPlayer.cast(player));
            Object container = activeContainer.get(entityPlayer);
            Object pConnection = playerConnection.get(entityPlayer);
            int slot = 0;
            for (org.bukkit.inventory.ItemStack item : inv.getContents()) {
                Object packet = PacketPlayOutSetSlotConstructor.newInstance(windowId.get(container), slot++,
                        asNMSCopy.invoke(null, item));
                sendPacket.invoke(pConnection, packet);
            }
        } catch (Exception ex) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Error while trying to set the contents in a inventory menu");
        }
    }

    public static void openAnvilInventory(Player player, Inventory inv, InventoryHolder invHolder) {
        if (inv.getType() != InventoryType.ANVIL)
            return;
        try {
            Object entityPlayer = getHandle.invoke(CraftPlayer.cast(player));

            Object anvil = ContainerAnvilConstructor.newInstance(inventory.get(entityPlayer), world.get(entityPlayer),
                    BlockPositionConstructor.newInstance(0, 0, 0), entityPlayer);
            checkReachable.setBoolean(anvil, false);
            bukkitOwner.set(anvilSubcontainer.get(anvil), invHolder);

            Object containerID = nextContainerCounter.invoke(entityPlayer);
            Object packet = PacketPlayOutOpenWindowConstructor.newInstance(containerID, "minecraft:anvil", a1.invoke(null, inv.getName()), 0);
            sendPacket.invoke(playerConnection.get(entityPlayer), packet);
            activeContainer.set(entityPlayer, anvil);
            Object playerContainer = activeContainer.get(entityPlayer);
            windowId.set(playerContainer, containerID);
            addSlotListener.invoke(playerContainer, entityPlayer);
            Inventory newInv = ((InventoryView) (getBukkitView.invoke(anvil))).getTopInventory();
            newInv.setItem(0, inv.getItem(0));
            newInv.setItem(1, inv.getItem(1));
        } catch (Exception ex) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Error while trying to open an anvil menu");
            ex.printStackTrace();
        }
    }

    public static void setProfileToMeta(SkullMeta meta, GameProfile profile) {
        try {
            setField(meta, "profile", profile);
        } catch (Exception e) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Exception while getting player head with texture", e);
        }
    }

    public static GameProfile getProfileFromMeta(SkullMeta meta) {
        Object craftMeta = CraftMetaSkull.cast(meta);
        profile.setAccessible(true);
        GameProfile profileToret = null;
        try {
            profileToret = (GameProfile) profile.get(craftMeta);
        } catch (Exception e) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Exception while getting GameProfile from SkullMeta", e);
        }
        profile.setAccessible(false);
        return profileToret;
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
}