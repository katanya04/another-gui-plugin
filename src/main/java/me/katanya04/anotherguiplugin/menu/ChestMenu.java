package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.Utils.Utils;
import me.katanya04.anotherguiplugin.actionItems.ActionItem;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChestMenu extends InventoryMenu {
    protected static final ActionItem nextPage = new ActionItem(Utils.setName(new ItemStack(Material.ARROW), "Next Page"));
    protected static final ActionItem previousPage = new ActionItem(Utils.setName(new ItemStack(Material.ARROW), "Previous Page"));
    protected ChestMenu nextPageMenu;
    protected ChestMenu previousPageMenu;
    public ChestMenu(String name, ItemStack[] contents) {
        this(name, contents, false, true, null, null);
    }
    public ChestMenu(String name, ItemStack[] contents, boolean canInteract, boolean saveChanges, ChestMenu previousPage, ChestMenu nextPage, int... protectedSlots) {
        super(name, canInteract, saveChanges, contents, InventoryType.CHEST);
        addProtectedSlots(protectedSlots);
        this.previousPageMenu = previousPage;
        this.nextPageMenu = nextPage;
    }
    public ChestMenu(String name, Map<Integer, ItemStack> contents) {
        this(name, contents, false, true, null, null);
    }
    public ChestMenu(String name, Map<Integer, ItemStack> contents, boolean canInteract, boolean saveChanges, ChestMenu previousPage, ChestMenu nextPage, int... protectedSlots) {
        this(name, Utils.mapToArray(contents, true), canInteract, saveChanges, previousPage, nextPage, protectedSlots);
    }
    @Override
    protected Inventory newInventory(ItemStack[] contents, InventoryType invType, String name) {
        Inventory toret = super.newInventory(contents, invType, name);
        int size = toret.getSize();
        if (nextPageMenu != null)
            toret.setItem(size - 1, nextPage);
        if (previousPageMenu != null)
            toret.setItem(size - 9, previousPage);
        return toret;
    }
    @Override
    public void setContents(Inventory inventory) {
        assert contents.getType() == InventoryType.CHEST;
        super.setContents(inventory);
    }
}