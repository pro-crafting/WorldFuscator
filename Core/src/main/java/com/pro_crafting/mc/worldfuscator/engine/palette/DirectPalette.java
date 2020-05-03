package com.pro_crafting.mc.worldfuscator.engine.palette;

import java.util.Collection;

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
    public boolean containsAny(Collection<Integer> materials) {
        return true;
    }

    @Override
    public boolean contains(Integer globalPaletteId) {
        return true;
    }

    @Override
    public Integer searchAnyNonMatching(Collection<Integer> globalPaletteIds) {
        // Should never be called anyways..
        return -1;
    }

    @Override
    public Collection<Integer> translate(Collection<Integer> materials) {
        return materials;
    }

    @Override
    public Integer translate(Integer globalPaletteId) {
        return globalPaletteId;
    }
}
