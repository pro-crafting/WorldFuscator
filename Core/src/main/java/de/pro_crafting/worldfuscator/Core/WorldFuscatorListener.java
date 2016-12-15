package de.pro_crafting.worldfuscator.Core;

import de.pro_crafting.region.Region;
import de.pro_crafting.region.events.RegionDomainChangeEvent;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;


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

        if (this.plugin.getConfiguration().isDebugEnabled()) {
            Bukkit.getLogger().info("Chunk refresh of region: " + event.getRegion().getId());
            Bukkit.getLogger().info("Old players: " + Arrays.toString(event.getOldPlayers().toArray()));
            Bukkit.getLogger().info("New players: " + Arrays.toString(event.getNewPlayers().toArray()));
        }

        Region updatedRegion = event.getRegion();
        World world = updatedRegion.getWorld();
        final Chunk startChunk = updatedRegion.getMin().toLocation(world).getChunk();
        final Chunk endChunk = updatedRegion.getMax().toLocation(world).getChunk();
        final boolean debugEnabled = plugin.getConfiguration().isDebugEnabled();
        Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
            public void run() {
                for (int cx = startChunk.getX(); cx <= endChunk.getX(); cx++) {
                    for (int cz = startChunk.getZ(); cz <= endChunk.getZ(); cz++) {
                        if (debugEnabled) {
                            Bukkit.getLogger().info("Refreshing Chunk: " + cx + "|" + cz);
                        }
                        startChunk.getWorld().refreshChunk(cx, cz);
                    }
                }
            }
        }, 20);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handlePlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!event.hasItem()) {
            return;
        }

        ItemStack item = event.getItem();

        if (item.getType() != Material.BLAZE_ROD) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("worldfuscator.debug.toggle")) {
            return;
        }

        boolean debugEnabled = this.plugin.getConfiguration().isDebugEnabled();
        debugEnabled = !debugEnabled;
        this.plugin.getConfiguration().setDebugEnabled(debugEnabled);

        player.sendMessage("[WorldFuscator] Debug ist nun auf " + debugEnabled);
    }
}
