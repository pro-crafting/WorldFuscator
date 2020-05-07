package com.pro_crafting.mc.worldfuscator.engine.palette;

import com.pro_crafting.mc.worldfuscator.VarIntUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.nio.ByteBuffer;

public class IndirectPalette implements Palette {
    private Int2IntMap globalPaletteIdToPaletteIndex = new Int2IntOpenHashMap();

    public IndirectPalette(ByteBuffer buffer) {
        int paletteLength = VarIntUtil.deserializeVarInt(buffer);

        for (int sectionIndex = 0; sectionIndex < paletteLength; sectionIndex++) {
            int globalPaletteId = VarIntUtil.deserializeVarInt(buffer);
            globalPaletteIdToPaletteIndex.put(globalPaletteId, sectionIndex);
        }

    }

    @Override
    public boolean containsAny(IntList globalPaletteIds) {
        for (int i = 0; i < globalPaletteIds.size(); i++) {
            if (globalPaletteIdToPaletteIndex.containsKey(globalPaletteIds.getInt(i))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean contains(int globalPaletteId) {
        return globalPaletteIdToPaletteIndex.containsKey(globalPaletteId);
    }

    @Override
    public int searchAnyNonMatching(IntList globalPaletteIds) {
        for (Int2IntMap.Entry next : globalPaletteIdToPaletteIndex.int2IntEntrySet()) {
            if (!globalPaletteIds.contains(next.getIntKey())) {
                return next.getIntValue();
            }
        }
        return -1;
    }

    @Override
    public IntList translate(IntList globalPaletteIds) {

        IntList paletteIndizes = new IntArrayList();

        for (int i = 0; i < globalPaletteIds.size(); i++) {
            if (globalPaletteIdToPaletteIndex.containsKey(globalPaletteIds.getInt(i))) {
                paletteIndizes.add(globalPaletteIdToPaletteIndex.get(globalPaletteIds.getInt(i)));
            }
        }

        return paletteIndizes;
    }

    @Override
    public int translate(int globalPaletteId) {
        return globalPaletteIdToPaletteIndex.get(globalPaletteId);
    }
}
