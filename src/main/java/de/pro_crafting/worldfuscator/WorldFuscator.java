package de.pro_crafting.worldfuscator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.example.BlockDisguiser;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

public class WorldFuscator extends JavaPlugin{
	RegionQuery query;
	
	public void onEnable() {
		new BlockDisguiser(this);
		this.query = ((WorldGuardPlugin)Bukkit.getPluginManager().getPlugin("WorldGuard")).getRegionContainer().createQuery();
	}
	
	public boolean hasRights(Player player, int x, int y, int z, World world) {
		Location loc = new Location(world, x, y, z);
		if (x < -1540 || x > -1490 || y < 4 || y > 17 || z < -691 || z > -641) {
			world.getBlockAt(x, y, z).setType(Material.MELON_BLOCK);
		}
		return query.testBuild(loc, player) ||
				!query.testState(loc, player, DefaultFlag.ENABLE_SHOP);
	}
}
