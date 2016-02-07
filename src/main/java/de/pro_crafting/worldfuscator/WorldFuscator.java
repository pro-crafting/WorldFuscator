package de.pro_crafting.worldfuscator;

import com.comphenix.example.BlockDisguiser;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.pro_crafting.region.Region;
import de.pro_crafting.region.events.RegionDomainChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class WorldFuscator extends JavaPlugin implements Listener {
	private WorldGuardPlugin wg;
	private List<Integer> hideIds;

	public void onEnable() {
		this.saveDefaultConfig();
		new BlockDisguiser(this);
		wg = WorldGuardPlugin.getPlugin(WorldGuardPlugin.class);
		this.hideIds = this.getConfig().getIntegerList("hidden");
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	public boolean hasRights(Player player, int x, int y, int z, World world) {
		ApplicableRegionSet ars = wg.getRegionManager(world).getApplicableRegions(new Vector(x, y, z));

		for (ProtectedRegion rg : ars) {
			if (rg.isMember(player.getName()) || rg.isOwner(player.getName()) || player.hasPermission("worldfuscator.bypass")) {
				return true;
			}
		}
		return ars.allows(DefaultFlag.ENABLE_SHOP);
	}

	public int translateBlockID(World world, int x, int y, int z, int blockId, Player player) {
		if (hideIds.contains(blockId)) {
			if (!this.hasRights(player, x, y, z, world)) {
				return 121;
			}
		}
		return blockId;
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void handleDomainChange(RegionDomainChangeEvent event) {
		if (event.getNewPlayers().containsAll(event.getOldPlayers())) {
			return;
		}
		Region updatedRegion = event.getRegion();
		World world = updatedRegion.getWorld();
		final Chunk startChunk = updatedRegion.getMin().toLocation(world).getChunk();
		final Chunk endChunk = updatedRegion.getMax().toLocation(world).getChunk();
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
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
