package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import me.katanya04.anotherguiplugin.actionItems.ActionItem;
import me.katanya04.anotherguiplugin.events.AfterUpdateOpenMenuEvent;
import me.katanya04.anotherguiplugin.events.BeforeUpdateOpenMenuEvent;
import me.katanya04.anotherguiplugin.utils.Callback;
import me.katanya04.anotherguiplugin.utils.ReflectionMethods;
import me.katanya04.anotherguiplugin.utils.Utils;
import me.katanya04.anotherguiplugin.yaml.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Menu which contents are displayed in an {@link Inventory}
 */
public class InventoryMenu implements Menu<Inventory>, InventoryHolder {
    protected static final int MAX_CHEST_SIZE = 54;
    protected static final List<InventoryType> UNIMPLEMENTED_INVENTORIES = Collections.unmodifiableList(Arrays.asList(InventoryType.DROPPER,
            InventoryType.CREATIVE, InventoryType.MERCHANT, InventoryType.ENDER_CHEST, InventoryType.BEACON, InventoryType.ANVIL));
    protected String GUIName;
    protected InventoryType invType;
    protected Function<Player, ItemStack[]> contents;
    protected boolean canInteract;
    public enum SaveOption {NONE, GLOBAL, INDIVIDUAL};
    protected SaveOption saveChanges;
    protected Set<Integer> protectedSlots;
    protected Consumer<InventoryOpenEvent> onOpenBehaviour;
    protected Consumer<InventoryClickEvent> onClickBehaviour;
    protected Consumer<InventoryCloseEvent> onCloseBehaviour;
    protected Consumer<InventoryDragEvent> onDragBehaviour;
    protected Consumer<BeforeUpdateOpenMenuEvent> beforeUpdateContents;
    protected Consumer<AfterUpdateOpenMenuEvent> afterUpdateContents;
    protected boolean retrieveItemOnCursorOnClose;
    protected List<Inventory> currentlyOpenCopies;
    protected Menu<?> parent; /*Menu to return after closing this menu*/
    protected YamlFile saveFile;
    public boolean parseActionItems;
    public InventoryMenu(String GUIName, boolean canInteract, SaveOption saveChanges, InventoryType invType, ItemStack[] contents) {
        this(GUIName, canInteract, saveChanges, invType, ignored -> contents);
    }
    public InventoryMenu(String GUIName, boolean canInteract, SaveOption saveChanges, InventoryType invType, Function<Player, ItemStack[]> contents) {
        this.canInteract = canInteract;
        this.saveChanges = saveChanges;
        this.protectedSlots = new HashSet<>();
        this.currentlyOpenCopies = new ArrayList<>();
        this.invType = invType;
        this.contents = contents;
        this.GUIName = GUIName;
        this.saveFile = AnotherGUIPlugin.getStorage();
        this.parseActionItems = true;
    }

    public static ItemStack[] parseActionItemsByPlayer(ItemStack[] contents, Player player) {
        for (int i = 0; i < contents.length; i++) {
            if (ActionItem.isActionItem(contents[i]))
                contents[i] = ActionItem.getActionItem(contents[i]).toItemStack(player);
        }
        return contents;
    }

    public String getGUIName() {
        return GUIName;
    }

    public void setGUIName(String GUIName) {
        this.GUIName = GUIName;
    }

    @Override
    public void setParent(Menu<?> parent) {
        this.parent = parent;
    }

    @Override
    public Menu<?> getParent() {
        return parent;
    }

    public static InventoryMenu getAnotherPlayersMenu(HumanEntity player) {
        Inventory openInv = player.getOpenInventory().getTopInventory();
        if (openInv == null)
            return null;
        InventoryHolder inv = openInv.getHolder();
        if (inv instanceof InventoryMenu)
            return (InventoryMenu) inv;
        else
            return null;
    }

    public List<Inventory> getCurrentlyOpenCopies() {
        return currentlyOpenCopies;
    }

    protected ItemStack[] getItemContents(Player player) {
        ItemStack[] contents;
        if (player == null || this.saveChanges != SaveOption.INDIVIDUAL ||
                (contents = getSavedMenu(player)) == null)
            contents = this.contents.apply(player);
        return this.parseActionItems ? parseActionItemsByPlayer(contents, player) : contents;
    }

    @Override
    public void openMenu(Player player) {
        Inventory inv = newInventory(player);
        openInventory(player, inv);
        if (invType != InventoryType.CHEST)
            ReflectionMethods.setContents(player, inv);
        this.currentlyOpenCopies.add(inv);
    }

    protected Inventory newInventory(Player player) {
        Inventory toret;
        if (invType == InventoryType.CHEST) {
            int size = Utils.ceilToMultipleOfNine(this.contents.apply(player).length);
            toret = Bukkit.createInventory(this, Math.min(size, MAX_CHEST_SIZE), GUIName);
        } else
            toret = Bukkit.createInventory(this, invType, GUIName);
        toret.setContents(getItemContents(player));
        return toret;
    }

    protected void openInventory(Player player, Inventory inv) {
        if (!UNIMPLEMENTED_INVENTORIES.contains(invType))
            player.openInventory(inv);
        else
            switch (inv.getType()) {
                case ANVIL:
                    ReflectionMethods.openAnvilInventory(player, inv, this);
                    break;
            }
    }

    public void updateContents() {
        currentlyOpenCopies.forEach(o -> o.setContents(getItemContents((Player) (o.getViewers().get(0)))));
    }

    @Override
    public void clear() {
        this.contents = ignored -> new ItemStack[0];
    }

    @Override
    public void setContents(Inventory inv) {
        setContents(inv.getContents());
    }

    public void setContents(ItemStack[] contents) {
        setContents(ignored -> contents);
    }

    public void setContents(Function<Player, ItemStack[]> contents) {
        this.contents = contents;
    }

    @Override
    public Inventory getContents() {
        return getInventory();
    }

    public ItemStack[] getContentsItemArray() {
        return contents.apply(null);
    }

    public ItemStack[] getContentsItemArray(Player player) {
        return contents.apply(player);
    }

    @Override
    public Inventory getInventory() {
        return newInventory(null);
    }

    public void setOnOpenBehaviour(Consumer<InventoryOpenEvent> onOpenBehaviour) {
        this.onOpenBehaviour = onOpenBehaviour;
    }

    public void setOnCloseBehaviour(Consumer<InventoryCloseEvent> onCloseBehaviour) {
        this.onCloseBehaviour = onCloseBehaviour;
    }

    public void setOnClickBehaviour(Consumer<InventoryClickEvent> onClickBehaviour) {
        this.onClickBehaviour = onClickBehaviour;
    }

    public void setOnDragBehaviour(Consumer<InventoryDragEvent> onDragBehaviour) {
        this.onDragBehaviour = onDragBehaviour;
    }

    public void setBeforeUpdateContents(Consumer<BeforeUpdateOpenMenuEvent> beforeUpdateContents) {
        this.beforeUpdateContents = beforeUpdateContents;
    }

    public void setAfterUpdateContents(Consumer<AfterUpdateOpenMenuEvent> afterUpdateContents) {
        this.afterUpdateContents = afterUpdateContents;
    }

    public void setRetrieveItemOnCursorOnClose(boolean retrieveItemOnCursorOnClose) {
        this.retrieveItemOnCursorOnClose = retrieveItemOnCursorOnClose;
    }

    public Set<Integer> getProtectedSlots() {
        return protectedSlots;
    }

    public void setProtectedSlots(Set<Integer> protectedSlots) {
        this.protectedSlots = protectedSlots;
    }

    public void addProtectedSlots(int... slots) {
        this.protectedSlots.addAll(Arrays.stream(slots).boxed().collect(Collectors.toSet()));
    }

    public boolean canInteract() {
        return canInteract;
    }

    public void setSaveFile(YamlFile saveFile) {
        this.saveFile = saveFile;
    }

    public ConfigurationSection getSaveFile() {
        return saveFile;
    }

    public SaveOption getSaveOption() {
        return saveChanges;
    }
    protected void save(Inventory contents, Player player, boolean saveToDisk) {
        switch (saveChanges) {
            case NONE:
                break;
            case GLOBAL:
                globalSave(contents, player);
                break;
            case INDIVIDUAL:
                if (saveToDisk)
                    saveToFile(contents, player);
        }
    }
    protected void globalSave(Inventory contents, Player player) {
        this.contents = ignored -> contents.getContents();
        updateOpenMenus(contents, player);
    }
    public void updateOpenMenus(Inventory contents, Player player) {
        this.currentlyOpenCopies.stream().filter(o -> !contents.equals(o)).forEach(inv -> {
            BeforeUpdateOpenMenuEvent event = new BeforeUpdateOpenMenuEvent(this, inv, contents.getContents());
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                inv.setContents(contents.getContents());
            Bukkit.getPluginManager().callEvent(new AfterUpdateOpenMenuEvent(this, inv));
        });
    }
    public void updateOpenMenus() {
        this.currentlyOpenCopies.forEach(inv -> {
            ItemStack[] newContents = contents.apply((Player) inv.getViewers().get(0));
            BeforeUpdateOpenMenuEvent event = new BeforeUpdateOpenMenuEvent(this, inv, newContents);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                inv.setContents(newContents);
            Bukkit.getPluginManager().callEvent(new AfterUpdateOpenMenuEvent(this, inv));
        });
    }
    protected void saveToFile(Inventory contents, Player player) {
        this.saveFile.set("menu-saves." + GUIName + "." + Utils.getPlayerUUID(player.getName()), contents.getContents().clone());
        this.saveFile.saveConfig();
    }
    public ItemStack[] getSavedMenu(Player player) {
        Object save = this.saveFile.get("menu-saves." + this.GUIName + "." + Utils.getPlayerUUID(player.getName()));
        return Utils.getCollectionOfItems(save);
    }
    @Deprecated
    public static ItemStack[] getSavedMenu(Player player, YamlFile saveFile, String GUIName) {
        if (saveFile == null)
            saveFile = AnotherGUIPlugin.getStorage();
        Object save = saveFile.get("menu-saves." + GUIName + "." + Utils.getPlayerUUID(player.getName()));
        return Utils.getCollectionOfItems(save);
    }
    public void resetPlayerSave(String playerName) {
        this.saveFile.set("menu-saves." + GUIName + "." + Utils.getPlayerUUID(playerName), null);
        this.saveFile.saveConfig();
    }
    @Deprecated
    public static void resetPlayerSave(String playerName, YamlFile saveFile, String GUIName) {
        if (saveFile == null)
            saveFile = AnotherGUIPlugin.getStorage();
        saveFile.set("menu-saves." + GUIName + "." + Utils.getPlayerUUID(playerName), null);
        saveFile.saveConfig();
    }
    public void resetSavedContents() {
        this.saveFile.set("menu-saves." + GUIName, null);
        this.saveFile.saveConfig();
    }
    @Deprecated
    public static void resetSavedContents(YamlFile saveFile, String GUIName) {
        if (saveFile == null)
            saveFile = AnotherGUIPlugin.getStorage();
        saveFile.set("menu-saves." + GUIName, null);
        saveFile.saveConfig();
    }
    public void getSavedMenuAsyncCallback(Player player, Callback<ItemStack[]> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(AnotherGUIPlugin.plugin, () -> {
            ItemStack[] items = getSavedMenu(player);
            Bukkit.getScheduler().runTask(AnotherGUIPlugin.plugin, () -> callback.onQueryDone(items));
        });
    }
    @Deprecated
    public static void getSavedMenuAsyncCallback(Player player, YamlFile saveFile, String GUIName, Callback<ItemStack[]> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(AnotherGUIPlugin.plugin, () -> {
            ItemStack[] items = getSavedMenu(player, saveFile, GUIName);
            Bukkit.getScheduler().runTask(AnotherGUIPlugin.plugin, () -> callback.onQueryDone(items));
        });
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onInventoryOpen(InventoryOpenEvent e) {
            if (e.getInventory().getHolder() instanceof InventoryMenu) {
                InventoryMenu inv = ((InventoryMenu) e.getInventory().getHolder());
                if (inv.onOpenBehaviour != null)
                    inv.onOpenBehaviour.accept(e);
            }
        }
        @EventHandler
        public void onInventoryDrag(InventoryDragEvent e) {
            if (e.getInventory().getHolder() instanceof InventoryMenu) {
                InventoryMenu inv = ((InventoryMenu) e.getInventory().getHolder());
                if (!inv.canInteract || !Collections.disjoint(inv.protectedSlots, e.getRawSlots()))
                    e.setCancelled(true);
                if (inv.onDragBehaviour != null)
                    inv.onDragBehaviour.accept(e);
            }
        }
        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getInventory() != null && e.getInventory().getHolder() instanceof InventoryMenu) {
                InventoryMenu inv = ((InventoryMenu) e.getInventory().getHolder());
                if (((!inv.canInteract || inv.protectedSlots.contains(e.getSlot())) && Objects.equals(e.getClickedInventory(), e.getInventory())) ||
                        (e.getClick().isShiftClick() && !Objects.equals(e.getClickedInventory(), e.getInventory()) && (!inv.canInteract || Utils.shareRepeatedValue(
                                Utils.findSlots(e.getInventory(), e.getCurrentItem(), e.getCurrentItem().getAmount()), inv.protectedSlots)))
                ) {
                    e.setCancelled(true);
                } else //save contents must be done on next server tick
                    Bukkit.getScheduler().runTask(AnotherGUIPlugin.plugin,
                            () -> inv.save(e.getInventory(), (Player) e.getWhoClicked(), false));
                if (inv.onClickBehaviour != null)
                    inv.onClickBehaviour.accept(e);
            }
        }
        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory().getHolder() instanceof InventoryMenu) {
                InventoryMenu inv = ((InventoryMenu) e.getInventory().getHolder());
                if (inv.retrieveItemOnCursorOnClose)
                    Utils.retrieveItemOnCursorOnClose(e);
                if (inv.onCloseBehaviour != null)
                    inv.onCloseBehaviour.accept(e);
                inv.currentlyOpenCopies.remove(e.getInventory());
                inv.save(e.getInventory(), (Player) e.getPlayer(), true);
                if (inv.invType == InventoryType.ANVIL) {
                    e.getInventory().setItem(0, new ItemStack(Material.AIR));
                    e.getInventory().setItem(1, new ItemStack(Material.AIR));
                }
                if (inv.parent != null)
                    Menu.openMenuOneTickLater((Player) e.getPlayer(), inv.parent, false);
            }
        }
        @EventHandler
        public void beforeUpdateContents(BeforeUpdateOpenMenuEvent e) {
            InventoryMenu inventoryMenu = e.getFrom();
            if (inventoryMenu.beforeUpdateContents != null)
                inventoryMenu.beforeUpdateContents.accept(e);
        }
        @EventHandler
        public void afterUpdateContents(AfterUpdateOpenMenuEvent e) {
            InventoryMenu inventoryMenu = e.getFrom();
            if (inventoryMenu.afterUpdateContents != null)
                inventoryMenu.afterUpdateContents.accept(e);
        }
    }
}