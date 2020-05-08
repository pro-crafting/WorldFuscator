package com.pro_crafting.mc.worldfuscator.engine.palette;

import com.pro_crafting.mc.worldfuscator.VarIntUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public void replace(IntList globalPaletteIds, Integer globalPaletteId) {
        Integer paletteIndex = translate(globalPaletteId);

        if (paletteIndex == null) {
            // TODO: Fuscate even if this block is not existent
            return;
        }

        for (int globalId : globalPaletteIds) {
            int localPaletteIndex = globalPaletteIdToPaletteIndex.get(globalId);
            globalPaletteIdToPaletteIndex.put(0, localPaletteIndex);
        }
    }

    public void write(ByteBuffer buffer) {
        VarIntUtil.serializeVarInt(buffer, globalPaletteIdToPaletteIndex.size());

        Map<Integer, Integer> integerIntegerMap = sortByValue(globalPaletteIdToPaletteIndex);
        for (Integer globalPaletteIndex : integerIntegerMap.keySet()) {
            VarIntUtil.serializeVarInt(buffer, globalPaletteIndex);
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
