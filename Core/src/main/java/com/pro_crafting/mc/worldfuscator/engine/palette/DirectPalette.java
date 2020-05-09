package com.pro_crafting.mc.worldfuscator.engine.palette;


import it.unimi.dsi.fastutil.ints.IntSet;

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
    public boolean containsAny(IntSet materials) {
        return true;
    }

    @Override
    public boolean contains(int globalPaletteId) {
        return true;
    }

    @Override
    public int searchAnyNonMatching(IntSet globalPaletteIds) {
        // Should never be called anyways..
        return -1;
    }

    @Override
    public IntSet translate(IntSet materials) {
        return materials;
    }

    @Override
    public int translate(int globalPaletteId) {
        return globalPaletteId;
    }
}
