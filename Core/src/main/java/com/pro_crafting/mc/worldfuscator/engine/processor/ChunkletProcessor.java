package com.pro_crafting.mc.worldfuscator.engine.processor;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Process the content of a single 16x16x16 chunklet in a 16x256x16 chunk.
 *
 * @author Kristian
 */
public interface ChunkletProcessor {

    public boolean processChunkletBlockData(Location origin, ByteBuffer buffer, Player player);

    public boolean processChunkletBlockEntities(World world, int chunkX, int chunkZ, List<NbtBase<?>> blockEntities, Player player);
}