package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.menu.Menu;
import org.bukkit.inventory.ItemStack;

/**
 * A build-in type of ActionItem that when interacted opens a {@link Menu}
 * @param <T> the type of Menu
 */
public class MenuItem<T extends Menu> extends ActionItem {
    protected T menu;
    public MenuItem(ItemStack itemStack, T menu, String uniqueName) {
        super(itemStack, menu::openMenu, uniqueName);
        this.menu = menu;
    }

    public T getMenu() {
        return menu;
    }
}
