package net.myplayplanet.worldfuscator.Plugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.myplayplanet.worldfuscator.Core.BlockTranslator;
import net.myplayplanet.worldfuscator.Core.WorldFuscator;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldFuscatorImpl extends WorldFuscator {

  public void onEnable() {
    setTranslater(new Translator());
    super.onEnable();
  }

  private class Translator extends BlockTranslator {

    WorldGuardPlugin wg = WorldGuardPlugin.inst();

    @Override
    protected boolean hasRights(Player player, int x, int y, int z, World world) {
      ApplicableRegionSet ars = wg.getRegionManager(world)
          .getApplicableRegions(new Vector(x, y, z));
      LocalPlayer wgPlayer = wg.wrapPlayer(player);
      for (ProtectedRegion rg : ars) {
        if (rg.isMember(wgPlayer)) {
          return true;
        }
      }
      return ars.allows(DefaultFlag.ENABLE_SHOP);
    }
  }
}
