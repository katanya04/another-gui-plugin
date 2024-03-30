package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import me.katanya04.anotherguiplugin.utils.Utils;
import me.katanya04.anotherguiplugin.actionItems.ActionItem;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * An InventoryMenu that supports scrolling between pages. Similar behaviour could be manually added to the InventoryMenu
 * class, but this one already provides it.
 */
public class ChestMenu extends InventoryMenu {
    protected static final ItemStack BARRIER = Utils.setName(new ItemStack(Material.BARRIER), " ");
    protected static final ActionItem nextPage = new ActionItem(Utils.setName(new ItemStack(Material.ARROW), "Next Page"),
            event -> {
                ChestMenu currentChestMenu = (ChestMenu) event.getInv().getHolder();
                if (currentChestMenu == null || currentChestMenu.nextPageMenu == null)
                    return;
                Menu.openMenuOneTickLater(event.getPlayer(), currentChestMenu.nextPageMenu, true);
            }, "nextPage");
    protected static final ActionItem previousPage = new ActionItem(Utils.setName(new ItemStack(Material.ARROW), "Previous Page"),
            event -> {
                ChestMenu currentChestMenu = (ChestMenu) event.getInv().getHolder();
                if (currentChestMenu == null || currentChestMenu.previousPageMenu == null)
                    return;
                Menu.openMenuOneTickLater(event.getPlayer(), currentChestMenu.previousPageMenu, true);
            }, "previousPage");
    protected InventoryMenu nextPageMenu;
    protected InventoryMenu previousPageMenu;
    private int numItems;
    private boolean fillWithBarriers;
    public ChestMenu(String name, ItemStack[] contents) {
        this(name, contents, false, SaveOption.NONE, null, null);
    }
    public ChestMenu(String name, ItemStack[] contents, boolean canInteract, SaveOption saveOption, InventoryMenu previousPage, InventoryMenu nextPage, int... protectedSlots) {
        super(name, canInteract, saveOption, InventoryType.CHEST, contents);
        addProtectedSlots(protectedSlots);
        this.nextPageMenu = nextPage;
        this.previousPageMenu = previousPage;
        this.numItems = contents.length;
    }
    public ChestMenu(String name, Map<Integer, ItemStack> contents) {
        this(name, contents, false, SaveOption.NONE, null, null);
    }
    public ChestMenu(String name, Map<Integer, ItemStack> contents, boolean canInteract, SaveOption saveOption, InventoryMenu previousPage, InventoryMenu nextPage, int... protectedSlots) {
        this(name, Utils.mapToArray(contents, true), canInteract, saveOption, previousPage, nextPage, protectedSlots);
    }
    @Override
    protected Inventory newInventory(Player player) {
        Inventory toret = super.newInventory(player);
        int size = toret.getSize();
        if (fillWithBarriers) {
            int i = numItems;
            while (i < size) {
                this.addProtectedSlots(i);
                toret.setItem(i++, BARRIER);
            }
        }
        if (nextPageMenu != null)
            toret.setItem(size - 1, nextPage.toItemStack());
        if (previousPageMenu != null)
            toret.setItem(size - 9, previousPage.toItemStack());
        return toret;
    }
    @Override
    public void setContents(ItemStack[] contents) {
        super.setContents(contents);
        this.numItems = contents.length;
    }
    public void setFillWithBarriers(boolean fillWithBarriers) {
        this.fillWithBarriers = fillWithBarriers;
    }
    public boolean isFillWithBarriers() {
        return fillWithBarriers;
    }
    public void setNextPageMenu(InventoryMenu nextPageMenu) {
        this.nextPageMenu = nextPageMenu;
    }
    public void setPreviousPageMenu(InventoryMenu previousPageMenu) {
        this.previousPageMenu = previousPageMenu;
    }
    @Override
    protected void save(Inventory contents, HumanEntity player, boolean saveToMemory) {
        ItemStack[] items = getWithoutUIElements(contents);
        switch (saveChanges) {
            case NONE:
                break;
            case GLOBAL:
                this.contents = items;
                this.currentlyOpenCopies.stream().filter(o -> !contents.equals(o)).forEach(inv -> inv.setContents(items));
                break;
            case INDIVIDUAL:
                if (!saveToMemory)
                    break;
                AnotherGUIPlugin.getStorage().set("menu-saves." + GUIName + "." + Utils.getPlayerUUID(player.getName()), items);
                AnotherGUIPlugin.getStorage().saveConfig();
        }
    }
    public ItemStack[] getWithoutUIElements(Inventory contents) {
        ItemStack[] items;
        if (this.numItems == 0)
            items = new ItemStack[0];
        else
            items = Arrays.stream(contents.getContents()).map(
                            o -> previousPage.isThisActionItem(o) || (nextPage.isThisActionItem(o) && this.numItems % 9 == 0) ? null : o)
                    .filter(o -> !nextPage.isThisActionItem(o) && !BARRIER.equals(o)).limit(numItems).toArray(ItemStack[]::new);
        return items;
    }
    @Override
    public Inventory getContents() {
        Inventory inv = getInventory();
        inv.setContents(getWithoutUIElements(inv));
        return inv;
    }
}