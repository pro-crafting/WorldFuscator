package com.pro_crafting.mc.worldfuscator.engine.guard;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Wrapper for a single @{@link WorldFuscatorGuard}, adding debug output to every call.
 */
public class WorldFuscatorGuardDebuggingWrapper implements WorldFuscatorGuard {
    private WorldFuscatorGuard delegate;

    @Override
    public boolean hasAreaRights(Player player, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, World world) {
        boolean hasAreaRights = delegate.hasAreaRights(player, minX, minY, minZ, maxX, maxY, maxZ, world);

        if (!hasAreaRights) {
            Bukkit.getLogger().info("No Rights: Translation for min:" + minX + "|" + minY + "|" + minZ + " max:" + maxX + "|" + maxY + "|" + maxZ + " for " + player.getName());
        } else {
            Bukkit.getLogger().info("Passed: Translation for min:" + minX + "|" + minY + "|" + minZ + " max:" + maxX + "|" + maxY + "|" + maxZ + " for " + player.getName());
        }

        return hasAreaRights;
    }

    @Override
    public boolean hasRights(Player player, int x, int y, int z, World world) {
        boolean hasRights = delegate.hasRights(player, x, y, z, world);

        if (!hasRights) {
            Bukkit.getLogger().info("No Rights: Translation for " + x + "|" + y + "|" + z + " for " + player.getName());
        } else {
            Bukkit.getLogger().info("Passed: Translation for " + x + "|" + y + "|" + z + " for " + player.getName());
        }

        return hasRights;
    }
}
