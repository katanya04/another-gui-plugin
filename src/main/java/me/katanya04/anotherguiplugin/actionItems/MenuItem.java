package me.katanya04.anotherguiplugin.actionItems;

import me.katanya04.anotherguiplugin.events.ActionItemInteractEvent;
import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import me.katanya04.anotherguiplugin.menu.Menu;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A build-in type of ActionItem that when interacted opens a {@link Menu}
 * @param <T> the type of Menu
 */
public class MenuItem<T extends Menu<?>, U> extends ActionItem<U> {
    protected T menu;
    public MenuItem(ItemStack itemStack, T menu, String uniqueName) {
        this(pl -> itemStack, menu, uniqueName);
    }
    public MenuItem(Function<U, ItemStack> itemStack, T menu, String uniqueName) {
        super(itemStack, event -> {
            if (event.getInv().getHolder() instanceof InventoryMenu) {
                InventoryMenu inv = (InventoryMenu) event.getInv().getHolder();
                if (Objects.equals(inv, menu))
                    return;
            }
            menu.openMenu(event.getPlayer());
        }, uniqueName);
        this.menu = menu;
    }
    @Override
    public void setOnInteract(Consumer<ActionItemInteractEvent> onInteract) {
        this.onInteract = onInteract.andThen(event -> menu.openMenu(event.getPlayer()));
    }

    public T getMenu() {
        return menu;
    }
}
