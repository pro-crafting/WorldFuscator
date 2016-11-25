package de.pro_crafting.worldfuscator.Core;

import com.comphenix.example.BlockDisguiser;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class WorldFuscator extends JavaPlugin {
	private List<Integer> hideIds;
	private Configuration configuration;
	private BlockTranslater translater;

	public void onEnable() {
		this.saveDefaultConfig();
		new BlockDisguiser(this);
		this.configuration = new Configuration(this.getConfig());
		Bukkit.getPluginManager().registerEvents(new WorldFuscatorListener(this), this);
	}

	public Configuration getConfiguration() {
		return this.configuration;
	}

	protected void setTranslater(BlockTranslater translater) {
		this.translater = translater;
	}

	public BlockTranslater getTranslater() {
		return translater;
	}
}
