package com.pro_crafting.mc.worldfuscator.engine.palette;


import it.unimi.dsi.fastutil.ints.IntList;

/**
 * The direct palette is a 1:1 mapping to the global palette
 */
public class DirectPalette implements Palette {
    /**
     * Uses the global palette, therefore always true
     *
     * @param materials materials to search for
     * @return No further description provided
     */
    @Override
    public boolean containsAny(IntList materials) {
        return true;
    }

    @Override
    public boolean contains(int globalPaletteId) {
        return true;
    }

    @Override
    public int searchAnyNonMatching(IntList globalPaletteIds) {
        // Should never be called anyways..
        return -1;
    }

    @Override
    public IntList translate(IntList materials) {
        return materials;
    }

    @Override
    public int translate(int globalPaletteId) {
        return globalPaletteId;
    }
}
