package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.Utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public abstract class ListItem<T> extends ActionItem {
    private final List<T> list;
    protected int index;
    protected final boolean nullOption;
    protected ListItem(ItemStack itemStack, List<T> list, int index, boolean nullOption, Consumer<Player> onInteract) {
        super(itemStack, onInteract);
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
    }
    protected T getCurrentItem() {
        return index >= 0 ? list.get(index) : null;
    }
    protected String getName() {
        return index >= 0 ? list.get(index).toString() : "None";
    }
}
