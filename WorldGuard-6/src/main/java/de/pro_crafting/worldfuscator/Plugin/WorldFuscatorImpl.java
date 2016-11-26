package de.pro_crafting.worldfuscator.Plugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.pro_crafting.worldfuscator.Core.BlockTranslater;
import de.pro_crafting.worldfuscator.Core.WorldFuscator;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldFuscatorImpl extends WorldFuscator {
    public void onEnable() {
        setTranslater(new Translater());
        super.onEnable();
    }

    private class Translater extends BlockTranslater {
        WorldGuardPlugin wg = WorldGuardPlugin.inst();

        @Override
        protected boolean hasRights(Player player, int x, int y, int z, World world) {
            ApplicableRegionSet ars = wg.getRegionManager(world).getApplicableRegions(new Vector(x, y, z));
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
