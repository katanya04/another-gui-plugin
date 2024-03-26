package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import me.katanya04.anotherguiplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
 * A blueprint for an item that does an action when interacted with, either when used in the hotbar or when clicked in the inventory.
 * The items have a nbt tag to easily know if an ItemStack was created by an ActionItem.
 * All instances of this class are stored on a map, so rather than creating multiple, similar ActionItems, it's for the best
 * to create an ActionItem whose itemConstructorFn function is flexible enough to cover all posible cases.
 */
public class ActionItem {
    public static final Map<String, ActionItem> actionItems = new HashMap<>();
    public static final String nameKeyString = "ActionItemName";
    protected Function<Player, ItemStack> itemConstructorFn;
    protected Consumer<ActionItemInteractEvent> onInteract;
    protected Consumer<PlayerDropItemEvent> onThrowBehaviour;
    private final String name;
    protected InventoryMenu parent;
    public ActionItem(ItemStack itemStack, Consumer<ActionItemInteractEvent> onInteract, String uniqueName) {
        this(pl -> itemStack, onInteract, uniqueName);
    }
    public ActionItem(Function<Player, ItemStack> itemStack, Consumer<ActionItemInteractEvent> onInteract, String uniqueName) {
        this.itemConstructorFn = itemStack;
        this.name = uniqueName;
        this.onInteract = onInteract;
        if (actionItems.containsKey(uniqueName))
            throw new RuntimeException("Action item with this name already exists");
        actionItems.put(name, this);
    }
    public ActionItem (ActionItem copyFrom, String newUniqueName) {
        this(copyFrom.itemConstructorFn, copyFrom.onInteract, newUniqueName);
        this.onThrowBehaviour = copyFrom.onThrowBehaviour;
        this.parent = copyFrom.parent;
    }

    public static void unregister(String name) {
        actionItems.remove(name);
    }

    public static ActionItem getByName(String name) {
        return actionItems.get(name);
    }

    public void setItemConstructorFn(Function<Player, ItemStack> itemConstructorFn) {
        this.itemConstructorFn = itemConstructorFn;
    }

    public void setOnInteract(Consumer<ActionItemInteractEvent> onInteract) {
        this.onInteract = onInteract;
    }

    public void setOnThrowBehaviour(Consumer<PlayerDropItemEvent> onThrowBehaviour) {
        this.onThrowBehaviour = onThrowBehaviour;
    }

    public ItemStack toItemStack() {
        return toItemStack(null);
    }

    public ItemStack toItemStack(Player arg) {
        ItemStack toret = itemConstructorFn.apply(arg);
        return toret == null || toret.getType() == Material.AIR ? null : Utils.setItemNBT(toret, nameKeyString, this.name);
    }

    public ItemStack convertToActionItem(ItemStack item) {
        return Utils.setItemNBT(item.clone(), nameKeyString, this.name);
    }

    public ItemStack returnPlaceholder() {
        return Utils.setItemNBT(new ItemStack(Material.PAPER), nameKeyString, this.name);
    }

    protected static String getName(ItemStack itemStack) {
        return Utils.getNBT(itemStack, nameKeyString);
    }

    public static boolean isActionItem(ItemStack itemStack) {
        return Utils.containsNBT(itemStack, nameKeyString);
    }

    public static ActionItem getActionItem(ItemStack itemStack) {
        return actionItems.get(ActionItem.getName(itemStack));
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
            Bukkit.getPluginManager().callEvent(new ActionItemInteractEvent(e.getPlayer(), actionItem, e.getItem(), e.getPlayer().getInventory()));
        }

        @EventHandler
        public void onClickInventory(InventoryClickEvent e) {
            ActionItem actionItem;
            if (e.getClick() == ClickType.LEFT && (actionItem = ActionItem.getActionItem(e.getCurrentItem())) != null) {
                if (e.getClickedInventory().getHolder() instanceof InventoryMenu)
                    actionItem.setParent((InventoryMenu) e.getClickedInventory().getHolder());
                e.setCancelled(true);
                Bukkit.getPluginManager().callEvent(new ActionItemInteractEvent((Player) e.getWhoClicked(), actionItem, e.getCurrentItem(), e.getClickedInventory()));
            }
        }

        @EventHandler
        public void onThrow(PlayerDropItemEvent e) {
            ActionItem actionItem = ActionItem.getActionItem(e.getItemDrop().getItemStack());
            if (actionItem != null && actionItem.onThrowBehaviour != null)
                actionItem.onThrowBehaviour.accept(e);
        }

        @EventHandler
        public void onInteract(ActionItemInteractEvent e) {
            if (e.getActionItem().onInteract != null)
                e.getActionItem().onInteract.accept(e);
        }
    }
}