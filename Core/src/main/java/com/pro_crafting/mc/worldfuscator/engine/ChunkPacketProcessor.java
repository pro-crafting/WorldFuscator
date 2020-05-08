package com.pro_crafting.mc.worldfuscator.engine;

import com.comphenix.protocol.events.PacketContainer;
import com.pro_crafting.mc.worldfuscator.VarIntUtil;
import com.pro_crafting.mc.worldfuscator.engine.processor.ChunkletProcessor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Used to process a chunk.
 *
 * @author Kristian
 */
public class ChunkPacketProcessor {

    // Useful Minecraft constants
    protected static final int BYTES_PER_NIBBLE_PART = 2048;
    protected static final int CHUNK_SEGMENTS = 16;
    protected static final int NIBBLES_REQUIRED = 4;
    protected static final int BIOME_ARRAY_LENGTH = 256;

    public static boolean isDebugEnabled;
    public static File dataFolder;

    private static void writeDebugChunkFiles(ChunkPacketData chunkPacketData) {
        if (!isDebugEnabled) {
            return;
        }

        File chunkFolder = new File(dataFolder, "chunks");
        chunkFolder.mkdir();

        File worldChunkFolder = new File(chunkFolder, chunkPacketData.world.getName());
        worldChunkFolder.mkdir();

        try (FileOutputStream fos = new FileOutputStream(
                new File(worldChunkFolder, chunkPacketData.chunkX + "." + chunkPacketData.chunkZ + ".chunk"))) {
            fos.write(chunkPacketData.data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[chunkPacketData.data.length + 2048 + 2048 + 1024];
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.putInt(chunkPacketData.chunkX);
        bb.putInt(chunkPacketData.chunkZ);
        bb.put(chunkPacketData.isFullChunk ? (byte) 0x01 : (byte) 0x00);
        VarIntUtil.serializeVarInt(bb, chunkPacketData.primaryBitMask);
        VarIntUtil.serializeVarInt(bb, chunkPacketData.data.length);
        bb.put(chunkPacketData.data);
        //TODO: Implement Biomes and BlockEntities

        try (FileOutputStream fos = new FileOutputStream(
                new File(worldChunkFolder, chunkPacketData.chunkX + "." + chunkPacketData.chunkZ + ".mcdp"))) {
            fos.write(bb.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PacketContainer process(ChunkPacketData chunkData, ChunkletProcessor processor, Player player, PacketContainer packet) {
        // Compute chunk number
        for (int i = 0; i < CHUNK_SEGMENTS; i++) {
            if ((chunkData.primaryBitMask & (1 << i)) > 0) {
                chunkData.chunkSectionNumber++;
            }
        }

        // There's no sun/moon in the end or in the nether, so Minecraft doesn't sent any skylight information
        // This optimization was added in 1.4.6. Note that ideally you should get this from the "f" (skylight) field.
        int skylightCount = 1;
        if (chunkData.world != null) {
            skylightCount = chunkData.world.getEnvironment() == Environment.NORMAL ? 1 : 0;
        }

        // The total size of a chunk is the number of blocks sent (depends on the number of sections) multiplied by the
        // amount of bytes per block. This last figure can be calculated by adding together all the data parts:
        //   For any block:
        //    * Block ID          -   8 bits per block (byte)
        //    * Block metadata    -   4 bits per block (nibble)
        //    * Block light array -   4 bits per block
        //   If 'worldProvider.skylight' is TRUE
        //    * Sky light array   -   4 bits per block
        //   If the segment has extra data:
        //    * Add array         -   4 bits per block
        //   Biome array - only if the entire chunk (has continous) is sent:
        //    * Biome array       -   256 bytes
        //
        // A section has 16 * 16 * 16 = 4096 blocks.
        int size = BYTES_PER_NIBBLE_PART * (
                (NIBBLES_REQUIRED + skylightCount) * chunkData.chunkSectionNumber) +
                (chunkData.isFullChunk ? BIOME_ARRAY_LENGTH : 0);

        int blockSize = 4096 * chunkData.chunkSectionNumber;

        // Make sure the chunk is loaded
        boolean didFuscate = translate(chunkData, processor, player);
        if (packet != null && didFuscate) {
            PacketContainer clonedPacket = packet.shallowClone();
            clonedPacket.getByteArrays().write(0, chunkData.data);
            clonedPacket.getListNbtModifier().write(0, chunkData.blockEntities);
            return clonedPacket;
        }

        return packet;
    }

    private boolean translate(ChunkPacketData chunkData, ChunkletProcessor processor, Player player) {
        // Loop over 16x16x16 chunks in the 16x256x16 column

        ByteBuffer buffer = ByteBuffer.wrap(chunkData.data);

        boolean didFuscate = false;
        for (int i = 0; i < 16; i++) {
            // If the bitmask indicates this chunk is sent
            if ((chunkData.primaryBitMask & 1 << i) > 0) {

                // The lowest block (in x, y, z) in this chunklet
                Location origin = new Location(chunkData.world, chunkData.chunkX << 4, i * 16, chunkData.chunkZ << 4);
                didFuscate = processor.processChunkletBlockData(origin, buffer, player) || didFuscate;
            }
        }
        chunkData.data = buffer.array();
        didFuscate = processor.processChunkletBlockEntities(chunkData.world, chunkData.chunkX, chunkData.chunkZ, chunkData.blockEntities, player) || didFuscate;
        return didFuscate;
    }
}
