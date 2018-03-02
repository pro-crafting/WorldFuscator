package net.myplayplanet.worldfuscator.Core;

import com.comphenix.example.BlockDisguiser;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class WorldFuscator extends JavaPlugin {

  private Configuration configuration;
  private BlockTranslator translater;
  private WorldRefresher worldRefresher;

  public void onEnable() {
    MinecraftVersion minecraftVersion = ProtocolLibrary.getProtocolManager().getMinecraftVersion();

    // Only activate this plugin for minecraft 1.9 and up
    if (MinecraftVersion.BOUNTIFUL_UPDATE.compareTo(minecraftVersion) > 0) {
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    this.saveDefaultConfig();
    this.configuration = new Configuration(this.getConfig());
    translater.setConfiguration(this.configuration);
    new BlockDisguiser(this);
    Bukkit.getPluginManager().registerEvents(new WorldFuscatorListener(this), this);
    this.worldRefresher = new WorldRefresher(this);
  }

  public Configuration getConfiguration() {
    return this.configuration;
  }

  public BlockTranslator getTranslater() {
    return translater;
  }

  protected void setTranslater(BlockTranslator translater) {
    this.translater = translater;
  }

  public WorldRefresher getWorldRefresher() {
    return worldRefresher;
  }
}