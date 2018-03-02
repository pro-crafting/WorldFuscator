package com.comphenix.example;

import net.myplayplanet.worldfuscator.Core.BlockTranslator;
import net.myplayplanet.worldfuscator.Core.VarIntUtil;
import java.nio.ByteBuffer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Implementation of http://wiki.vg/SMP_Map_Format
 */
public class MapPacketChunkletProcessor implements ChunkPacketProcessor.ChunkletProcessor {

  private static final State AIR = new State(0, 0);
  private final BlockTranslator blockTranslator;
  private final State[] emptyState = new State[0];

  public MapPacketChunkletProcessor(BlockTranslator blockTranslator) {
    this.blockTranslator = blockTranslator;
  }

  public void processChunklet(Location origin, ByteBuffer buffer, Player player) {
    int bitsPerBlock = buffer.get();
    State[] palette = getPalette(buffer, bitsPerBlock);

    int dataLength = VarIntUtil.deserializeVarInt(buffer);

    int beforeData = buffer.position();
    if (shouldHide(palette, bitsPerBlock)) {
      tranlateChunkData(origin, buffer, player, bitsPerBlock, palette, dataLength, beforeData);
    }
    buffer.position(beforeData + (dataLength * 8));
  }

  private void tranlateChunkData(Location origin, ByteBuffer buffer, Player player,
      int bitsPerBlock, State[] palette, int dataLength, int beforeData) {
    World world = origin.getWorld();
    int originX = origin.getBlockX();
    int originY = origin.getBlockY();
    int originZ = origin.getBlockZ();

    int hideIndex = this.getHiddenPaletteIndex(palette);

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
          int blockIdAfter = blockTranslator
              .translateBlockID(world, x, y, z, player, blockStateBefore);

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
  }

  State[] getPalette(ByteBuffer buffer, int bitsPerBlock) {
    State[] palette = emptyState;
    // The Palette is only sent, when we have less then 9 bits per block
    // Otherwise, global palette is used
    if (bitsPerBlock < 9) {
      int paletteLength = VarIntUtil.deserializeVarInt(buffer);
      palette = new State[paletteLength];
      for (int x = 0; x < paletteLength; x++) {
        int state = VarIntUtil.deserializeVarInt(buffer);
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
    int hideId = blockTranslator.getConfiguration().getObfuscationBlock();
    for (int i = 0; i < palette.length; i++) {
      if (palette[i].getId() == hideId) {
        return i;
      }
    }

    // We should never obfuscate to a hidden block
    for (int i = 0; i < palette.length; i++) {
      if (!blockTranslator.getConfiguration().getHideIds().contains(palette[i].getId())) {
        return i;
      }
    }

    // For now, just returning the index 0 is good enough
    return 0;
  }

  boolean shouldHide(State[] palette, int bitsPerBlock) {
    //the palette is empty for the global palette
    //so just asume we have to hide blocks
    if (bitsPerBlock > 8) {
      return true;
    }

    for (State paletteEntry : palette) {
      if (blockTranslator.getConfiguration().getHideIds().contains(paletteEntry.getId())) {
        return true;
      }
    }
    return false;
  }
}