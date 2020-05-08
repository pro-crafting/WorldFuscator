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
    private Material preferredObfuscationMaterial;
    private Set<String> hiddenBlockEntityIds;

    public Configuration(FileConfiguration configuration) {
        this.debugEnabled = configuration.getBoolean("debug.enabled", false);
        this.fuscationMode = FuscationMode.valueOf(configuration.getString("fuscation-mode", FuscationMode.CHUNK_AND_BLOCK.name()));

        String preferredObfuscationBlockName = configuration.getString("referred-obfuscation-material", "minecraft:end_stone");
        preferredObfuscationMaterial = Material.matchMaterial(preferredObfuscationBlockName);

        hiddenBlockEntityIds = new HashSet<>(configuration.getStringList("hidden-block-entities"));
        // read material names and get values of Material enum
        List<String> materialNames = configuration.getStringList("hidden-materials");
        for (String materialName : materialNames) {
            Material material = Material.matchMaterial(materialName, false);
            if (material != null) {
                hideMaterials.add(material);
            }
        }
    }

    public Material getPreferredObfuscationMaterial() {
        return preferredObfuscationMaterial;
    }

    public Set<Material> getHideMaterials() {
        return this.hideMaterials;
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
}
