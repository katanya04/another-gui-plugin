package me.katanya04.anotherguiplugin;

import me.katanya04.anotherguiplugin.Utils.ReflectionMethods;
import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class InventoryMenuPlugin extends JavaPlugin {
    public static InventoryMenuPlugin plugin;
    @Override
    public void onEnable() {
        plugin = this;
        try {
            ReflectionMethods.cacheObjects();
        } catch (Exception ex) {
            getLog().log(Level.SEVERE, "Reflection error, plugin may not work as expected");
        }
        getServer().getPluginManager().registerEvents(new InventoryMenu.EventListener(), InventoryMenuPlugin.plugin);
        this.getLogger().info("Inventory menu API enabled");
    }
    public static Logger getLog() {
        return plugin.getLogger();
    }
}
