package com.pro_crafting.mc.worldfuscator.engine;

import com.pro_crafting.mc.worldfuscator.engine.palette.GlobalPaletteAdapter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockFilterAdapter {
    private GlobalPaletteAdapter globalPaletteAdapter = new GlobalPaletteAdapter();

    public IntList getMatchingGlobalPaletteIds(Collection<String> filters) {
        IntList globalPaletteIdList = new IntArrayList();
        if (filters == null || filters.isEmpty()) {
            return globalPaletteIdList;
        }

        List<Set<String>> requestedStateList = new ArrayList<>();
        for (String filter : filters) {
            Material material = Material.matchMaterial(filter);
            if (material != null) {
                globalPaletteIdList.addAll(globalPaletteAdapter.getAllStateIds(material));
                continue;
            }

            try {
                BlockData blockData = Bukkit.createBlockData(filter);
                globalPaletteIdList.add(NMSReflection.getCombinedId(blockData));
                continue;
            } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
                // No valid data string in format minecraft:material[waterlogged=true]
                // It may be a simple filter
            }

            // Might be a simple filter, add it for later checking
            requestedStateList.add(split(filter));
        }

        if (!requestedStateList.isEmpty()) {
            globalPaletteIdList.addAll(globalPaletteAdapter.getAllStateIds(requestedStateList));
        }

        return globalPaletteIdList;
    }

    private Set<String> split(String blockStateValue) {
        String normalized = blockStateValue.replace('[', ' ').replace(']', ' ').trim();
        String[] split = normalized.split(",");
        Set<String> splitState = Arrays.stream(split).collect(Collectors.toSet());
        return splitState;
    }
}
