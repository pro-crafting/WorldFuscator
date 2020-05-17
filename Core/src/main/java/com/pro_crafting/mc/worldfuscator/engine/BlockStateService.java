package com.pro_crafting.mc.worldfuscator.engine;

import com.pro_crafting.mc.worldfuscator.engine.palette.GlobalPaletteAdapter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockStateService {
    private GlobalPaletteAdapter globalPaletteAdapter = new GlobalPaletteAdapter();

    public IntList getMatchingGlobalPaletteIds(Set<String> blockStateValues) {
        if (blockStateValues == null || blockStateValues.isEmpty()) {
            return new IntArrayList();
        }

        List<Set<String>> requestedStateList = split(blockStateValues);
        return globalPaletteAdapter.getAllStateIds(requestedStateList);
    }

    private List<Set<String>> split(Set<String> blockStateValues) {
        List<Set<String>> splitStateValues = new ArrayList<>();
        for (String blockStateValue : blockStateValues) {
            String normalized = blockStateValue.replace('[', ' ').replace(']', ' ').trim();
            String[] split = normalized.split(",");
            Set<String> splitState = Arrays.stream(split).collect(Collectors.toSet());
            splitStateValues.add(splitState);
        }

        return splitStateValues;
    }
}
