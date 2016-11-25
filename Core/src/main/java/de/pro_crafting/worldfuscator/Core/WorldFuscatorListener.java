package de.pro_crafting.worldfuscator.Core;

import de.pro_crafting.region.Region;
import de.pro_crafting.region.events.RegionDomainChangeEvent;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


public class WorldFuscatorListener implements Listener {
    private WorldFuscator plugin;

    public WorldFuscatorListener(WorldFuscator plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleDomainChange(RegionDomainChangeEvent event) {
        if (event.getNewPlayers().size() == event.getOldPlayers().size() &&
                event.getNewPlayers().containsAll(event.getOldPlayers()) &&
                event.getOldPlayers().containsAll(event.getNewPlayers())) {
            return;
        }
        Region updatedRegion = event.getRegion();
        World world = updatedRegion.getWorld();
        final Chunk startChunk = updatedRegion.getMin().toLocation(world).getChunk();
        final Chunk endChunk = updatedRegion.getMax().toLocation(world).getChunk();
        Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
            public void run() {
                for (int cx = startChunk.getX(); cx <= endChunk.getX(); cx++) {
                    for (int cz = startChunk.getZ(); cz <= endChunk.getZ(); cz++) {
                        startChunk.getWorld().refreshChunk(cx, cz);
                    }
                }
            }
        }, 20);
    }
}
