package com.pro_crafting.mc.worldfuscator.engine;

import com.pro_crafting.mc.worldfuscator.Configuration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.pro_crafting.mc.worldfuscator.engine.palette.GlobalPaletteAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockTranslator {

    private GlobalPaletteAdapter globalPaletteAdapter = new GlobalPaletteAdapter();

    private Configuration configuration;
    private List<Integer> hiddenGlobalPaletteIds = new ArrayList<>();
    private Integer preferedObfuscationGlobalPaletteId;

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

        // TODO: Use default block state instead of any
        // But in theory, the first block state should be the default state
        preferedObfuscationGlobalPaletteId = globalPaletteAdapter.getAllStateIds(configuration.getPreferredObfuscationMaterial()).iterator().next();
        if (configuration.isDebugEnabled()) {
            System.out.println("Chosen Global Palette Id: " + preferedObfuscationGlobalPaletteId + " as prefered obfuscation material");
        }
    }

    protected boolean hasRights(Player player, int x, int y, int z, World world) {
        return false;
    }

    public boolean needsTranslation(World world, int x, int y, int z, Player player) {
        if (!this.hasRights(player, x, y, z, world)) {
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

    public List<Integer> getHiddenGlobalPaletteIds() {
        return hiddenGlobalPaletteIds;
    }

    public Integer getPreferedObfuscationGlobalPaletteId() {
        return preferedObfuscationGlobalPaletteId;
    }
}
