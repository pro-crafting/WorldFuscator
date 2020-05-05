package com.pro_crafting.mc.worldfuscator;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Configuration {

    private Set<Material> hideMaterials = new HashSet<>();
    private Material preferredObfuscationMaterial;
    private boolean debugEnabled;
    private Set<String> hiddenBlockEntityIds;

    public Configuration(FileConfiguration configuration) {
        hiddenBlockEntityIds = new HashSet<>(configuration.getStringList("hidden-block-entities"));

        // read material names and get values of Material enum
        List<String> materialNames = configuration.getStringList("hidden-materials");
        for (String materialName : materialNames) {
            Material material = Material.matchMaterial(materialName, false);
            if (material != null) {
                hideMaterials.add(material);
            }
        }

        String preferredObfuscationBlockName = configuration.getString("preferred-obfuscation-material", "minecraft:end_stone");
        preferredObfuscationMaterial = Material.matchMaterial(preferredObfuscationBlockName);

        this.debugEnabled = configuration.getBoolean("debug.enabled", false);
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
}
