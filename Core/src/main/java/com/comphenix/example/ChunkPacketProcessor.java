package com.comphenix.example;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Used to process a chunk.
 * 
 * @author Kristian
 */
public class ChunkPacketProcessor {
    /**
	 * Process the content of a single 16x16x16 chunklet in a 16x256x16 chunk.
	 * @author Kristian
	 */
	public interface ChunkletProcessor {
		public void processChunklet(Location origin, ByteBuffer buffer, Player player);
	}
	
	// Useful Minecraft constants		
	protected static final int BYTES_PER_NIBBLE_PART = 2048;
	protected static final int CHUNK_SEGMENTS = 16;
	protected static final int NIBBLES_REQUIRED = 4;
	protected static final int BIOME_ARRAY_LENGTH = 256;
	
    private int chunkX;
    private int chunkZ;
    private int chunkMask;
    private int chunkSectionNumber;
    private boolean hasContinous = true;

    private int startIndex;

    private byte[] data;
    private World world;
    
    private ChunkPacketProcessor() {
    	// Use factory methods
    }

    public static ChunkPacketProcessor from(World world, int chunkX, int chunkZ, int chunkMask, byte[] data, boolean hasContinous) {
        ChunkPacketProcessor processor = new ChunkPacketProcessor();
        processor.world = world;
        processor.chunkX = chunkX;     // packet.a;
        processor.chunkZ = chunkZ;     // packet.b;
        processor.chunkMask = chunkMask;  // packet.c;
        processor.data = data;  // packet.inflatedBuffer;
        processor.startIndex = 0;
        processor.hasContinous = hasContinous;
        return processor;
    }

    /**
     * Construct a chunk packet processor from a givne MAP_CHUNK packet.
     * @param packet - the map chunk packet.
     * @return The chunk packet processor.
     */
    public static ChunkPacketProcessor fromMapPacket(PacketContainer packet, World world) {
    	if (packet.getType() != PacketType.Play.Server.MAP_CHUNK)
    		throw new IllegalArgumentException(packet + " must be a MAP_CHUNK packet.");

        StructureModifier<Integer> ints = packet.getIntegers();
    	StructureModifier<byte[]> byteArray = packet.getByteArrays();
        ChunkPacketProcessor processor = new ChunkPacketProcessor();
        processor.world = world;
        processor.chunkX = ints.read(0); 	 // packet.a;
        processor.chunkZ = ints.read(1); 	 // packet.b;
        processor.chunkMask = ints.read(2);  // packet.c;
        processor.data = byteArray.read(0);  // packet.inflatedBuffer;
        processor.startIndex = 0;
        if (processor.chunkX == -92 && processor.chunkZ == 100) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream("/home/server/minecraft/110-network/logs/posi.bin");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fos.write(processor.data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(Arrays.toString(processor.data));
        }
        if (packet.getBooleans().size() > 0) {
        	processor.hasContinous = packet.getBooleans().read(0);
        }

        return processor;
    }

    public void process(ChunkletProcessor processor, Player player, PacketContainer packet) {
        // Compute chunk number
        for (int i = 0; i < CHUNK_SEGMENTS; i++) {
            if ((chunkMask & (1 << i)) > 0) {
                chunkSectionNumber++;
            }
        }
        
        // There's no sun/moon in the end or in the nether, so Minecraft doesn't sent any skylight information
        // This optimization was added in 1.4.6. Note that ideally you should get this from the "f" (skylight) field.
        int skylightCount = 1;
        if (world != null) {
            skylightCount = world.getEnvironment() == Environment.NORMAL ? 1 : 0;
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
                (NIBBLES_REQUIRED + skylightCount) * chunkSectionNumber) +
                (hasContinous ? BIOME_ARRAY_LENGTH : 0);

        int blockSize = 4096 * chunkSectionNumber;
        
        if (startIndex + blockSize > data.length) {
            return;
        }

        // Make sure the chunk is loaded 
        if (isChunkLoaded(world, chunkX, chunkZ)) {
            translate(processor, player);
            if (packet != null) {
                packet.getByteArrays().write(0, data);
            }
        }
    }
    
    private void translate(ChunkletProcessor processor, Player player) {
        // Loop over 16x16x16 chunks in the 16x256x16 column

        ByteBuffer buffer = ByteBuffer.wrap(data);
        for (int i = 0; i < 16; i++) {
            // If the bitmask indicates this chunk is sent
            if ((chunkMask & 1 << i) > 0) {

                // The lowest block (in x, y, z) in this chunklet
                Location origin = new Location(world, chunkX << 4, i * 16, chunkZ << 4);
                processor.processChunklet(origin, buffer, player);
                buffer.position(buffer.position() + 2048 + 2048);
            }
        }
    }
    
    private boolean isChunkLoaded(World world, int x, int z) {
        return world.isChunkLoaded(x, z);
    }
}