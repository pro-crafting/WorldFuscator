package com.pro_crafting.mc.worldfuscator;

import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSReflection {
    public static Method CRAFTMAGIGNUMBERS_GET_BLOCK;
    public static Method BLOCK_GET_STATES;
    public static Method BLOCK_GET_BY_COMBINED_ID;
    public static Method BLOCK_GET_COMBINED_ID;
    public static Method BLOCKSTATELIST_GET_BLOCK_DATA_LIST;
    public static Method REGISTRYMATERIAL_GET_ITERATOR;

    public static Field IREGISTRY_BLOCK;

    static {
        Class<?> craftMagicNumbers = MinecraftReflection.getCraftBukkitClass("util.CraftMagicNumbers");
        try {
            CRAFTMAGIGNUMBERS_GET_BLOCK = craftMagicNumbers.getDeclaredMethod("getBlock", Material.class);

            Class<?> blockClass = MinecraftReflection.getBlockClass();
            BLOCK_GET_STATES = blockClass.getDeclaredMethod("getStates");
            BLOCK_GET_BY_COMBINED_ID = blockClass.getDeclaredMethod("getByCombinedId", int.class);
            BLOCK_GET_COMBINED_ID = blockClass.getMethod("getCombinedId", MinecraftReflection.getIBlockDataClass());

            Class<?> blockStateList = MinecraftReflection.getMinecraftClass("BlockStateList");
            BLOCKSTATELIST_GET_BLOCK_DATA_LIST = blockStateList.getDeclaredMethod("a");

            Class<?> registryMaterials = MinecraftReflection.getMinecraftClass("RegistryMaterials");
            REGISTRYMATERIAL_GET_ITERATOR = registryMaterials.getDeclaredMethod("iterator");

            Class<?> iRegistry = MinecraftReflection.getMinecraftClass("IRegistry");
            IREGISTRY_BLOCK = iRegistry.getDeclaredField("BLOCK");
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private NMSReflection() { }

    /**
     * Gets the combined id from an nms blockData using the block registry
     * @param blockData
     * @return
     */
    public static int getCombinedId(Object blockData) throws InvocationTargetException, IllegalAccessException {
        return (int) BLOCK_GET_COMBINED_ID.invoke(null, blockData);
    }

    /**
     * Get the block data from the block registry by id
     * @return
     */
    public static Object getFromId(int id) throws IllegalAccessException, InvocationTargetException {
        return BLOCK_GET_BY_COMBINED_ID.invoke(null, id);
    }
}
