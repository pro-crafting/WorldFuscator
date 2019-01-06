package net.myplayplanet.worldfuscator.Core;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Configuration {

  private Set<Material> hideMaterials = new HashSet<>();
  private boolean debugEnabled;

  public Configuration(FileConfiguration configuration) {
    // read material names and get values of Material enum
    List<String> materialNames = configuration.getStringList("hidden-materials");
    if (materialNames != null) {
      for (String materialName : materialNames) {
        Material material = Material.matchMaterial(materialName, false);
        if (material != null) {
          hideMaterials.add(material);
        }
      }
    }

    this.debugEnabled = configuration.getBoolean("debug.enabled", false);
  }

  public Material getObfuscationBlock() {
    return Material.END_STONE;
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
}
