package com.pro_crafting.mc.worldfuscator.engine;

import com.comphenix.protocol.events.PacketContainer;
import com.pro_crafting.mc.worldfuscator.VarIntUtil;
import com.pro_crafting.mc.worldfuscator.engine.processor.ChunkletProcessor;
import org.bukkit.Location;
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
    public static boolean isDebugEnabled;
    public static File dataFolder;

    private void writeDebugChunkFiles(ChunkPacketData chunkPacketData) {
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
        writeDebugChunkFiles(chunkData);

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
            // The bitmask is from the ground up, with the least significant bit being the bottum
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
