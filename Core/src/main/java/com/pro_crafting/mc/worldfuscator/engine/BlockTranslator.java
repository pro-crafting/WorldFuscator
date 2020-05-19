package com.pro_crafting.mc.worldfuscator.engine;

import com.pro_crafting.mc.worldfuscator.Configuration;
import com.pro_crafting.mc.worldfuscator.engine.guard.WorldFuscatorGuard;
import com.pro_crafting.mc.worldfuscator.engine.guard.WorldFuscatorGuardDebuggingWrapper;
import com.pro_crafting.mc.worldfuscator.engine.palette.BlockFilterAdapter;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Bukkit;

import java.util.Collections;

public class BlockTranslator {

    private BlockFilterAdapter blockFilterAdapter = new BlockFilterAdapter();
    private WorldFuscatorGuard guard;

    private Configuration configuration;
    private IntSet hiddenGlobalPaletteIds = new IntOpenHashSet();
    private int preferedObfuscationGlobalPaletteId;

    public BlockTranslator() {
    }

    public void updateConfiguration(Configuration configuration, WorldFuscatorGuard guard) {
        this.configuration = configuration;
        this.guard = guard;
        this.updatePaletteIds();

        if (this.configuration != null && this.configuration.isDebugEnabled()) {
            this.guard = new WorldFuscatorGuardDebuggingWrapper(guard);
        }
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

    public Configuration getConfiguration() {
        return this.configuration;
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
}
