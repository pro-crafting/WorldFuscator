package com.pro_crafting.mc.worldfuscator.engine.processor;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.pro_crafting.mc.worldfuscator.VarIntUtil;
import com.pro_crafting.mc.worldfuscator.engine.BlockTranslator;
import com.pro_crafting.mc.worldfuscator.engine.palette.IndirectPalette;
import com.pro_crafting.mc.worldfuscator.engine.palette.Palette;
import com.pro_crafting.mc.worldfuscator.engine.palette.PaletteFactory;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Implementation of http://wiki.vg/SMP_Map_Format, checking only the chunklet pallette.
 * Only the palette is fuscated.
 */
public class ChunkPaletteChunkletProcessor implements ChunkletProcessor {
    private final BlockTranslator blockTranslator;

    public ChunkPaletteChunkletProcessor(BlockTranslator blockTranslator) {
        this.blockTranslator = blockTranslator;
    }

    @Override
    public boolean processChunkletBlockData(Location origin, ByteBuffer buffer, Player player) {
        short blockCount = buffer.getShort();
        byte bitsPerBlock = buffer.get();

        int beforePalettePosition = buffer.position();
        Palette palette = PaletteFactory.getInstance(bitsPerBlock, buffer);

        int dataLength = VarIntUtil.deserializeVarInt(buffer);

        int beforeData = buffer.position();

        boolean didFuscate = false;
        if (palette.containsAny(blockTranslator.getHiddenGlobalPaletteIds())) {
            if (!blockTranslator.getWorldFuscatorGuard().hasAreaRights(player, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(),origin.getBlockX()+15, origin.getBlockY()+15, origin.getBlockZ()+15, origin.getWorld())) {
                if (palette instanceof IndirectPalette) {
                    didFuscate = true;
                    // TODO: Fallback blocks
                    IndirectPalette indirectPalette = (IndirectPalette) palette;
                    indirectPalette.replace(blockTranslator.getHiddenGlobalPaletteIds(), blockTranslator.getPreferedObfuscationGlobalPaletteId());
                    buffer.position(beforePalettePosition);
                    indirectPalette.write(buffer);
                } else {
                    didFuscate = true;
                    // TODO: Do Global block replacement, using the ChunkAndBlockChunkletProcessor
                }
            }
        }
        buffer.position(beforeData + (dataLength * 8));

        return didFuscate;
    }

    @Override
    public boolean processChunkletBlockEntities(World world, int chunkX, int chunkZ, List<NbtBase<?>> blockEntities, Player player) {
        // TODO: FIX THIS
        return false;
    }

    @Override
    public boolean isThreadSafe() {
        return true;
    }
}
