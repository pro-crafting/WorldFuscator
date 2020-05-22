package com.pro_crafting.mc.worldfuscator.worldguard7;

import com.pro_crafting.mc.worldfuscator.engine.guard.WorldFuscatorGuard;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldFuscatorGuardImpl implements WorldFuscatorGuard {

    WorldGuardPlugin wgp = WorldGuardPlugin.inst();
    WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

    // TODO: Clear this map on reload
    // Reload of plugin is currently not implemented, therefore we do not need to worry about it
    private final Map<String, RegionManager> regionManagers = new ConcurrentHashMap<>();

    @Override
    public boolean hasAreaRights(Player player, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, World world) {
        LocalPlayer wgPlayer = wgp.wrapPlayer(player);

        // TODO: Maybe use new Spatial Queries api for performance reasons?

        ProtectedRegion region = new ProtectedCuboidRegion("CHUNKS", BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));

        RegionManager manager = getRegionManager(world);

        ApplicableRegionSet ars = manager.getApplicableRegions(region);

        if (ars.isMemberOfAll(wgPlayer)) {
            return true;
        }

        // TODO: Allow a list of visible regions to be configured
        // If the player is on no region, he can see everything
        return ars.size() == 0;
    }

    @Override
    public boolean hasRights(Player player, int x, int y, int z, World world) {
        LocalPlayer wgPlayer = wgp.wrapPlayer(player);

        // TODO: Maybe use new Spatial Queries api for performance reasons?

        RegionManager manager = getRegionManager(world);
        ApplicableRegionSet ars = manager.getApplicableRegions(BlockVector3.at(x, y, z));

        if (hasRights(ars, wgPlayer)) {
            return true;
        }

        // TODO: Allow a list of visible regions to be configured
        // If the player is on no region, he can see everything
        return ars.size() == 0;
    }

    /**
     * If the player is not a member of any of these regions, he has no rights
     *
     * @return
     */
    protected boolean hasRights(ApplicableRegionSet ars, LocalPlayer wgPlayer) {
        if (ars.size() == 0) {
            return true;
        }

        ProtectedRegion highest = ars.iterator().next();
        return highest.isMember(wgPlayer);
    }

    protected RegionManager getRegionManager(World world) {
        RegionManager manager = regionManagers.get(world.getName());
        if (manager == null) {
            manager = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            regionManagers.put(world.getName(), manager);
        }

        return manager;
    }
}