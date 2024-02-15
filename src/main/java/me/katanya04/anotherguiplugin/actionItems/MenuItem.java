package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.menu.Menu;
import org.bukkit.inventory.ItemStack;

public class MenuItem<T extends Menu> extends ActionItem {
    protected T menu;
    protected MenuItem(ItemStack itemStack, T menu) {
        super(itemStack);
        this.menu = menu;
        setOnInteract(player -> this.menu.openMenu(player));
    }

    public T getMenu() {
        return menu;
    }
}
