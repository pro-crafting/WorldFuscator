package com.pro_crafting.mc.worldfuscator.worldguard7;

import com.pro_crafting.mc.worldfuscator.engine.BlockTranslator;
import com.pro_crafting.mc.worldfuscator.WorldFuscator;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class WorldFuscatorImpl extends WorldFuscator {

    public void onEnable() {
        setTranslator(new Translator());
        super.onEnable();
    }

    public void updateRegion(World world, String id, Collection<UUID> oldMembers, Collection<UUID> newMembers) {
        if (getConfiguration().isDebugEnabled()) {
            Bukkit.getLogger().info("Chunk refresh of region: " + id);
        }

        WorldGuardPlugin wgp = WorldGuardPlugin.inst();
        WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager rm = container.get(BukkitAdapter.adapt(world));
        ProtectedRegion region = rm.getRegion(id);

        getWorldRefresher().updateArea(world,
                BukkitAdapter.adapt(world, region.getMinimumPoint()),
                BukkitAdapter.adapt(world, region.getMaximumPoint()),
                oldMembers,
                newMembers
        );
    }

    private class Translator extends BlockTranslator {

        WorldGuardPlugin wgp = WorldGuardPlugin.inst();
        WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        @Override
        protected boolean hasRights(Player player, int x, int y, int z, World world) {
            LocalPlayer wgPlayer = wgp.wrapPlayer(player);
            RegionContainer container = wg.getPlatform().getRegionContainer();

            // TODO: Maybe use new Spatial Queries api for performance reasons?

            RegionManager manager = container.get(BukkitAdapter.adapt(world));
            ApplicableRegionSet ars = manager.getApplicableRegions(BlockVector3.at(x, y, z));

            for (ProtectedRegion rg : ars) {
                if (rg.isMember(wgPlayer)) {
                    return true;
                }
            }

            // TODO: Also allow a list of visible regions to be configured
            return ars.queryState(wgPlayer, Flags.ENDERDRAGON_BLOCK_DAMAGE) == StateFlag.State.DENY;
        }
    }
}
