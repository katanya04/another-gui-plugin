package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import me.katanya04.anotherguiplugin.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * An item that does an action when interacted with, either when used in the hotbar or when clicked in the inventory
 * <p>
 * An event listener is registered when setting the action to be done, so it would rather be a good idea to not create
 * duplicates or similar instances when whatever you're trying to achieve can be done with less of them
 * <p>
 * Each instance has a nbt tag to easily know if an ItemStack is an ActionItem. This tag contains a numerical id
 * used to identify a specific ActionItem, set to each one sequentially as they are created. They may have another tag
 * to identify that they have been loaded from an {@link InventoryMenu} storage file. In this case, the ActionItem is
 * identified by its name, since a sequential number may be inconsistent (the items may be loaded in a particular
 * order and, after shutting down and restarting the server, they may be loaded in another order)
 */
public class ActionItem extends ItemStack implements Listener {
    protected static int ids = 0;
    public static final String IDKeyString = "ActionItemID";
    public static final String nameKeyString = "ActionItemName";
    protected Consumer<Player> onInteract;
    protected Consumer<PlayerDropItemEvent> onThrowBehaviour;
    protected String name;

    public ActionItem(ItemStack itemStack, Consumer<Player> onInteract) {
        this(itemStack, onInteract, "");
    }
    public ActionItem(ItemStack itemStack, Consumer<Player> onInteract, String name) {
        super(Utils.setItemNBT(itemStack, Utils.getActionItemMap(ids++, name)));
        this.name = name;
        setOnInteract(onInteract);
    }

    public final void setOnInteract(Consumer<Player> onInteract) {
        this.onInteract = onInteract;
        unregister();
        AnotherGUIPlugin.plugin.getServer().getPluginManager().registerEvents(this, AnotherGUIPlugin.plugin);
    }

    public void canBeThrown(boolean canBeThrown) {
        if (!canBeThrown)
            this.onThrowBehaviour = event -> event.setCancelled(true);
    }

    public void setOnThrowBehaviour(Consumer<PlayerDropItemEvent> onThrowBehaviour) {
        this.onThrowBehaviour = onThrowBehaviour;
    }

    protected final void unregister() {
        HandlerList.unregisterAll(this);
    }

    public void interact(Player player) {
        if (onInteract != null)
            onInteract.accept(player);
    }

    protected static int getId(ItemStack itemStack) {
        String id = Utils.getNBT(itemStack, IDKeyString);
        return id != null ? Integer.parseInt(id) : -1;
    }

    public static boolean isActionItem(ItemStack itemStack) {
        return Utils.containsNBT(itemStack, IDKeyString);
    }

    public static boolean isActionItemFromMemory(ItemStack itemStack) {
        return Utils.containsNBT(itemStack, InventoryMenu.ACTION_ITEM_FROM_MEMORY);
    }

    public String getName() {
        return name;
    }

    public static int numCreatedItems() {
        return ids;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemStack))
            return false;
        ItemStack item = (ItemStack) obj;
        return isActionItemFromMemory(item) && !this.name.isEmpty() ?
                Objects.equals(Utils.getNBT(this, nameKeyString), Utils.getNBT(item, nameKeyString)) : getId(item) == getId(this);
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

    @EventHandler
    public void onThrow(PlayerDropItemEvent e) {
        if (this.onThrowBehaviour != null)
            this.onThrowBehaviour.accept(e);
    }
}