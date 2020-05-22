package com.pro_crafting.mc.worldfuscator;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigurationService {
    private ConfigurationService() {}

    public static YamlConfiguration loadConfigurationFile(JavaPlugin plugin, String fileName) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File(fileName));

        final InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream == null) {
            return configuration;
        }

        configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));

        return configuration;
    }

    public static void saveDefaultConfiguration(JavaPlugin plugin, String fileName) {
        if (!new File(fileName).exists()) {
            plugin.saveResource(fileName, false);
        }
    }
}
