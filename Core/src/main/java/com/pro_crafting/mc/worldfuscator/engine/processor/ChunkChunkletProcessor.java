package com.pro_crafting.mc.worldfuscator.engine.processor;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.pro_crafting.mc.worldfuscator.VarIntUtil;
import com.pro_crafting.mc.worldfuscator.engine.BlockTranslator;
import com.pro_crafting.mc.worldfuscator.engine.VariableValueArray;
import com.pro_crafting.mc.worldfuscator.engine.palette.Palette;
import com.pro_crafting.mc.worldfuscator.engine.palette.PaletteFactory;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

public class ChunkChunkletProcessor implements ChunkletProcessor {
    private final BlockTranslator blockTranslator;

    public ChunkChunkletProcessor(BlockTranslator blockTranslator) {
        this.blockTranslator = blockTranslator;
    }

    @Override
    public boolean processChunkletBlockData(Location origin, ByteBuffer buffer, Player player) {
        // skip short blockCount - we do not need it
        buffer.position(buffer.position() + 2);
        byte bitsPerBlock = buffer.get();

        Palette palette = PaletteFactory.getInstance(bitsPerBlock, buffer);

        int dataLength = VarIntUtil.deserializeVarInt(buffer);

        int beforeData = buffer.position();

        boolean didFuscate = false;
        if (palette.containsAny(blockTranslator.getHiddenGlobalPaletteIds())) {
            if (!blockTranslator.getWorldFuscatorGuard().hasAreaRights(player, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(), origin.getBlockX() + 15, origin.getBlockY() + 15, origin.getBlockZ() + 15, origin.getWorld())) {
                didFuscate = translateChunkData(buffer, bitsPerBlock, palette, dataLength, beforeData);
            }
        }
        buffer.position(beforeData + (dataLength * 8));

        return didFuscate;
    }

    private boolean translateChunkData(ByteBuffer buffer, int bitsPerBlock, Palette palette, int dataLength, int beforeData) {
        int obfuscationPaletteId;
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

        VariableValueArray flexibleStorage = new VariableValueArray(bitsPerBlock, blockIndizes);

        boolean didFuscate = false;
        for (int posY = 0; posY < 16; posY++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                for (int posX = 0; posX < 16; posX++) {
                    int index = posX + posZ * 16 + posY * 256;

                    int paletteIdBefore = flexibleStorage.get(index);

                    if (hiddenPaletteIds.contains(paletteIdBefore)) {
                        flexibleStorage.set(index, obfuscationPaletteId);
                        didFuscate = true;
                    }
                }
            }
        }

        if (didFuscate) {
            buffer.position(beforeData);
            buffer.asLongBuffer().put(flexibleStorage.getBacking());
        }

        return didFuscate;
    }

    @Override
    public boolean processChunkletBlockEntities(World world, int chunkX, int chunkZ, List<NbtBase<?>> blockEntities, Player player) {

        // TODO: Fix this
        return false;
    }

    @Override
    public boolean isThreadSafe() {
        return true;
    }
}
