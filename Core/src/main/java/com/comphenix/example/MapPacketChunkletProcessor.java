package com.comphenix.example;

import net.myplayplanet.worldfuscator.Core.BlockTranslator;
import net.myplayplanet.worldfuscator.Core.VarIntUtil;
import net.myplayplanet.worldfuscator.Core.palette.Palette;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * Implementation of http://wiki.vg/SMP_Map_Format
 */
public class MapPacketChunkletProcessor implements ChunkPacketProcessor.ChunkletProcessor {

  private final BlockTranslator blockTranslator;

  public MapPacketChunkletProcessor(BlockTranslator blockTranslator) {
    this.blockTranslator = blockTranslator;
  }

  public void processChunklet(Location origin, ByteBuffer buffer, Player player) {
    byte bitsPerBlock = buffer.get();
    Palette palette = Palette.getInstance(bitsPerBlock, buffer);


    int dataLength = VarIntUtil.deserializeVarInt(buffer);

    int beforeData = buffer.position();
    if (palette.containsAny(blockTranslator.getHiddenGlobalPaletteIds())) {
      tranlateChunkData(origin, buffer, player, bitsPerBlock, palette, dataLength, beforeData);
    }
    buffer.position(beforeData + (dataLength * 8));
  }

  private void tranlateChunkData(Location origin, ByteBuffer buffer, Player player,
      int bitsPerBlock, Palette palette, int dataLength, int beforeData) {
    World world = origin.getWorld();
    int originX = origin.getBlockX();
    int originY = origin.getBlockY();
    int originZ = origin.getBlockZ();

    int obfuscationPaletteId = -1;
    // We can only use the preferred obfuscation block, if it actually exists in the palette.
    // If it does not exists, we fall back to any other block state from the palette
    // which is not part of the hidden blocks
    // TODO: The obfuscation block needs to be inserted into the palette if no suitable fallback is found
    if (palette.contains(this.blockTranslator.getPreferedObfuscationGlobalPaletteId())) {
      obfuscationPaletteId = palette.translate(this.blockTranslator.getPreferedObfuscationGlobalPaletteId());
    } else {
      // Does not exist in palette
      // search for fallback
      obfuscationPaletteId = palette.searchAnyNonMatching(this.blockTranslator.getHiddenGlobalPaletteIds());
    }

    // Translate the hidden states from global ids to palette ids
    Collection<Integer> hiddenPaletteIds = palette.translate(this.blockTranslator.getHiddenGlobalPaletteIds());

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

          int paletteIdBefore = getPaletteId(fS, index);

          if (!hiddenPaletteIds.contains(paletteIdBefore) && blockTranslator.needsTranslation(world, x, y, z, player)) {
            fS.set(index, obfuscationPaletteId);
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

  int getPaletteId(FlexibleStorage storage, int index) {
    return storage.get(index);
  }
}