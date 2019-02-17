package net.myplayplanet.worldfuscator.Core;

import com.comphenix.example.BlockDisguiser;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class WorldFuscator extends JavaPlugin {

  private Configuration configuration;
  private BlockTranslator translator;
  private WorldRefresher worldRefresher;

  public void onEnable() {
    MinecraftVersion minecraftVersion = ProtocolLibrary.getProtocolManager().getMinecraftVersion();

    // Only activate this plugin for minecraft 1.13 and up
    if (MinecraftVersion.AQUATIC_UPDATE.compareTo(minecraftVersion) > 0) {
      Bukkit.getLogger().info("This plugin only works for 1.13.0 and up");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    this.saveDefaultConfig();
    this.configuration = new Configuration(this.getConfig());
    translator.setConfiguration(this.configuration);
    new BlockDisguiser(this);
    Bukkit.getPluginManager().registerEvents(new WorldFuscatorListener(this), this);
    this.worldRefresher = new WorldRefresher(this);
  }

  public Configuration getConfiguration() {
    return this.configuration;
  }

  public BlockTranslator getTranslator() {
    return translator;
  }

  protected void setTranslator(BlockTranslator translator) {
    this.translator = translator;
  }

  public WorldRefresher getWorldRefresher() {
    return worldRefresher;
  }
}
