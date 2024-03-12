package me.katanya04.anotherguiplugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.katanya04.anotherguiplugin.debug.DebugCommand;
import me.katanya04.anotherguiplugin.menu.BookMenu;
import me.katanya04.anotherguiplugin.utils.PlayerUUIDCache;
import me.katanya04.anotherguiplugin.utils.ReflectionMethods;
import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import me.katanya04.anotherguiplugin.yaml.Config;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
        getServer().getPluginManager().registerEvents(new InventoryMenu.EventListener(), AnotherGUIPlugin.plugin);
        storage = new Config("storage.yml");
        config = new Config("config.yml");
        PlayerUUIDCache.initialize();
        //ProtocolManager manager = ProtocolLibrary.getProtocolManager();

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
