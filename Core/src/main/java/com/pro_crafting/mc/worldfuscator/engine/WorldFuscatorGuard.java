package com.pro_crafting.mc.worldfuscator.engine;

import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class WorldFuscatorGuard {
    public boolean hasAreaRights(Player player, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,  World world) {
        return false;
    }

    public abstract boolean hasRights(Player player, int x, int y, int z, World world);
}
