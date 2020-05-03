package com.pro_crafting.mc.worldfuscator.worldguard7;

import com.pro_crafting.mc.worldfuscator.WorldFuscator;
import com.pro_crafting.mc.worldfuscator.engine.WorldFuscatorGuard;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class WorldFuscatorImpl extends WorldFuscator {

    public void onEnable() {
        super.onEnable();
        super.getTranslator().setWorldFuscatorGuard(new WorldFuscatorGuardImpl());
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

    private class WorldFuscatorGuardImpl extends WorldFuscatorGuard {

        WorldGuardPlugin wgp = WorldGuardPlugin.inst();
        WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        @Override
        public boolean hasChunkRights(Player player, int chunkX, int chunkY, int chunkZ, World world) {
            LocalPlayer wgPlayer = wgp.wrapPlayer(player);
            RegionContainer container = wg.getPlatform().getRegionContainer();

            // TODO: Maybe use new Spatial Queries api for performance reasons?

            ProtectedRegion region = new ProtectedCuboidRegion("CHUNKS", BlockVector3.at(chunkX*16, chunkY*16, chunkZ*16), BlockVector3.at(chunkX*16+15, chunkY*16+15, chunkZ*16+15));

            RegionManager manager = container.get(BukkitAdapter.adapt(world));
            ApplicableRegionSet ars = manager.getApplicableRegions(region);

            for (ProtectedRegion rg : ars) {
                if (rg.isMember(wgPlayer)) {
                    return true;
                }
            }

            // TODO: Allow a list of visible regions to be configured
            // If the player is on no region, he can see everything
            return ars.size() == 0;
        }

        @Override
        public boolean hasRights(Player player, int x, int y, int z, World world) {
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

            // TODO: Allow a list of visible regions to be configured
            // If the player is on no region, he can see everything
            return ars.size() == 0;
        }
    }
}
