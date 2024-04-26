package me.katanya04.anotherguiplugin.events;

import me.katanya04.anotherguiplugin.menu.ChestMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class ChangePageChestMenuEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ChestMenu chestMenu;
    private final Player player;
    private final Inventory inv;
    private final int currentPage;
    private int pageToBeMoved;
    private boolean cancelled;
    public ChangePageChestMenuEvent(ChestMenu chestMenu, Player player, Inventory inv, int currentPage, int pageToBeMoved) {
        this.chestMenu = chestMenu;
        this.player = player;
        this.inv = inv;
        this.currentPage = currentPage;
        this.pageToBeMoved = pageToBeMoved;
        this.cancelled = false;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public ChestMenu getChestMenu() {
        return chestMenu;
    }

    public int getPageToBeMoved() {
        return pageToBeMoved;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public Inventory getInv() {
        return inv;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPageToBeMoved(int pageToBeMoved) {
        this.pageToBeMoved = pageToBeMoved;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
