package com.pro_crafting.mc.worldfuscator.engine;

import com.pro_crafting.mc.worldfuscator.Configuration;
import com.pro_crafting.mc.worldfuscator.engine.palette.GlobalPaletteAdapter;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;

public class BlockTranslator {

    private GlobalPaletteAdapter globalPaletteAdapter = new GlobalPaletteAdapter();
    private BlockStateService blockDataService = new BlockStateService();
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
        Set<Material> hideMaterials = configuration.getHideMaterials();
        for (Material hideMaterial : hideMaterials) {
            hiddenGlobalPaletteIds.addAll(globalPaletteAdapter.getAllStateIds(hideMaterial));
        }

        IntList matchingHidenBlockStateGlobalPaletteIds = blockDataService.getMatchingGlobalPaletteIds(configuration.getHideBlockData());
        hiddenGlobalPaletteIds.addAll(matchingHidenBlockStateGlobalPaletteIds);

        // TODO: Use default block state instead of any
        // But in theory, the first block state should be the default state
        preferedObfuscationGlobalPaletteId = globalPaletteAdapter.getAllStateIds(configuration.getPreferredObfuscationMaterial()).getInt(0);
        if (configuration.isDebugEnabled()) {
            System.out.println("Chosen Global Palette Id: " + preferedObfuscationGlobalPaletteId + " as prefered obfuscation material");
        }
    }

    public boolean needsTranslation(World world, int x, int y, int z, Player player) {
        if (!guard.hasRights(player, x, y, z, world)) {
            if (configuration.isDebugEnabled()) {
                System.out.println("No Rights: Translation for " + x + "|" + y + "|" + z + " for " + player.getName());
            }
            return true;
        }
        if (configuration.isDebugEnabled()) {
            System.out.println("Passed: Translation for " + x + "|" + y + "|" + z + " for " + player.getName());
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
