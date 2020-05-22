package com.pro_crafting.mc.worldfuscator;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;

public class ConfigurationService {
    private ConfigurationService() {}

    public static YamlConfiguration loadConfigurationFile(JavaPlugin plugin, String fileName) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), fileName));

        final InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream == null) {
            return configuration;
        }

        return configuration;
    }

    public static void saveDefaultConfiguration(JavaPlugin plugin, String fileName) {
        if (!new File(fileName).exists()) {
            plugin.saveResource(fileName, false);
        }
    }
}
