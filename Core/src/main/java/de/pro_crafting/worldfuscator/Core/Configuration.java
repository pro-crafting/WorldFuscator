package de.pro_crafting.worldfuscator.Core;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Configuration {
    private List<Integer> hideIds;
    private boolean debugEnabled;

    public Configuration(FileConfiguration configuration) {
        this.hideIds = configuration.getIntegerList("hidden");
        this.debugEnabled = configuration.getBoolean("debug.enabled", true);
    }

    public int getObfuscationBlock() {
        return 121;
    }

    public List<Integer> getHideIds() {
        return this.hideIds;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
