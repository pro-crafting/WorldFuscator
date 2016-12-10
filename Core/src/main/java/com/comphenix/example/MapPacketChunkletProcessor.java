package com.comphenix.example;

import de.pro_crafting.worldfuscator.Core.BlockTranslater;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;

/**
 * Implementation of http://wiki.vg/SMP_Map_Format
 */
public class MapPacketChunkletProcessor implements ChunkPacketProcessor.ChunkletProcessor {
    private final BlockTranslater blockTranslater;
    private final State[] emptyState = new State[0];
    private static final State AIR = new State(0, 0);

    public MapPacketChunkletProcessor(BlockTranslater blockTranslater) {
        this.blockTranslater = blockTranslater;
    }

    public void processChunklet(Location origin, ByteBuffer buffer, Player player) {
        World world = origin.getWorld();
        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        int bitsPerBlock = buffer.get();
        State[] palette = getPalette(buffer, bitsPerBlock);
        int hideIndex = this.getHiddenPaletteIndex(palette);

        int dataLength = deserializeVarInt(buffer);

        int beforeData = buffer.position();
        long[] blockIndizes = new long[dataLength];
        buffer.asLongBuffer().get(blockIndizes);

        FlexibleStorage fS = new FlexibleStorage(bitsPerBlock, blockIndizes);

        boolean didFuscate = false;
        for (int posY = 0; posY < 16; posY++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                for (int posX = 0; posX < 16; posX++) {
                    int index = posX + posZ * 16 + posY * 256;

                    int x = originX + posX;
                    int y = originY + posY;
                    int z = originZ + posZ;

                    State blockStateBefore = getState(fS, palette, index, bitsPerBlock);
                    int blockIdAfter = blockTranslater.translateBlockID(world, x, y, z, player, blockStateBefore);

                    if (blockStateBefore.getId() != blockIdAfter) {
                        fS.set(index, hideIndex);
                        didFuscate = true;
                    }
                }
            }
        }

        if (didFuscate) {
            buffer.position(beforeData);
            buffer.asLongBuffer().put(blockIndizes);
        }
        buffer.position(beforeData + (dataLength * 8));
    }

    State[] getPalette(ByteBuffer buffer, int bitsPerBlock) {
        State[] palette = emptyState;
        // The Palette is only sent, when we have less then 9 bits per block
        // Otherwise, global palette is used
        if (bitsPerBlock < 9) {
            int paletteLength = deserializeVarInt(buffer);
            palette = new State[paletteLength];
            for (int x = 0; x < paletteLength; x++) {
                int state = deserializeVarInt(buffer);
                palette[x] = new State(state >> 4, state & 0xF);
            }
        }
        return palette;
    }

    State getState(FlexibleStorage storage, State[] palette, int index, int bitsPerBlock) {
        int blockState = storage.get(index);
        if (bitsPerBlock < 9) {
            if (blockState < palette.length) {
                return palette[blockState];
            } else {
                return AIR;
            }
        } else {
            return new State(blockState >> 4, blockState & 0xF);
        }
    }

    int getHiddenPaletteIndex(State[] palette) {
        // We first want to search if we have the obfuscation block in our palette
        // because this is the optimal match
        int hideId = blockTranslater.getConfiguration().getObfuscationBlock();
        for (int i = 0; i < palette.length; i++) {
            if (palette[i].getId() == hideId) {
                return i;
            }
        }

        // We should never obfuscate to a hidden block
        for (int i = 0; i < palette.length; i++) {
            if (!blockTranslater.getConfiguration().getHideIds().contains(palette[i].getId())) {
                return i;
            }
        }

        // For now, just returning the index 0 is good enough
        return 0;
    }

    // Aus dem 1.9.1 MC Server (PacketSerializer)
    int deserializeVarInt(ByteBuffer buf) {
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