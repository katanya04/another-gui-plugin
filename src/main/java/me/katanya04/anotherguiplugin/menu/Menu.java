package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * An Interface that represents a Menu, a GUI that players may open to see or edit values
 * @param <T> the contents of the Menu
 */
public interface Menu<T> {
    void openMenu(Player player);
    void setContents(T generateContents);
    T getContents();
    void clear();
    static void openMenuOneTickLater(Player player, Menu<?> invMenu, boolean closeCurrent) {
        if (closeCurrent)
            player.closeInventory();
        (new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isInsideVehicle())
                    invMenu.openMenu(player);
            }
        }).runTaskLater(AnotherGUIPlugin.plugin, 1L);
    }
}
