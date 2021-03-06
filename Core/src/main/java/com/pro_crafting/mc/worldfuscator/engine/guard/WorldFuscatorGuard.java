package com.pro_crafting.mc.worldfuscator.engine.guard;

import org.bukkit.World;
import org.bukkit.entity.Player;

public interface WorldFuscatorGuard {
    default boolean hasAreaRights(Player player, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,  World world) {
        return false;
    }

    boolean hasRights(Player player, int x, int y, int z, World world);
}
