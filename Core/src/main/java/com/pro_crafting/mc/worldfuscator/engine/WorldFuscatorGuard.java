package com.pro_crafting.mc.worldfuscator.engine;

import org.bukkit.World;
import org.bukkit.entity.Player;

public interface WorldFuscatorGuard {
    public default boolean hasAreaRights(Player player, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,  World world) {
        return false;
    }

    public boolean hasRights(Player player, int x, int y, int z, World world);

    public default boolean isThreadSafe() {
        return true;
    }
}
