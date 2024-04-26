package me.katanya04.anotherguiplugin;

import me.katanya04.anotherguiplugin.actionItems.ActionItem;
import me.katanya04.anotherguiplugin.debug.DebugCommand;
import me.katanya04.anotherguiplugin.menu.BookMenu;
import me.katanya04.anotherguiplugin.menu.ChestMenu;
import me.katanya04.anotherguiplugin.utils.PlayerUUIDCache;
import me.katanya04.anotherguiplugin.utils.ReflectionMethods;
import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import me.katanya04.anotherguiplugin.utils.Skulls;
import me.katanya04.anotherguiplugin.yaml.Config;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class AnotherGUIPlugin extends JavaPlugin {
    public static AnotherGUIPlugin plugin;
    private static Config storage;
    private static Config config;
    @Override
    public void onEnable() {
        plugin = this;
        try {
            ReflectionMethods.cacheObjects();
        } catch (Exception ex) {
            getLog().log(Level.SEVERE, "Reflection error, plugin may not work as expected");
            ex.printStackTrace();
        }
        storage = new Config("storage.yml");
        storage = new Config("storage.yml");
        config = new Config("config.yml");
        PlayerUUIDCache.initialize();
        Skulls.initialize();
        getServer().getPluginManager().registerEvents(new InventoryMenu.EventListener(), AnotherGUIPlugin.plugin);
        getServer().getPluginManager().registerEvents(new ChestMenu.EventListener(), AnotherGUIPlugin.plugin);
        getServer().getPluginManager().registerEvents(new ActionItem.EventListener(), AnotherGUIPlugin.plugin);
        getServer().getPluginManager().registerEvents(new PlayerUUIDCache.PutOnCacheOnJoin(), AnotherGUIPlugin.plugin);
        getServer().getPluginManager().registerEvents(new Skulls.PutOnCacheOnJoin(), AnotherGUIPlugin.plugin);

        getCommand("menusDebug").setExecutor(new DebugCommand());
        getCommand("menus").setExecutor(new BookMenu.CommandListener());

        this.getLogger().info("Inventory menu API enabled");
    }
    public static Logger getLog() {
        return plugin.getLogger();
    }
    public static Config getStorage() {
        return storage;
    }
    public static Config getConfiguration() {
        return config;
    }
}
