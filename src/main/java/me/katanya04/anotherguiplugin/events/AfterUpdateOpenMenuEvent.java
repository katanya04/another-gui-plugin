package me.katanya04.anotherguiplugin.events;

import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AfterUpdateOpenMenuEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final InventoryMenu from;
    private final Inventory inventory;
    public AfterUpdateOpenMenuEvent(InventoryMenu from, Inventory inventory) {
        this.from = from;
        this.inventory = inventory;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public InventoryMenu getFrom() {
        return from;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
