package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.events.ActionItemInteractEvent;
import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import me.katanya04.anotherguiplugin.utils.ReflectionMethods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
 * @param <T> the class of the argument used to parse the ActionItem to a ItemStack
 */
public class ActionItem<T> {
    public static final Map<String, ActionItem<?>> actionItems = new HashMap<>();
    public static final String nameKeyString = "ActionItemName";
    protected Function<T, ItemStack> itemConstructorFn;
    protected Consumer<ActionItemInteractEvent> onInteract;
    protected Consumer<PlayerDropItemEvent> onThrowBehaviour;
    private final String name;
    protected InventoryMenu parent;
    public ActionItem(ItemStack itemStack, Consumer<ActionItemInteractEvent> onInteract, String uniqueName) {
        this(pl -> itemStack, onInteract, uniqueName);
    }
    public ActionItem(Function<T, ItemStack> itemStack, Consumer<ActionItemInteractEvent> onInteract, String uniqueName) {
        this.itemConstructorFn = itemStack;
        this.name = uniqueName;
        this.onInteract = onInteract;
        if (actionItems.containsKey(uniqueName))
            throw new RuntimeException("Action item with this name already exists");
        actionItems.put(name, this);
    }
    public ActionItem(ActionItem<T> copyFrom, String newUniqueName) {
        this(copyFrom.itemConstructorFn, copyFrom.onInteract, newUniqueName);
        this.onThrowBehaviour = copyFrom.onThrowBehaviour;
        this.parent = copyFrom.parent;
    }

    public static void unregister(String name) {
        actionItems.remove(name);
    }

    public static ActionItem<Object> getByName(String name) {
        return (ActionItem) actionItems.get(name);
    }

    public void setItemConstructorFn(Function<T, ItemStack> itemConstructorFn) {
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

    public ItemStack toItemStack(T arg) {
        ItemStack toret = itemConstructorFn.apply(arg);
        return toret == null || toret.getType() == Material.AIR ? null : ReflectionMethods.setItemNBT(toret, nameKeyString, this.name);
    }

    public ItemStack convertToActionItem(ItemStack item) {
        return ReflectionMethods.setItemNBT(item.clone(), nameKeyString, this.name);
    }

    public ItemStack returnPlaceholder() {
        return ReflectionMethods.setItemNBT(new ItemStack(Material.PAPER), nameKeyString, this.name);
    }

    protected static String getName(ItemStack itemStack) {
        return ReflectionMethods.getNBT(itemStack, nameKeyString);
    }

    public static boolean isActionItem(ItemStack itemStack) {
        return ReflectionMethods.containsNBT(itemStack, nameKeyString);
    }

    public boolean isThisActionItem(ItemStack itemStack) {
        return isActionItem(itemStack) && getName(itemStack).equals(this.name);
    }

    public static ActionItem<Object> getActionItem(ItemStack itemStack) {
        return getByName(ActionItem.getName(itemStack));
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
            ActionItem<Object> actionItem = ActionItem.getActionItem(e.getItem());
            if (actionItem == null)
                return;
            actionItem.setParent(null);
            e.setCancelled(true);
            Bukkit.getPluginManager().callEvent(new ActionItemInteractEvent(e.getPlayer(), actionItem, e.getItem(),
                    e.getPlayer().getInventory(), e.getAction()));
        }

        @EventHandler
        public void onClickInventory(InventoryClickEvent e) {
            ActionItem<Object> actionItem = ActionItem.getActionItem(e.getCurrentItem());
            if (actionItem == null)
                return;
            if (e.getClickedInventory().getHolder() instanceof InventoryMenu) {
                InventoryMenu invMenu = (InventoryMenu) e.getClickedInventory().getHolder();
                actionItem.setParent(invMenu);
                if (actionItem instanceof MenuItem)
                    ((MenuItem<?, ?>) actionItem).getMenu().setParent(invMenu);
            }
            e.setCancelled(true);
            Bukkit.getPluginManager().callEvent(new ActionItemInteractEvent((Player) e.getWhoClicked(), actionItem,
                    e.getCurrentItem(), e.getClickedInventory(), e.getClick()));
        }

        @EventHandler
        public void onThrow(PlayerDropItemEvent e) {
            ActionItem<Object> actionItem = ActionItem.getActionItem(e.getItemDrop().getItemStack());
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