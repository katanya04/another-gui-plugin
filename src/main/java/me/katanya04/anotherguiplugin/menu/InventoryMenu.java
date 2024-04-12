package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import me.katanya04.anotherguiplugin.actionItems.ActionItem;
import me.katanya04.anotherguiplugin.utils.Callback;
import me.katanya04.anotherguiplugin.utils.ReflectionMethods;
import me.katanya04.anotherguiplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
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
    protected ItemStack[] contents;
    protected boolean canInteract;
    public enum SaveOption {NONE, GLOBAL, INDIVIDUAL};
    protected SaveOption saveChanges;
    protected Set<Integer> protectedSlots;
    protected Consumer<InventoryOpenEvent> onOpenBehaviour;
    protected Consumer<InventoryClickEvent> onClickBehaviour;
    protected Consumer<InventoryCloseEvent> onCloseBehaviour;
    protected List<Inventory> currentlyOpenCopies;
    protected Menu<?> parent; /*Menu to return after closing this menu*/
    public InventoryMenu(String GUIName, boolean canInteract, SaveOption saveChanges, InventoryType invType, ItemStack[] contents) {
        this.canInteract = canInteract;
        this.saveChanges = saveChanges;
        this.protectedSlots = new HashSet<>();
        this.currentlyOpenCopies = new ArrayList<>();
        this.invType = invType;
        this.contents = contents;
        this.GUIName = GUIName;
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

    public void setParent(Menu<?> parent) {
        this.parent = parent;
    }

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
                (contents = getSavedMenu(player, this.GUIName)) == null)
            contents = Arrays.copyOf(this.contents, this.contents.length);
        return parseActionItemsByPlayer(contents, player);
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
            int size = Utils.ceilToMultipleOfNine(contents.length);
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
        for (int i = 0; i < contents.length; i++)
            contents[0] = null;
    }

    @Override
    public void setContents(Inventory inv) {
        setContents(inv.getContents());
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    @Override
    public Inventory getContents() {
        return getInventory();
    }

    public ItemStack[] getContentsItemArray() {
        return contents;
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

    public SaveOption getSaveOption() {
        return saveChanges;
    }
    protected void save(Inventory contents, HumanEntity player, boolean saveToMemory) {
        switch (saveChanges) {
            case NONE:
                break;
            case GLOBAL:
                this.contents = contents.getContents();
                this.currentlyOpenCopies.stream().filter(o -> !contents.equals(o)).forEach(inv -> inv.setContents(contents.getContents()));
                break;
            case INDIVIDUAL:
                if (!saveToMemory)
                    break;
                AnotherGUIPlugin.getStorage().set("menu-saves." + GUIName + "." + Utils.getPlayerUUID(player.getName()), contents.getContents());
                AnotherGUIPlugin.getStorage().saveConfig();
        }
    }
    public static ItemStack[] getSavedMenu(Player player, String GUIName) {
        Object save = AnotherGUIPlugin.getStorage().get("menu-saves." + GUIName + "." + Utils.getPlayerUUID(player.getName()));
        return Utils.getCollectionOfItems(save);
    }
    public static void getSavedMenuAsyncCallback(Player player, String GUIName, Callback<ItemStack[]> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(AnotherGUIPlugin.plugin, () -> {
            ItemStack[] items = getSavedMenu(player, GUIName);
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
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getClickedInventory() != null && e.getInventory().getHolder() instanceof InventoryMenu) {
                InventoryMenu inv = ((InventoryMenu) e.getInventory().getHolder());
                if (((!inv.canInteract || inv.protectedSlots.contains(e.getSlot())) && e.getClickedInventory().equals(e.getInventory())) ||
                        (e.getClick().isShiftClick() && !e.getClickedInventory().equals(e.getInventory()) && (!inv.canInteract || Utils.shareRepeatedValue(
                                Utils.findSlots(e.getInventory(), e.getCurrentItem(), e.getCurrentItem().getAmount()), inv.protectedSlots)))
                ) {
                    e.setCancelled(true);
                } else //save contents must be done on next server tick
                    Bukkit.getScheduler().runTask(AnotherGUIPlugin.plugin,
                            () -> inv.save(e.getInventory(), e.getWhoClicked(), false));
                if (inv.onClickBehaviour != null)
                    inv.onClickBehaviour.accept(e);
            }
        }
        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory().getHolder() instanceof InventoryMenu) {
                InventoryMenu inv = ((InventoryMenu) e.getInventory().getHolder());
                if (inv.onCloseBehaviour != null)
                    inv.onCloseBehaviour.accept(e);
                inv.currentlyOpenCopies.remove(e.getInventory());
                inv.save(e.getInventory(), e.getPlayer(), true);
                if (inv.invType == InventoryType.ANVIL) {
                    e.getInventory().setItem(0, new ItemStack(Material.AIR));
                    e.getInventory().setItem(1, new ItemStack(Material.AIR));
                }
                if (inv.parent != null)
                    Menu.openMenuOneTickLater((Player) e.getPlayer(), inv.parent, false);
            }
        }
    }
}