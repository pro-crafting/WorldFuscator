package com.pro_crafting.mc.worldfuscator.worldguard7;

import com.pro_crafting.mc.worldfuscator.Configuration;
import com.pro_crafting.mc.worldfuscator.engine.guard.WorldFuscatorGuard;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.UUID;

public class WorldFuscatorImpl extends JavaPlugin {

    private final WorldFuscatorGuardImpl guard = new WorldFuscatorGuardImpl();


    public void onEnable() {
        this.saveDefaultConfig();
        this.setConfiguration(new Configuration(this.getConfig()));
        super.onEnable();
    }

    @Override
    public WorldFuscatorGuard getWorldFuscatorGuard() {
        return guard;
    }

    public void updateRegion(World world, String id, Collection<UUID> oldMembers, Collection<UUID> newMembers) {
        if (getConfiguration().isDebugEnabled()) {
            Bukkit.getLogger().info("Chunk refresh of region: " + id);
        }

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
}
