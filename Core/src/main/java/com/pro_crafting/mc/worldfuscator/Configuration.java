package com.pro_crafting.mc.worldfuscator;

import com.pro_crafting.mc.worldfuscator.engine.processor.FuscationMode;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Configuration {

    private boolean debugEnabled;
    private FuscationMode fuscationMode;
    private List<String> hiddenMaterialFilters;
    private String preferredObfuscationFilter;
    private Set<String> hiddenBlockEntityIds;
    private int asyncWorkerCount;

    public Configuration(FileConfiguration configuration) {
        this.debugEnabled = configuration.getBoolean("debug.enabled", false);
        this.fuscationMode = FuscationMode.valueOf(configuration.getString("fuscation-mode", FuscationMode.CHUNK_AND_BLOCK.name()));

        this.asyncWorkerCount = configuration.getInt("async.worker-count", 0);

        this.preferredObfuscationFilter = configuration.getString("preferred-obfuscation-filter", "minecraft:end_stone");

        this.hiddenBlockEntityIds = new HashSet<>(configuration.getStringList("hidden.block.entities"));
        // read material filters
        this.hiddenMaterialFilters = configuration.getStringList("hidden.block.filters");
    }

    public String getPreferredObfuscationFilter() {
        return preferredObfuscationFilter;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public Set<String> getHiddenBlockEntityIds() {
        return hiddenBlockEntityIds;
    }

    public FuscationMode getFuscationMode() {
        return fuscationMode;
    }

    public int getAsyncWorkerCount() {
        return asyncWorkerCount;
    }

    public List<String> getHiddenMaterialFilters() {
        return hiddenMaterialFilters;
    }
}
