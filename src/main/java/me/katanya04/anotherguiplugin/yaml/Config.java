package me.katanya04.anotherguiplugin.yaml;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

public class Config extends YamlConfiguration {
    private final String name;
    private final File file;
    public Config(String nameFile) {
        this.name = nameFile;
        this.file = new File(AnotherGUIPlugin.plugin.getDataFolder().getPath(), nameFile);
        saveDefaultConfig();
    }

    private void saveDefaultConfig() {
        if (!this.file.exists()) {
            try {
                Files.createDirectories(Paths.get(AnotherGUIPlugin.plugin.getDataFolder().getPath()));
                FileUtils.copyInputStreamToFile(AnotherGUIPlugin.plugin.getResource(name), this.file);
            } catch (IOException e) {
                AnotherGUIPlugin.getLog().log(Level.SEVERE, "Error while creating " + name + " config file");
                throw new RuntimeException(e);
            }
        }
        loadConfig();
    }

    private void loadConfig() {
        try {
            load(file);
        } catch (IOException | InvalidConfigurationException e) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Error while loading " + name + " config file");
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public void saveConfig() {
        try {
            save(file);
        } catch (IOException e) {
            AnotherGUIPlugin.getLog().log(Level.SEVERE, "Error while saving " + name + " config file");
        }
    }
}
