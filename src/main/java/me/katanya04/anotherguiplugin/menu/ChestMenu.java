package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import me.katanya04.anotherguiplugin.utils.Utils;
import me.katanya04.anotherguiplugin.actionItems.ActionItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Function;

/**
 * An InventoryMenu that supports scrolling between pages. Similar behaviour could be manually added to the InventoryMenu
 * class, but this one already provides it.
 */
public class ChestMenu extends InventoryMenu {
    protected static final ActionItem nextPage = new ActionItem(Utils.setName(new ItemStack(Material.ARROW), "Next Page"),
            pl -> {
                ChestMenu currentChestMenu = (ChestMenu) InventoryMenu.getAnotherPlayersMenu(pl);
                if (currentChestMenu == null)
                    return;
                Menu.openMenuOneTickLater(pl, currentChestMenu.nextPageMenu, true);
            }, "nextPage");
    protected static final ActionItem previousPage = new ActionItem(Utils.setName(new ItemStack(Material.ARROW), "Previous Page"),
            pl -> {
                ChestMenu currentChestMenu = (ChestMenu) InventoryMenu.getAnotherPlayersMenu(pl);
                if (currentChestMenu == null)
                    return;
                Menu.openMenuOneTickLater(pl, currentChestMenu.previousPageMenu, true);
            }, "previousPage");
    protected InventoryMenu nextPageMenu;
    protected InventoryMenu previousPageMenu;
    public ChestMenu(String name, ItemStack[] contents) {
        this(name, contents, false, SaveOption.NONE, null, null);
    }
    public ChestMenu(String name, ItemStack[] contents, boolean canInteract, SaveOption saveOption, InventoryMenu previousPage, InventoryMenu nextPage, int... protectedSlots) {
        super(name, canInteract, saveOption, InventoryType.CHEST, contents);
        addProtectedSlots(protectedSlots);
        this.nextPageMenu = nextPage;
        this.previousPageMenu = previousPage;
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
        if (nextPageMenu != null)
            toret.setItem(size - 1, nextPage.getItemStack(player));
        if (previousPageMenu != null)
            toret.setItem(size - 9, previousPage.getItemStack(player));
        return toret;
    }
}