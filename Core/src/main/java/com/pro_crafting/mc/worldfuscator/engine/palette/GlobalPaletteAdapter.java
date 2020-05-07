package com.pro_crafting.mc.worldfuscator.engine.palette;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.Material;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for providing access to the global palette.
 * The Global Palette is not directly accessible via api code. Therefore, we need a tiny bit of reflection.
 * This class uses reflection. For ease of migration to newer minecraft versions, this class should contain as few lines as possible.
 */
public class GlobalPaletteAdapter {
    private static final Map<Material, IntList> materialToGlobalPaletteId = new HashMap<>();

    private Method getBlock;
    private Method getStates;
    private Method getStateList;
    private Method getCombinedId;

    public GlobalPaletteAdapter() {
        Class<?> craftMagicNumbers = MinecraftReflection.getCraftBukkitClass("util.CraftMagicNumbers");
        try {
            getBlock = craftMagicNumbers.getDeclaredMethod("getBlock", Material.class);

            Class<?> blockClass = MinecraftReflection.getBlockClass();
            getStates = blockClass.getDeclaredMethod("getStates");
            getCombinedId = blockClass.getMethod("getCombinedId", MinecraftReflection.getIBlockDataClass());

            Class<?> blockStateList = MinecraftReflection.getMinecraftClass("BlockStateList");
            getStateList = blockStateList.getDeclaredMethod("a");
        } catch (NoSuchMethodException e) {
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
        if (!materialToGlobalPaletteId.containsKey(material)) {
            try {
                Object block = getBlock.invoke(null, material);
                Object states = getStates.invoke(block);
                Object stateList = getStateList.invoke(states);
                @SuppressWarnings("unchecked")
                ImmutableList<Object> casted = (ImmutableList<Object>) stateList;

                IntList globalPaletteList = new IntArrayList(casted.size());

                for (Object blockData : casted) {
                    int id = (int) getCombinedId.invoke(block, blockData);
                    globalPaletteList.add(id);
                }

                materialToGlobalPaletteId.put(material, globalPaletteList);

                return globalPaletteList;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return materialToGlobalPaletteId.get(material);
    }
}
