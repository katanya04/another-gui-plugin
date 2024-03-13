package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import me.katanya04.anotherguiplugin.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An item that does an action when interacted with, either when used in the hotbar or when clicked in the inventory
 * <p>
 * Each instance has a nbt tag to easily know if an ItemStack is an ActionItem. This tag contains a numerical id
 * used to identify a specific ActionItem, set to each one sequentially as they are created. They may have another tag
 * to identify that they have been loaded from an {@link InventoryMenu} storage file. In this case, the ActionItem is
 * identified by its name, since a sequential number may be inconsistent (the items may be loaded in a particular
 * order and, after shutting down and restarting the server, they may be loaded in another order)
 */
public class ActionItem extends ItemStack {
    public static final Map<String, ActionItem> actionItems = new HashMap<>();
    public static final String nameKeyString = "ActionItemName";
    protected Function<Object, ItemStack> itemConstructorFn;
    protected Consumer<Player> onInteract;
    protected Consumer<PlayerDropItemEvent> onThrowBehaviour;
    private final String name;
    protected InventoryMenu parent;
    public ActionItem(ItemStack itemStack, Consumer<Player> onInteract, String uniqueName) {
        this(pl -> itemStack, onInteract, uniqueName);
    }
    public ActionItem(Function<Object, ItemStack> itemStack, Consumer<Player> onInteract, String uniqueName) {
        super(Utils.setItemNBT(itemStack.apply(null), nameKeyString, uniqueName));
        this.itemConstructorFn = itemStack;
        this.name = uniqueName;
        this.onInteract = onInteract;
        if (actionItems.containsKey(uniqueName))
            throw new RuntimeException("Action item with this name already exists");
        actionItems.put(name, this);
    }

    public static ActionItem getByName(String name) {
        return actionItems.get(name);
    }

    public void setItemConstructorFn(Function<Object, ItemStack> itemConstructorFn) {
        this.itemConstructorFn = itemConstructorFn;
    }

    public final void setOnInteract(Consumer<Player> onInteract) {
        this.onInteract = onInteract;
    }

    public void setOnThrowBehaviour(Consumer<PlayerDropItemEvent> onThrowBehaviour) {
        this.onThrowBehaviour = onThrowBehaviour;
    }

    public void interact(Player player) {
        if (onInteract != null)
            onInteract.accept(player);
    }

    public ItemStack getItemStack() {
        return getItemStack(null);
    }

    public ItemStack getItemStack(Object arg) {
        ItemStack toret = itemConstructorFn.apply(arg);
        return Utils.setItemNBT(toret, nameKeyString, this.name);
    }

    protected static String getName(ItemStack itemStack) {
        return Utils.getNBT(itemStack, nameKeyString);
    }

    public static boolean isActionItem(ItemStack itemStack) {
        return itemStack instanceof ActionItem || Utils.containsNBT(itemStack, nameKeyString);
    }

    public static ActionItem getActionItem(ItemStack itemStack) {
        return itemStack instanceof ActionItem ? (ActionItem) itemStack : actionItems.get(ActionItem.getName(itemStack));
    }

    public InventoryMenu getParent() {
        return parent;
    }

    public void setParent(InventoryMenu parent) {
        this.parent = parent;
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onInteract(PlayerInteractEvent e) {
            ActionItem actionItem;
            if (!(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) || (actionItem = ActionItem.getActionItem(e.getItem())) == null)
                return;
            actionItem.setParent(null);
            e.setCancelled(true);
            actionItem.interact(e.getPlayer());
        }

        @EventHandler
        public void onClickInventory(InventoryClickEvent e) {
            ActionItem actionItem;
            if (e.getClick() == ClickType.LEFT && (actionItem = ActionItem.getActionItem(e.getCurrentItem())) != null) {
                e.setCancelled(true);
                if (e.getClickedInventory().getHolder() instanceof InventoryMenu)
                    actionItem.setParent((InventoryMenu) e.getClickedInventory().getHolder());
                actionItem.interact((Player) e.getWhoClicked());
            }
        }

        @EventHandler
        public void onThrow(PlayerDropItemEvent e) {
            ActionItem actionItem = ActionItem.getActionItem(e.getItemDrop().getItemStack());
            if (actionItem != null && actionItem.onThrowBehaviour != null)
                actionItem.onThrowBehaviour.accept(e);
        }
    }
}