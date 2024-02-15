package me.katanya04.anotherguiplugin.menu;

import org.bukkit.entity.Player;

public interface Menu<T> {
    void openMenu(Player player);
    T getContents();
    void clear();
}
