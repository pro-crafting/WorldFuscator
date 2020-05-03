package com.pro_crafting.mc.worldfuscator.engine;

import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class WorldFuscatorGuard {
    public boolean hasChunkRights(Player player, int chunkX, int chunkY, int chunkZ, World world) {
        return false;
    }

    public abstract boolean hasRights(Player player, int x, int y, int z, World world);
}
