package me.katanya04.anotherguiplugin.actionItems;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ActionItemInteractEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ActionItem actionItem;
    private final ItemStack item;
    private final Inventory inv;

    public ActionItemInteractEvent(Player player, ActionItem actionItem, ItemStack item, Inventory inv) {
        this.player = player;
        this.actionItem = actionItem;
        this.item = item;
        this.inv = inv;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItem() {
        return item;
    }

    public Inventory getInv() {
        return inv;
    }

    public ActionItem getActionItem() {
        return actionItem;
    }
}
