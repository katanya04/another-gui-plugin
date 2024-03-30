package me.katanya04.anotherguiplugin.actionItems;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ActionItemInteractEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ActionItem actionItem;
    private final ItemStack item;
    private final Inventory inv;
    private final ClickType clickType;
    private final Action actionType;
    private final InteractionType interactionType;

    public ActionItemInteractEvent(Player player, ActionItem actionItem, ItemStack item, Inventory inv, ClickType clickType) {
        this.player = player;
        this.actionItem = actionItem;
        this.item = item;
        this.inv = inv;
        this.clickType = clickType;
        this.actionType = null;
        this.interactionType = fromClickType(clickType);
    }

    public ActionItemInteractEvent(Player player, ActionItem actionItem, ItemStack item, Inventory inv, Action actionType) {
        this.player = player;
        this.actionItem = actionItem;
        this.item = item;
        this.inv = inv;
        this.clickType = null;
        this.actionType = actionType;
        this.interactionType = fromActionType(actionType);
    }

    private InteractionType fromClickType(ClickType clickType) {
        if (clickType.isLeftClick())
            return InteractionType.LEFT_CLICK;
        if (clickType.isRightClick())
            return InteractionType.RIGHT_CLICK;
        return InteractionType.OTHER;
    }

    private InteractionType fromActionType(Action actionType) {
        if (actionType == Action.LEFT_CLICK_AIR || actionType == Action.LEFT_CLICK_BLOCK)
            return InteractionType.LEFT_CLICK;
        if (actionType == Action.RIGHT_CLICK_AIR || actionType == Action.RIGHT_CLICK_BLOCK)
            return InteractionType.RIGHT_CLICK;
        return InteractionType.OTHER;
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

    public Action getActionType() {
        return actionType;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }
}