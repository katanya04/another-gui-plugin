package me.katanya04.anotherguiplugin.menu;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AnvilMenu extends InventoryMenu {
    public AnvilMenu(String name) {
        this(name, null, null);
    }
    public AnvilMenu(String name, ItemStack itemOnSlot1, ItemStack itemOnSlot2) {
        this(name, false, false, itemOnSlot1, itemOnSlot2);
    }
    public AnvilMenu(String name, boolean canInteract, boolean saveChanges, ItemStack itemOnSlot1, ItemStack itemOnSlot2) {
        super(name, canInteract, saveChanges, new ItemStack[]{itemOnSlot1, itemOnSlot2}, InventoryType.ANVIL);
    }
    @Override
    public void setContents(Inventory inventory) {
        assert contents.getType() == InventoryType.ANVIL;
        super.setContents(inventory);
    }
}