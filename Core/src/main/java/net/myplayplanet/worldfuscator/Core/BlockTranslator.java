package net.myplayplanet.worldfuscator.Core;

import net.myplayplanet.worldfuscator.Core.palette.GlobalPaletteAdapter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockTranslator {

  private GlobalPaletteAdapter globalPaletteAdapter = new GlobalPaletteAdapter();

  private Configuration configuration;
  private List<Integer> hiddenGlobalPaletteIds = new ArrayList<>();
  private Integer preferedObfuscationGlobalPaletteId;

  public BlockTranslator() {
  }

  public BlockTranslator(Configuration configuration) {
    this.configuration = configuration;
    updatePaletteIds();
  }

  private void updatePaletteIds() {
    // Initialize list with global palette ids to hide
    Set<Material> hideMaterials = configuration.getHideMaterials();
    for (Material hideMaterial : hideMaterials) {
      hiddenGlobalPaletteIds.addAll(globalPaletteAdapter.getAllStateIds(hideMaterial));
    }

    // TODO: Use default block state instead of any
    preferedObfuscationGlobalPaletteId = globalPaletteAdapter.getAllStateIds(configuration.getPreferredObfuscationBlock()).iterator().next();
  }

  protected boolean hasRights(Player player, int x, int y, int z, World world) {
    return false;
  }

  public boolean needsTranslation(World world, int x, int y, int z, Player player) {
    if (!this.hasRights(player, x, y, z, world)) {
      if (configuration.isDebugEnabled()) {
        System.out.println(
            "No Rights: Translation for " + x + "|" + y + "|" + z + " for " + player.getName());
      }
      return true;
    }
    if (configuration.isDebugEnabled()) {
      System.out.println(
          "Passed: Translation for " + x + "|" + y + "|" + z + " for " + player.getName());
    }
    return false;
  }

  public Configuration getConfiguration() {
    return this.configuration;
  }

  void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
    updatePaletteIds();
  }

  public List<Integer> getHiddenGlobalPaletteIds() {
    return hiddenGlobalPaletteIds;
  }

  public Integer getPreferedObfuscationGlobalPaletteId() {
    return preferedObfuscationGlobalPaletteId;
  }
}
