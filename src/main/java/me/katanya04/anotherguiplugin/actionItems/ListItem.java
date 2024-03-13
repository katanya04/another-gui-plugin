package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

/**
 * A build-in type of ActionItem that when interacted scrolls to the next object of a list
 * @param <T> the type of objects that will be stored in the list
 */
public abstract class ListItem<T> extends ActionItem {
    private final List<T> list;
    protected int index;
    protected final boolean nullOption;
    protected int defaultValueIndex;
    public ListItem(ItemStack itemStack, List<T> list, int index, boolean nullOption, Consumer<Player> onInteract, String name) {
        super(itemStack, onInteract, name);
        this.list = list;
        this.index = index;
        this.nullOption = nullOption;
        Utils.setLore(this, "§r§f" + getName());
        super.setOnInteract(player -> {
            if (this.index >= list.size() - 1)
                this.index = nullOption ? -1 : 0;
            else
                this.index++;
            Utils.setLore(this, "§r§f" + getName());
        });
        this.defaultValueIndex = 0;
    }
    private boolean isValidIndex(int index) {
        if (index >= list.size())
            return false;
        return index >= -1 && (index >= 0 || nullOption);
    }
    public void setDefaultValueIndex(int defaultValueIndex) {
        if (!isValidIndex(defaultValueIndex))
            throw new RuntimeException();
        this.defaultValueIndex = defaultValueIndex;
    }
    public T getCurrentItem() {
        return index >= 0 ? list.get(index) : null;
    }
    public String getName() {
        T item = getCurrentItem();
        return item != null ? item.toString() : "None";
    }
    public void setIndex(int index) {
        if (!isValidIndex(index))
            throw new RuntimeException();
        else
            this.index = index;
    }
    public void reset() {
        this.index = defaultValueIndex;
    }
}
