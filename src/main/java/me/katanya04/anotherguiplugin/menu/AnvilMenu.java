package me.katanya04.anotherguiplugin.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

/**
 * An InventoryMenu of inventory type anvil. It may be created with the parent class InventoryMenu instead, as this class
 * just provides some constructors to make the creation of an anvil inventory easier, but lacks any different methods or
 * fields from its parent class
 */
public class AnvilMenu extends InventoryMenu {
    public AnvilMenu(String name, ItemStack itemOnSlot1, ItemStack itemOnSlot2) {
        this(name, false, SaveOption.NONE, itemOnSlot1, itemOnSlot2);
    }
    public AnvilMenu(String name, boolean canInteract, SaveOption saveOption, ItemStack itemOnSlot1, ItemStack itemOnSlot2) {
        this(name, canInteract, saveOption, pl -> new ItemStack[]{itemOnSlot1, itemOnSlot2});
    }
    public AnvilMenu(String name, Function<Object, ItemStack[]> contents) {
        this(name, false, SaveOption.NONE, contents);
    }
    public AnvilMenu(String name, boolean canInteract, SaveOption saveOption, Function<Object, ItemStack[]> contents) {
        super(name, canInteract, saveOption, InventoryType.ANVIL, contents);
    }
}