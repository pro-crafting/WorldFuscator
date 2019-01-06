package net.myplayplanet.worldfuscator.Plugin;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.myplayplanet.worldfuscator.Core.BlockTranslator;
import net.myplayplanet.worldfuscator.Core.WorldFuscator;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldFuscatorImpl extends WorldFuscator {

  public void onEnable() {
    setTranslator(new Translator());
    super.onEnable();
  }

  private class Translator extends BlockTranslator {

    WorldGuardPlugin wgp = WorldGuardPlugin.inst();
    WorldGuard wg = WorldGuard.getInstance();

    @Override
    protected boolean hasRights(Player player, int x, int y, int z, World world) {
      LocalPlayer wgPlayer = wgp.wrapPlayer(player);
      RegionContainer container = wg.getPlatform().getRegionContainer();

      // TODO: Maybe use new Spatial Queries api for performance reasons?

      RegionManager manager = container.get(new BukkitWorld(world));
      ApplicableRegionSet ars = manager.getApplicableRegions(BlockVector3.at(x, y, z));

      for (ProtectedRegion rg : ars) {
        if (rg.isMember(wgPlayer)) {
          return true;
        }
      }
      return ars.queryState(wgPlayer, Flags.ENABLE_SHOP) == StateFlag.State.ALLOW;
    }
  }
}
