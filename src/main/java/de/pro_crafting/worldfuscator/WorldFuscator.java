package de.pro_crafting.worldfuscator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.example.BlockDisguiser;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

public class WorldFuscator extends JavaPlugin {
	WorldGuardPlugin wg;

	public void onEnable() {

		new BlockDisguiser(this);
		wg = WorldGuardPlugin.getPlugin(WorldGuardPlugin.class);
	}
	
	public boolean hasRights(Player player, int x, int y, int z, World world) {
		Location loc = new Location(world, x, y, z);
		ApplicableRegionSet ars = wg.getRegionManager(world).getApplicableRegions(loc);
		return ars.canBuild(wg.wrapPlayer(player))
				|| ars.allows(DefaultFlag.ENABLE_SHOP);
	}
}
