package com.pro_crafting.mc.worldfuscator;

import com.pro_crafting.mc.worldfuscator.engine.processor.FuscationMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Configuration {

    private boolean debugEnabled;
    private FuscationMode fuscationMode;
    private Set<Material> hideMaterials = new HashSet<>();
    private Set<String> hideBlockData = new HashSet<>();
    private Material preferredObfuscationMaterial;
    private Set<String> hiddenBlockEntityIds;
    private int asyncWorkerCount;

    public Configuration(FileConfiguration configuration) {
        this.debugEnabled = configuration.getBoolean("debug.enabled", false);
        this.fuscationMode = FuscationMode.valueOf(configuration.getString("fuscation-mode", FuscationMode.CHUNK_AND_BLOCK.name()));

        this.asyncWorkerCount = configuration.getInt("async.worker-count", 0);

        String preferredObfuscationBlockName = configuration.getString("preferred-obfuscation-material", "minecraft:end_stone");
        preferredObfuscationMaterial = Material.matchMaterial(preferredObfuscationBlockName);

        hiddenBlockEntityIds = new HashSet<>(configuration.getStringList("hidden.block-entities"));
        // read material names and get values of Material enum
        List<String> materialNames = configuration.getStringList("hidden.block.material");
        for (String materialName : materialNames) {
            Material material = Material.matchMaterial(materialName, false);
            if (material != null) {
                hideMaterials.add(material);
            }
        }

        // read block data
        List<String> blockDataValues = configuration.getStringList("hidden.block.block-data");
        hideBlockData = new HashSet<>(blockDataValues);
    }

    public Material getPreferredObfuscationMaterial() {
        return preferredObfuscationMaterial;
    }

    public Set<Material> getHideMaterials() {
        return this.hideMaterials;
    }

    public Set<String> getHideBlockData() {
        return hideBlockData;
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
}
