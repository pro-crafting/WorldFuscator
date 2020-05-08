package com.pro_crafting.mc.worldfuscator.engine;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import org.bukkit.World;

import java.util.List;

public class ChunkPacketData {
    int chunkX;
    int chunkZ;
    int primaryBitMask;
    boolean isFullChunk;
    byte[] data;
    List<NbtBase<?>> blockEntities;
    int startIndex;
    World world;

    public static ChunkPacketData from(World world, int chunkX, int chunkZ, int chunkMask,
                                            byte[] data, boolean isContinuous) {
        ChunkPacketData chunkData = new ChunkPacketData();
        chunkData.world = world;
        chunkData.chunkX = chunkX;     // packet.a;
        chunkData.chunkZ = chunkZ;     // packet.b;
        chunkData.primaryBitMask = chunkMask;  // packet.c;
        chunkData.data = data;  // packet.inflatedBuffer;
        chunkData.startIndex = 0;
        chunkData.isFullChunk = isContinuous;

        return chunkData;
    }

    /**
     * Construct a chunk packet processor from a givne MAP_CHUNK packet.
     *
     * @param packet - the map chunk packet.
     * @param world  No further description provided
     * @return The chunk packet processor.
     */
    public static ChunkPacketData fromMapPacket(PacketContainer packet, World world) {
        if (packet.getType() != PacketType.Play.Server.MAP_CHUNK) {
            throw new IllegalArgumentException(packet + " must be a MAP_CHUNK packet.");
        }


        StructureModifier<Integer> ints = packet.getIntegers();
        StructureModifier<byte[]> byteArray = packet.getByteArrays();
        ChunkPacketData chunkData = new ChunkPacketData();

        chunkData.world = world;
        chunkData.chunkX = ints.read(0);
        chunkData.chunkZ = ints.read(1);
        chunkData.isFullChunk = packet.getBooleans().read(0);
        chunkData.primaryBitMask = ints.read(2);
        chunkData.data = byteArray.read(0).clone();

        // TODO: Somehow needs to be cloned
        chunkData.blockEntities = packet.getListNbtModifier().read(0);


        return chunkData;
    }

    public static PacketContainer clone(PacketContainer packet, World world) {
        return ChunkPacketData.fromMapPacket(packet, world).getNewChunkPacket();
    }

    public static PacketContainer clone(ChunkPacketData chunkData) {
        PacketContainer clone = new PacketContainer(PacketType.Play.Server.MAP_CHUNK);

        StructureModifier<Integer> ints = clone.getIntegers();
        StructureModifier<byte[]> bytes = clone.getByteArrays();

        ints.write(0, chunkData.chunkX);
        ints.write(1, chunkData.chunkZ);
        clone.getBooleans().write(0, chunkData.isFullChunk);
        ints.write(2, chunkData.primaryBitMask);
        bytes.write(0, chunkData.data.clone());
        clone.getListNbtModifier().write(0, chunkData.blockEntities);

        return clone;
    }

    public PacketContainer getNewChunkPacket() {
        return clone(this);
    }
}
