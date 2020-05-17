package com.pro_crafting.mc.worldfuscator.engine.palette;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Responsible for providing access to the global palette.
 * The Global Palette is not directly accessible via api code. Therefore, we need a tiny bit of reflection.
 * This class uses reflection. For ease of migration to newer minecraft versions, this class should contain as few lines as possible.
 */
public class GlobalPaletteAdapter {
    private Method getBlock;
    private Method getStates;
    private Method getBlockDataList;
    private Method getCombinedId;
    private Method getRegistryMaterialsIterator;

    private Field iRegistryBlock;

    public GlobalPaletteAdapter() {
        Class<?> craftMagicNumbers = MinecraftReflection.getCraftBukkitClass("util.CraftMagicNumbers");
        try {
            getBlock = craftMagicNumbers.getDeclaredMethod("getBlock", Material.class);

            Class<?> blockClass = MinecraftReflection.getBlockClass();
            getStates = blockClass.getDeclaredMethod("getStates");
            getCombinedId = blockClass.getMethod("getCombinedId", MinecraftReflection.getIBlockDataClass());

            Class<?> blockStateList = MinecraftReflection.getMinecraftClass("BlockStateList");
            getBlockDataList = blockStateList.getDeclaredMethod("a");

            Class<?> registryMaterials = MinecraftReflection.getMinecraftClass("RegistryMaterials");
            getRegistryMaterialsIterator = registryMaterials.getDeclaredMethod("iterator");

            Class<?> iRegistry = MinecraftReflection.getMinecraftClass("IRegistry");
            iRegistryBlock = iRegistry.getDeclaredField("BLOCK");
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all possible state ids of a material from the global palette. For example, a stair has a state id for every rotation it can be in.
     * This id is also used when transfering the state of a block through the network.
     *
     * @param material No further description provided
     * @return all possible state ids, never null
     */
    public IntList getAllStateIds(Material material) {
        try {
            Object block = getBlock.invoke(null, material);
            Object states = getStates.invoke(block);
            Object stateList = getBlockDataList.invoke(states);
            @SuppressWarnings("unchecked")
            ImmutableList<Object> casted = (ImmutableList<Object>) stateList;

            IntList globalPaletteList = new IntArrayList(casted.size());

            for (Object blockData : casted) {
                int id = (int) getCombinedId.invoke(null, blockData);
                globalPaletteList.add(id);
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
            Object o = iRegistryBlock.get(null);
            @SuppressWarnings("unchecked")
            Iterator<Object> blockIterator = (Iterator<Object>)getRegistryMaterialsIterator.invoke(o);

            IntList globalPaletteList = new IntArrayList();
            while (blockIterator.hasNext()) {
                Object block = blockIterator.next();

                Object states = getStates.invoke(block);
                @SuppressWarnings("unchecked")
                ImmutableList<Object> blockDataList =  (ImmutableList<Object>)getBlockDataList.invoke(states);

                for (Object blockData : blockDataList) {
                    for (Set<String> requestedStates : requestedStateList) {
                        for (String requestedState : requestedStates) {
                            if (blockData.toString().contains(requestedState)) {
                                int id = (int) getCombinedId.invoke(null, blockData);
                                globalPaletteList.add(id);
                            }
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
}
