package com.comphenix.example;

import de.pro_crafting.worldfuscator.Core.BlockTranslater;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;

public class MapPacketChunkletProcessor implements ChunkPacketProcessor.ChunkletProcessor {
    private final BlockTranslater blockTranslater;
    private final State[] emptyState = new State[0];

    public MapPacketChunkletProcessor(BlockTranslater blockTranslater) {
        this.blockTranslater = blockTranslater;
    }

    public void processChunklet(Location origin, ByteBuffer buffer, Player player) {
        World world = origin.getWorld();
        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        int bitsPerBlock = buffer.get();
        int paletteLength;
        State[] palette = emptyState;
        if (bitsPerBlock != 0) {
            paletteLength = deserializeVarInt(buffer);
            palette = new State[paletteLength];
            for (int x = 0; x < paletteLength; x++) {
                int state = deserializeVarInt(buffer);
                palette[x] = new State(state >> 4, state & 0xF);
            }
        }
        int dataLength = deserializeVarInt(buffer) * 8;

        int beforeData = buffer.position();
        long[] blockIndizes = new long[dataLength / 8];
        buffer.asLongBuffer().get(blockIndizes);
        FlexibleStorage fS = new FlexibleStorage(bitsPerBlock, blockIndizes);

        for (int posY = 0; posY < 16; posY++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                for (int posX = 0; posX < 16; posX++) {
                    int index = posX + posZ * 16 + posY * 256;

                    int x = originX + posX;
                    int y = originY + posY;
                    int z = originZ + posZ;

                    State blockStateBefore = palette[fS.get(index)];
                    int blockIdAfter = blockTranslater.translateBlockID(world, x, y, z, player, blockStateBefore);

                    if (blockStateBefore.getId() != blockIdAfter) {
                        fS.set(index, 0);
                    }
                }
            }
        }

        buffer.position(beforeData);
        buffer.asLongBuffer().put(blockIndizes);
        buffer.position(beforeData + dataLength);
    }

    // Aus dem 1.9.1 MC Server (PacketSerializer)
    public int deserializeVarInt(ByteBuffer buf) {
        int i = 0;
        int j = 0;
        for (; ; ) {
            int k = buf.get();

            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((k & 0x80) != 128) {
                break;
            }
        }
        return i;
    }
}