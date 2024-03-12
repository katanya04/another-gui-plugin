package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import me.katanya04.anotherguiplugin.actionItems.ActionItem;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A Menu which contents are displayed in an {@link Inventory}
 */
public class InventoryMenu implements Menu<Inventory>, InventoryHolder {
    protected static final int MAX_CHEST_SIZE = 54;
    protected static final List<InventoryType> UNIMPLEMENTED_INVENTORIES = Collections.unmodifiableList(Arrays.asList(InventoryType.DROPPER,
            InventoryType.CREATIVE, InventoryType.MERCHANT, InventoryType.ENDER_CHEST, InventoryType.BEACON, InventoryType.ANVIL));
    protected Function<Object, ItemStack[]> createContentsFn;
    protected final InventoryType invType;
    protected String name;
    protected boolean canInteract;
    public enum SaveOption {NONE, GLOBAL, INDIVIDUAL};
    protected SaveOption saveChanges;
    protected Set<Integer> protectedSlots;
    protected Consumer<InventoryClickEvent> onClickBehaviour;
    protected Consumer<InventoryCloseEvent> onCloseBehaviour;
    protected List<Inventory> currentlyOpenCopies;
    public static final String ACTION_ITEM_FROM_MEMORY = "ActionItemFromMemory";
    public InventoryMenu(String name, boolean canInteract, SaveOption saveChanges, InventoryType invType, ItemStack[] contents) {
        this(name, canInteract, saveChanges, invType, pl -> contents);
    }
    public InventoryMenu(String name, boolean canInteract, SaveOption saveChanges, InventoryType invType, Function<Object, ItemStack[]> createContentsFn) {
        this.name = name;
        this.canInteract = canInteract;
        this.saveChanges = saveChanges;
        this.invType = invType;
        this.createContentsFn = createContentsFn;
        this.protectedSlots = new HashSet<>();
        this.currentlyOpenCopies = new ArrayList<>();
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

    public List<Inventory> getCurrentlyViewedInventory(HumanEntity player) {
        return getCurrentlyViewedInventory(inv -> inv.getViewers().contains(player));
    }

    public List<Inventory> getCurrentlyViewedInventory(Function<Inventory, Boolean> function) {
        return currentlyOpenCopies.stream().filter(function::apply).collect(Collectors.toList());
    }

    public ItemStack[] getItemContents() {
        return getItemContents(null, null);
    }

    public ItemStack[] getItemContents(Player player, Object arg) {
        ItemStack[] contents;
        int existingActionItems = ActionItem.numCreatedItems();
        if (player == null || this.saveChanges != SaveOption.INDIVIDUAL ||
                (contents = getSavedMenu(Utils.getPlayerUUID(player.getName()))) == null)
            contents = this.createContentsFn.apply(arg);
        if (ActionItem.numCreatedItems() > existingActionItems)
            AnotherGUIPlugin.getLog().log(Level.WARNING, "You shouldn't create new ActionItems every time an inventory is opened, instead use references to already existing ones");
        return contents;
    }

    @Override
    public void openMenu(Player player) {
        openMenu(player, null);
    }
    public void openMenu(Player player, Object arg) {
        Inventory inv = newInventory(this.invType, this.name, getItemContents(player, arg));
        openInventory(player, inv);
        if (this.invType != InventoryType.CHEST)
            ReflectionMethods.setContents(player, inv);
        currentlyOpenCopies.add(inv);
    }

    public void updateContents(Object arg) {
        currentlyOpenCopies.forEach(o -> o.setContents(getItemContents((Player) (o.getViewers().get(0)), arg)));
    }

    protected void openInventory(Player player, Inventory inv) {
        if (!UNIMPLEMENTED_INVENTORIES.contains(this.invType))
            player.openInventory(inv);
        else
            switch (inv.getType()) {
                case ANVIL:
                    ReflectionMethods.openAnvilInventory(player, inv, this);
                    break;
            }
    }

    @Override
    public void clear() {
        this.createContentsFn = pl -> new ItemStack[0];
    }

    public String getName() {
        return name;
    }

    @Override
    public void setContents(Inventory inv) {
        this.createContentsFn = pl -> inv.getContents();
    }

    @Override
    public Inventory getContents() {
        return getInventory();
    }

    @Override
    public Inventory getInventory() {
        return newInventory(this.invType, this.name, getItemContents());
    }

    protected Inventory newInventory(InventoryType invType, String name, ItemStack[] contents) {
        Inventory inv;
        if (invType == InventoryType.CHEST) {
            int size = Utils.ceilToMultipleOfNine(contents.length);
            inv = Bukkit.createInventory(this, Math.min(size, MAX_CHEST_SIZE), name);
        } else
            inv = Bukkit.createInventory(this, invType, name);
        inv.setContents(contents);
        return inv;
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
                this.createContentsFn = pl -> contents.getContents();
                this.currentlyOpenCopies.stream().filter(o -> !contents.equals(o)).forEach(inv -> inv.setContents(contents.getContents()));
                break;
            case INDIVIDUAL:
                if (!saveToMemory)
                    break;
                Arrays.stream(contents.getContents()).filter(ActionItem::isActionItem).forEach(o ->
                        Utils.setItemNBT(o, ACTION_ITEM_FROM_MEMORY, "true"));
                AnotherGUIPlugin.getStorage().set("menu-saves." + name + "." + Utils.getPlayerUUID(player.getName()), contents.getContents());
                AnotherGUIPlugin.getStorage().saveConfig();
        }
    }
    protected ItemStack[] getSavedMenu(UUID uuid) {
        Object save = AnotherGUIPlugin.getStorage().get("menu-saves." + name + "." + uuid);
        if (save instanceof List)
            return ((List<?>)save).stream().map(o -> (ItemStack) o).toArray(ItemStack[]::new);
        if (save instanceof ItemStack[])
            return (ItemStack[]) save;
        return null;
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getClickedInventory() != null && e.getInventory().getHolder() instanceof InventoryMenu) {
                InventoryMenu inv = ((InventoryMenu) e.getInventory().getHolder());
                if (!inv.canInteract || inv.protectedSlots.contains(e.getSlot()))
                    e.setCancelled(true);
                else //save contents must be done on next server tick
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
                inv.currentlyOpenCopies.remove(e.getInventory());
                if (inv.onCloseBehaviour != null)
                    inv.onCloseBehaviour.accept(e);
                if (inv.invType == InventoryType.ANVIL)
                    e.getInventory().setItem(0, new ItemStack(Material.AIR));
                Bukkit.getScheduler().runTask(AnotherGUIPlugin.plugin,
                        () -> inv.save(e.getInventory(), e.getPlayer(), true));
            }
        }
    }
}