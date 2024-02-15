package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.InventoryMenuPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ActionItem extends ItemStack implements Listener {
    protected Consumer<Player> onInteract;
    public ActionItem(ItemStack itemStack, Consumer<Player> onInteract) {
        super(itemStack);
        setOnInteract(onInteract);
    }
    public ActionItem(ItemStack itemStack) {
        this(itemStack, null);
    }

    public final void setOnInteract(Consumer<Player> onInteract) {
        this.onInteract = onInteract;
        unregister();
        InventoryMenuPlugin.plugin.getServer().getPluginManager().registerEvents(this, InventoryMenuPlugin.plugin);
    }

    protected final void unregister() {
        HandlerList.unregisterAll(this);
    }

    public void interact(Player player) {
        if (onInteract != null)
            onInteract.accept(player);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemStack))
            return false;
        return super.isSimilar((ItemStack) obj) && !(obj instanceof ActionItem) || onInteract.equals(((ActionItem) obj).onInteract);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!this.equals(e.getItem()) || !(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;
        e.setCancelled(true);
        this.interact(e.getPlayer());
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent e) {
        if (e.getClick() == ClickType.LEFT && this.equals(e.getCurrentItem())) {
            e.setCancelled(true);
            this.interact((Player) e.getWhoClicked());
        }
    }
}