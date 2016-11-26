package de.pro_crafting.worldfuscator.Core;

import com.comphenix.example.State;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class BlockTranslater {
    private Configuration configuration;

    public BlockTranslater() {
    }

    public BlockTranslater(Configuration configuration) {
        this.configuration = configuration;
    }

    protected boolean hasRights(Player player, int x, int y, int z, World world) {
        return false;
    }

    public int translateBlockID(World world, int x, int y, int z, Player player, State block) {
        if (configuration.getHideIds().contains(block.getId())) {
            if (!this.hasRights(player, x, y, z, world)) {
                return this.configuration.getObfuscationBlock();
            }
        }
        return block.getId();
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
