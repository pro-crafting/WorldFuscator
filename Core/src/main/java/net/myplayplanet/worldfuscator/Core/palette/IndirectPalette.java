package net.myplayplanet.worldfuscator.Core.palette;

import net.myplayplanet.worldfuscator.Core.VarIntUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndirectPalette implements Palette {
    private Map<Integer, Integer> globalPaletteIdToPaletteIndex = new HashMap<>();

    public IndirectPalette(ByteBuffer buffer) {
        int paletteLength = VarIntUtil.deserializeVarInt(buffer);

        for (int sectionIndex = 0; sectionIndex < paletteLength; sectionIndex++) {
            int globalPaletteId = VarIntUtil.deserializeVarInt(buffer);
            globalPaletteIdToPaletteIndex.put(globalPaletteId, sectionIndex);
        }

    }

    @Override
    public boolean containsAny(Collection<Integer> globalPaletteIds) {
        for (Integer paletteId : globalPaletteIds) {
            if (globalPaletteIdToPaletteIndex.containsKey(paletteId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(Integer globalPaletteId) {
        return globalPaletteIdToPaletteIndex.containsKey(globalPaletteId);
    }

    @Override
    public Integer searchAnyNonMatching(Collection<Integer> globalPaletteIds) {
        for (Map.Entry<Integer, Integer> globalPaleteIdPaleteId : globalPaletteIdToPaletteIndex.entrySet()) {
            if (!globalPaletteIds.contains(globalPaleteIdPaleteId.getKey())) {
                return globalPaleteIdPaleteId.getValue();
            }
        }
        return -1;
    }

    @Override
    public Collection<Integer> translate(Collection<Integer> globalPaletteIds) {
        List<Integer> paletteIndizes = new ArrayList<>();

        for (Integer globalId : globalPaletteIds) {
            paletteIndizes.add(globalPaletteIdToPaletteIndex.get(globalId));
        }

        return paletteIndizes;
    }

    @Override
    public Integer translate(Integer globalPaletteId) {
        return globalPaletteIdToPaletteIndex.get(globalPaletteId);
    }
}
