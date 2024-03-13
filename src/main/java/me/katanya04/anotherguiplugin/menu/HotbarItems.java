package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.actionItems.ActionItem;
import me.katanya04.anotherguiplugin.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Simple class to store and set player's hotbars
 */
public class HotbarItems {
    protected final ItemStack[] contents;
    protected BiFunction<Player, ItemStack, Boolean> canBeGiven;
    public HotbarItems() {
        this.contents = new ItemStack[9];
    }
    public HotbarItems(ItemStack[] contents) {
        this();
        setContents(contents);
    }
    public HotbarItems(Map<Integer, ItemStack> contents) {
        this(Utils.mapToArray(contents, true));
    }
    public void apply(Player player) {
        int i = 0;
        for (ItemStack item : this.contents)
            if (canBeGiven == null || canBeGiven.apply(player, item))
                player.getInventory().setItem(i++, ActionItem.isActionItem(item) ?
                        ActionItem.getActionItem(item).getItemStack(player) : item);
    }
    public ItemStack getItem(int slot) {
        return this.contents[slot];
    }
    public void setItem(int slot, ItemStack item) {
        this.contents[slot] = item;
    }
    public void setContents(ItemStack[] contents) {
        if (contents.length > 9)
            throw new RuntimeException("Contents of hotbar should be 9 or less");
        else
            for (int i = 0; i < contents.length; i++)
                setItem(i, contents[i]);
    }
    public ItemStack[] getContents() {
        return contents;
    }

    public void setCanBeGiven(BiFunction<Player, ItemStack, Boolean> canBeGiven) {
        this.canBeGiven = canBeGiven;
    }

    public BiFunction<Player, ItemStack, Boolean> getCanBeGivenFunction() {
        return canBeGiven;
    }
}