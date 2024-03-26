package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A build-in type of ActionItem that when interacted scrolls to the next object of a list.
 * The index is stored on the ItemStack representation of the ListItem
 */
public class ListItem extends ActionItem {
    private final List<String> list;
    Function<Player, Integer> initialIndex;
    protected final boolean nullOption;
    public ListItem(ItemStack itemStack, List<String> list, Function<Player, Integer> initialIndex, boolean nullOption, String name) {
        this(pl -> itemStack, list, initialIndex, nullOption, name);
    }
    public ListItem(Function<Player, ItemStack> itemStack, List<String> list, Function<Player, Integer> initialIndex, boolean nullOption, String name) {
        super(itemStack, null, name);
        this.list = new ArrayList<>(new LinkedHashSet<>(list));
        this.initialIndex = initialIndex;
        this.nullOption = nullOption;
        super.setOnInteract(event -> increaseIndex(event.getItem()));
    }
    private boolean isValidIndex(int index) {
        if (index >= list.size())
            return false;
        return index >= -1 && (index >= 0 || nullOption);
    }
    public String getFromItem(ItemStack item) {
        if (item.getItemMeta() == null || item.getItemMeta().getLore() == null)
            return null;
        String value = item.getItemMeta().getLore().get(0);
        return value.replaceFirst("§r§f", "");
    }
    private String getName(int index) {
        return index >= 0 && isValidIndex(index) ? list.get(index) : "None";
    }
    private void increaseIndex(ItemStack item) {
        int index = list.indexOf(getFromItem(item));
        if (index >= list.size() - 1)
            index = nullOption ? -1 : 0;
        else
            index++;
        Utils.setLore(item, "§r§f" + getName(index));
    }
    public void setInitialIndex(Function<Player, Integer> initialIndex) {
        this.initialIndex = initialIndex;
    }
    @Override
    public void setOnInteract(Consumer<ActionItemInteractEvent> onInteract) {
        Consumer<ActionItemInteractEvent> increaseIndex = event -> increaseIndex(event.getItem());
        this.onInteract = increaseIndex.andThen(onInteract);
    }
    @Override
    public ItemStack toItemStack(Player arg) {
        ItemStack item = super.toItemStack(arg);
        int index = initialIndex.apply(arg);
        Utils.setLore(item, "§r§f" + getName(index));
        return item;
    }
}