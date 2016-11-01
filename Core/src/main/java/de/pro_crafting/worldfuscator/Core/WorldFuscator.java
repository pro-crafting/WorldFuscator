package de.pro_crafting.worldfuscator.Core;

import com.comphenix.example.BlockDisguiser;
import com.comphenix.example.State;
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

public abstract class WorldFuscator extends JavaPlugin implements Listener {
	private List<Integer> hideIds;

	public void onEnable() {
		this.saveDefaultConfig();
		new BlockDisguiser(this);
		this.hideIds =this.getConfig().getIntegerList("hidden");
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	protected abstract boolean hasRights(Player player, int x, int y, int z, World world);

	public int translateBlockID(World world, int x, int y, int z, Player player, State block) {
		if (hideIds.contains(block.getId())) {
			if (!this.hasRights(player, x, y, z, world)) {
				return this.getObfuscationBlock();
			}
		}
		return block.getId();
	}

	public int getObfuscationBlock() {
		return 121;
	}

	public List<Integer> getHideIds() {
		return this.hideIds;
	}

	@EventHandler (priority = EventPriority.MONITOR)
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
