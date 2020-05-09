package com.pro_crafting.mc.worldfuscator.engine.palette;

import it.unimi.dsi.fastutil.ints.IntSet;

public interface Palette {
    /**
     * Checks if this palette contains any of the specified global palette block state identifier
     *
     * @param globalPaletteIds global palette ids to search for
     * @return true if any of the global palette ids found in palette, otherwise false
     */
    boolean containsAny(IntSet globalPaletteIds);

    /**
     * Checks if this palette contains the specified global palette block state identifier
     *
     * @param globalPaletteId No further description provided
     * @return true if this palette contains the specified global palette block state identifier, otherwise false
     */
    boolean contains(int globalPaletteId);

    /**
     * Searches for any palette index whose global palette id does not exists in the given list of ids
     *
     * @param globalPaletteIds No further description provided
     * @return No further description provided
     */
    int searchAnyNonMatching(IntSet globalPaletteIds);

    /**
     * Translates global palette ids to its corresponding palette index. The palette index is always used in the chunk packet.
     *
     * @param globalPaletteIds global palette ids to translate
     * @return Collection of palette indexes
     */
    IntSet translate(IntSet globalPaletteIds);

    /**
     * Translate a global palette id to the corresponding pallte index. The palette index is always used in the chunk packet.
     *
     * @param globalPaletteId No further description provided
     * @return palette index
     */
    int translate(int globalPaletteId);
}
