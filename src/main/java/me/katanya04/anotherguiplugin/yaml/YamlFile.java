package me.katanya04.anotherguiplugin.yaml;

import org.bukkit.configuration.ConfigurationSection;

public interface YamlFile extends ConfigurationSection {
    void saveDefaultConfig();
    void loadConfig();
    void reloadConfig();
    void saveConfig();
}
