package net.myplayplanet.worldfuscator.Core.palette;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import org.bukkit.Material;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Responsible for providing access to the global palette.
 * The Global Palette is not directly accessible via api code. Therefore, we need a tiny bit of reflection.
 * This class uses reflection. For ease of migration to newer minecraft versions, this class should contain as few lines as possible.
 */
public class GlobalPaletteAdapter {
    private static final Multimap<Material, Integer> materialToGlobalPaletteId = ArrayListMultimap.create();

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
     * Get the id of a material from the global palette. This id is also used when transfering the state of a block through the network.
     * @param material
     * @return
     */
    public Collection<Integer> getAllStateIds(Material material) {
        if (!materialToGlobalPaletteId.containsKey(material)) {
            try {
                Object block = getBlock.invoke(null, material);
                Object states = getStates.invoke(block);
                Object stateList = getStateList.invoke(states);
                ImmutableList<Object> casted = (ImmutableList<Object>)stateList;

                for (Object blockData : casted) {
                    int id = (int) getCombinedId.invoke(block, blockData);
                    materialToGlobalPaletteId.put(material, id);
                }

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return materialToGlobalPaletteId.get(material);
    }
}
