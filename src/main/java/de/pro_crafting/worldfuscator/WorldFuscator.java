package de.pro_crafting.worldfuscator;

import com.comphenix.example.BlockDisguiser;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WorldFuscator extends JavaPlugin {
	private WorldGuardPlugin wg;
	private List<Integer> hideIds;
	
	public void onEnable() {

		new BlockDisguiser(this);
		wg = WorldGuardPlugin.getPlugin(WorldGuardPlugin.class);
		hideIds = new ArrayList<Integer>();
	    hideIds.add(29);
	    hideIds.add(36);
	    hideIds.add(55);
	    hideIds.add(75);
	    hideIds.add(76);
	    hideIds.add(93);
	    hideIds.add(94);
	    hideIds.add(131);
	    hideIds.add(149);
	    hideIds.add(150);
	    hideIds.add(152);
	    hideIds.add(154);
	    hideIds.add(158);
	}
	
	public boolean hasRights(Player player, int x, int y, int z, World world) {
		ApplicableRegionSet ars = wg.getRegionManager(world).getApplicableRegions(new Vector(x, y, z));
		Iterator<ProtectedRegion> it = ars.iterator();
		while(it.hasNext()) {
			ProtectedRegion rg = it.next();
			if (rg.isMember(player.getName()) || rg.isOwner(player.getName())) {
				return true;
			}
		}
		return ars.allows(DefaultFlag.ENABLE_SHOP);
	}
	
	public int translateBlockID(World world, int x, int y, int z, int blockId,
			Player player) {
		if (hideIds.contains(blockId)) {
			if (!this.hasRights(player, x, y, z, world)) {
				return 121;
			}
		}
		return blockId;
	}
}
