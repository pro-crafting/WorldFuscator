package com.pro_crafting.mc.worldfuscator.engine.palette;

import com.google.common.collect.ImmutableList;
import com.pro_crafting.mc.worldfuscator.NMSReflection;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.Material;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Responsible for providing access to the global palette.
 * The Global Palette is not directly accessible via api code. Therefore, we need a tiny bit of reflection.
 * This class uses reflection. For ease of migration to newer minecraft versions, this class should contain as few lines as possible.
 */
public class GlobalPaletteAdapter {
    /**
     * Get all possible state ids of a material from the global palette. For example, a stair has a state id for every rotation it can be in.
     * This id is also used when transfering the state of a block through the network.
     *
     * @param material No further description provided
     * @param requestedStates Optionally, check if the block data matches all states
     * @return all possible state ids, never null
     */
    public IntList getAllStateIds(Material material, Set<String> requestedStates) {
        try {
            Object block = NMSReflection.CRAFTMAGIGNUMBERS_GET_BLOCK.invoke(null, material);
            Object states = NMSReflection.BLOCK_GET_STATES.invoke(block);
            @SuppressWarnings("unchecked")
            ImmutableList<Object> blockDataList =  (ImmutableList<Object>) NMSReflection.BLOCKSTATELIST_GET_BLOCK_DATA_LIST.invoke(states);

            IntList globalPaletteList = new IntArrayList(blockDataList.size());
            for (Object blockData : blockDataList) {
                if (requestedStates == null || requestedStates.isEmpty() || matches(blockData, requestedStates)) {
                    globalPaletteList.add(NMSReflection.getCombinedId(blockData));
                }
            }

            return globalPaletteList;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return new IntArrayList();
    }

    public IntList getAllStateIds(List<Set<String>> requestedStateList) {
        if (requestedStateList == null || requestedStateList.isEmpty()) {
            return new IntArrayList();
        }

        try {
            Object o = NMSReflection.IREGISTRY_BLOCK.get(null);
            @SuppressWarnings("unchecked")
            Iterator<Object> blockIterator = (Iterator<Object>)NMSReflection.REGISTRYMATERIAL_GET_ITERATOR.invoke(o);

            IntList globalPaletteList = new IntArrayList();
            while (blockIterator.hasNext()) {
                Object block = blockIterator.next();

                Object states = NMSReflection.BLOCK_GET_STATES.invoke(block);
                @SuppressWarnings("unchecked")
                ImmutableList<Object> blockDataList =  (ImmutableList<Object>)NMSReflection.BLOCKSTATELIST_GET_BLOCK_DATA_LIST.invoke(states);

                for (Object blockData : blockDataList) {
                    for (Set<String> requestedStates : requestedStateList) {
                        if (matches(blockData, requestedStates)) {
                            globalPaletteList.add(NMSReflection.getCombinedId(blockData));
                        }
                    }
                }
            }

            return globalPaletteList;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return new IntArrayList();
    }

    /**
     * Check if the Minecraft blockData matches all of the requested block states
     * @param blockData
     * @param requestedStates
     * @return true if the block data matches all states
     */
    private boolean matches(Object blockData, Set<String> requestedStates) {
        for (String requestedState : requestedStates) {
            if (!blockData.toString().contains(requestedState)) {
                return false;
            }
        }

        return true;
    }
}
