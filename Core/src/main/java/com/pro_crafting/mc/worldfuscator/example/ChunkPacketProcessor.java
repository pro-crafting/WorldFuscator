package com.pro_crafting.mc.worldfuscator.example;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.pro_crafting.mc.worldfuscator.VarIntUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

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

    private int chunkX;
    private int chunkZ;
    private int primaryBitMask;
    private boolean isFullChunk;
    private int dataSize;
    private byte[] data;
    private List<NbtBase<?>> blockEntities;
    private int chunkSectionNumber;
    private int startIndex;
    private World world;

    private ChunkPacketProcessor() {
        // Use factory methods
    }

    public int getChunkX() {
        return chunkX;
    }

    public void setChunkX(int chunkX) {
        this.chunkX = chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public void setChunkZ(int chunkZ) {
        this.chunkZ = chunkZ;
    }

    public int getPrimaryBitMask() {
        return primaryBitMask;
    }

    public void setPrimaryBitMask(int primaryBitMask) {
        this.primaryBitMask = primaryBitMask;
    }

    public boolean isFullChunk() {
        return isFullChunk;
    }

    public void setFullChunk(boolean fullChunk) {
        isFullChunk = fullChunk;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public List<NbtBase<?>> getBlockEntities() {
        return blockEntities;
    }

    public void setBlockEntities(List<NbtBase<?>> blockEntities) {
        this.blockEntities = blockEntities;
    }

    public int getChunkSectionNumber() {
        return chunkSectionNumber;
    }

    public void setChunkSectionNumber(int chunkSectionNumber) {
        this.chunkSectionNumber = chunkSectionNumber;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public static ChunkPacketProcessor from(World world, int chunkX, int chunkZ, int chunkMask,
                                            byte[] data, boolean isContinuous) {
        ChunkPacketProcessor processor = new ChunkPacketProcessor();
        processor.world = world;
        processor.chunkX = chunkX;     // packet.a;
        processor.chunkZ = chunkZ;     // packet.b;
        processor.primaryBitMask = chunkMask;  // packet.c;
        processor.data = data;  // packet.inflatedBuffer;
        processor.startIndex = 0;
        processor.isFullChunk = isContinuous;
        writeDebugChunkFiles(processor);
        return processor;
    }

    /**
     * Construct a chunk packet processor from a givne MAP_CHUNK packet.
     *
     * @param packet - the map chunk packet.
     * @param world  No further description provided
     * @return The chunk packet processor.
     */
    public static ChunkPacketProcessor fromMapPacket(PacketContainer packet, World world) {
        if (packet.getType() != PacketType.Play.Server.MAP_CHUNK) {
            throw new IllegalArgumentException(packet + " must be a MAP_CHUNK packet.");
        }


        StructureModifier<Integer> ints = packet.getIntegers();
        StructureModifier<byte[]> byteArray = packet.getByteArrays();
        ChunkPacketProcessor processor = new ChunkPacketProcessor();

        processor.setWorld(world);
        processor.setChunkX(ints.read(0));
        processor.setChunkZ(ints.read(1));
        processor.setFullChunk(packet.getBooleans().read(0));
        processor.setPrimaryBitMask(ints.read(2));
        processor.setData(byteArray.read(0));

        processor.setBlockEntities(packet.getListNbtModifier().read(0));

        writeDebugChunkFiles(processor);

        return processor;
    }

    private static void writeDebugChunkFiles(ChunkPacketProcessor processor) {
        if (!isDebugEnabled) {
            return;
        }

        File chunkFolder = new File(dataFolder, "chunks");
        chunkFolder.mkdir();

        File worldChunkFolder = new File(chunkFolder, processor.world.getName());
        worldChunkFolder.mkdir();

        try (FileOutputStream fos = new FileOutputStream(
                new File(worldChunkFolder, processor.chunkX + "." + processor.chunkZ + ".chunk"))) {
            fos.write(processor.data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[processor.data.length + 2048 + 2048 + 1024];
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.putInt(processor.chunkX);
        bb.putInt(processor.chunkZ);
        bb.put(processor.isFullChunk ? (byte) 0x01 : (byte) 0x00);
        VarIntUtil.serializeVarInt(bb, processor.primaryBitMask);
        VarIntUtil.serializeVarInt(bb, processor.data.length);
        bb.put(processor.data);
        //TODO: Implement Biomes and BlockEntities

        try (FileOutputStream fos = new FileOutputStream(
                new File(worldChunkFolder, processor.chunkX + "." + processor.chunkZ + ".mcdp"))) {
            fos.write(bb.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PacketContainer clone(PacketContainer packet, World world) {
        return ChunkPacketProcessor.fromMapPacket(packet, world).getNewChunkPacket();
    }

    public static PacketContainer clone(ChunkPacketProcessor processor) {
        PacketContainer clone = new PacketContainer(PacketType.Play.Server.MAP_CHUNK);

        StructureModifier<Integer> ints = clone.getIntegers();
        StructureModifier<byte[]> bytes = clone.getByteArrays();

        ints.write(0, processor.chunkX);
        ints.write(1, processor.chunkZ);
        clone.getBooleans().write(0, processor.isFullChunk);
        ints.write(2, processor.primaryBitMask);
        bytes.write(0, processor.data.clone());
        clone.getListNbtModifier().write(0, processor.blockEntities);

        return clone;
    }

    public PacketContainer getNewChunkPacket() {
        return clone(this);
    }

    public void process(ChunkletProcessor processor, Player player, PacketContainer packet) {
        // Compute chunk number
        for (int i = 0; i < CHUNK_SEGMENTS; i++) {
            if ((primaryBitMask & (1 << i)) > 0) {
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
                (isFullChunk ? BIOME_ARRAY_LENGTH : 0);

        int blockSize = 4096 * chunkSectionNumber;

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

        ByteBuffer buffer = ByteBuffer.wrap(this.data);

        for (int i = 0; i < 16; i++) {
            // If the bitmask indicates this chunk is sent
            if ((primaryBitMask & 1 << i) > 0) {

                // The lowest block (in x, y, z) in this chunklet
                Location origin = new Location(world, chunkX << 4, i * 16, chunkZ << 4);
                processor.processChunklet(origin, buffer, player);
            }
        }
        this.data = buffer.array();
    }

    private boolean isChunkLoaded(World world, int x, int z) {
        return world.isChunkLoaded(x, z);
    }

    /**
     * Process the content of a single 16x16x16 chunklet in a 16x256x16 chunk.
     *
     * @author Kristian
     */
    public interface ChunkletProcessor {

        public void processChunklet(Location origin, ByteBuffer buffer, Player player);
    }
}
