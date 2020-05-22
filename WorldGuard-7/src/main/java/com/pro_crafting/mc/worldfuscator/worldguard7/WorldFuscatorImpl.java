package com.pro_crafting.mc.worldfuscator.worldguard7;

import com.pro_crafting.mc.worldfuscator.Configuration;
import com.pro_crafting.mc.worldfuscator.ConfigurationService;
import com.pro_crafting.mc.worldfuscator.engine.BlockTranslator;
import com.pro_crafting.mc.worldfuscator.engine.WorldFuscatorEngine;
import com.pro_crafting.mc.worldfuscator.engine.WorldRefresher;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.UUID;

public class WorldFuscatorImpl extends JavaPlugin {

    private final WorldFuscatorGuardImpl guard = new WorldFuscatorGuardImpl();
    private BlockTranslator translator = new BlockTranslator();
    private WorldRefresher refresher;

    public void onEnable() {
        ConfigurationService.saveDefaultConfiguration(this, "worldfuscator-config.yml");
        YamlConfiguration yamlConfiguration = ConfigurationService.loadConfigurationFile(this, "worldfuscator-config.yml");
        Configuration configuration = new Configuration(yamlConfiguration);

        translator.updateConfiguration(configuration, guard);
        refresher = new WorldRefresher(this, translator);

        WorldFuscatorEngine engine = new WorldFuscatorEngine(this, translator);
        engine.start();
    }

    public void updateRegion(World world, String id, Collection<UUID> oldMembers, Collection<UUID> newMembers) {
        if (translator.getConfiguration().isDebugEnabled()) {
            Bukkit.getLogger().info("Chunk refresh of region: " + id);
        }

        WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager rm = container.get(BukkitAdapter.adapt(world));
        ProtectedRegion region = rm.getRegion(id);

        refresher.updateArea(world,
                BukkitAdapter.adapt(world, region.getMinimumPoint()),
                BukkitAdapter.adapt(world, region.getMaximumPoint()),
                oldMembers,
                newMembers
        );
    }
}
