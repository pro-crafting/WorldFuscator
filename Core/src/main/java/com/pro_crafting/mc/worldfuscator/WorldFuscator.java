package com.pro_crafting.mc.worldfuscator;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.pro_crafting.mc.worldfuscator.engine.WorldFuscatorEngine;
import com.pro_crafting.mc.worldfuscator.engine.BlockTranslator;
import com.pro_crafting.mc.worldfuscator.engine.guard.WorldFuscatorGuard;
import com.pro_crafting.mc.worldfuscator.engine.WorldRefresher;
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

        translator = new BlockTranslator();
        translator.setConfiguration(this.configuration);
        translator.setWorldFuscatorGuard(getWorldFuscatorGuard());
        WorldFuscatorEngine.start(this);
        this.worldRefresher = new WorldRefresher(this);
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    protected void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected void prepareDefaultConfiguration() {
        this.saveDefaultConfig();
        this.setConfiguration(new Configuration(this.getConfig()));
    }

    public BlockTranslator getTranslator() {
        return translator;
    }

    public WorldRefresher getWorldRefresher() {
        return worldRefresher;
    }

    public abstract WorldFuscatorGuard getWorldFuscatorGuard();
}
