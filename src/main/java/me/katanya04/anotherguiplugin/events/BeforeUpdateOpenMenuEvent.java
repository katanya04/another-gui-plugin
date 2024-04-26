package me.katanya04.anotherguiplugin.events;

import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BeforeUpdateOpenMenuEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    private final InventoryMenu from;
    private final Inventory inventoryToBeUpdated;
    private ItemStack[] newContents;
    public BeforeUpdateOpenMenuEvent(InventoryMenu from, Inventory inventoryToBeUpdated, ItemStack[] newContents) {
        this.from = from;
        this.inventoryToBeUpdated = inventoryToBeUpdated;
        this.newContents = newContents;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    public InventoryMenu getFrom() {
        return from;
    }

    public Inventory getInventoryToBeUpdated() {
        return inventoryToBeUpdated;
    }

    public ItemStack[] getNewContents() {
        return newContents;
    }

    public void setNewContents(ItemStack[] newContents) {
        this.newContents = newContents;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }
}
