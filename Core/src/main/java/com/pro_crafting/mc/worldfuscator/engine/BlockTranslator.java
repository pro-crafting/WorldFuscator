package com.pro_crafting.mc.worldfuscator.engine;

import com.pro_crafting.mc.worldfuscator.Configuration;
import com.pro_crafting.mc.worldfuscator.engine.palette.GlobalPaletteAdapter;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;

public class BlockTranslator {

    private GlobalPaletteAdapter globalPaletteAdapter = new GlobalPaletteAdapter();
    private BlockFilterAdapter blockFilterAdapter = new BlockFilterAdapter();
    private WorldFuscatorGuard guard;

    private Configuration configuration;
    private IntSet hiddenGlobalPaletteIds = new IntOpenHashSet();
    private int preferedObfuscationGlobalPaletteId;

    public BlockTranslator() {
    }

    public BlockTranslator(Configuration configuration) {
        setConfiguration(configuration);
    }

    private void updatePaletteIds() {
        // Initialize list with global palette ids to hide
        IntList matchingHidenBlockStateGlobalPaletteIds = blockFilterAdapter.getMatchingGlobalPaletteIds(configuration.getHiddenMaterialFilters());
        hiddenGlobalPaletteIds.addAll(matchingHidenBlockStateGlobalPaletteIds);

        IntList matchingGlobalPaletteIds = blockFilterAdapter.getMatchingGlobalPaletteIds(Collections.singleton(configuration.getPreferredObfuscationFilter()));
        if (!matchingGlobalPaletteIds.isEmpty()) {
            preferedObfuscationGlobalPaletteId = matchingGlobalPaletteIds.getInt(0);
        }
        if (configuration.isDebugEnabled()) {
            Bukkit.getLogger().info("Chosen Global Palette Id: " + preferedObfuscationGlobalPaletteId + " as prefered obfuscation block state");
        }
    }

    public boolean needsTranslation(World world, int x, int y, int z, Player player) {
        if (!guard.hasRights(player, x, y, z, world)) {
            if (configuration.isDebugEnabled()) {
                Bukkit.getLogger().info("No Rights: Translation for " + x + "|" + y + "|" + z + " for " + player.getName());
            }
            return true;
        }
        if (configuration.isDebugEnabled()) {
            Bukkit.getLogger().info("Passed: Translation for " + x + "|" + y + "|" + z + " for " + player.getName());
        }
        return false;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        updatePaletteIds();
    }

    public IntSet getHiddenGlobalPaletteIds() {
        return hiddenGlobalPaletteIds;
    }

    public int getPreferedObfuscationGlobalPaletteId() {
        return preferedObfuscationGlobalPaletteId;
    }

    public WorldFuscatorGuard getWorldFuscatorGuard() {
        return guard;
    }

    public void setWorldFuscatorGuard(WorldFuscatorGuard guard) {
        this.guard = guard;
    }
}
