package com.pro_crafting.mc.worldfuscator.engine;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.pro_crafting.mc.worldfuscator.VarIntUtil;
import com.pro_crafting.mc.worldfuscator.engine.palette.Palette;
import com.pro_crafting.mc.worldfuscator.engine.palette.PaletteFactory;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of http://wiki.vg/SMP_Map_Format
 */
public class MapPacketChunkletProcessor implements ChunkPacketProcessor.ChunkletProcessor {

    private final BlockTranslator blockTranslator;

    public MapPacketChunkletProcessor(BlockTranslator blockTranslator) {
        this.blockTranslator = blockTranslator;
    }

    public boolean processChunkletBlockData(Location origin, ByteBuffer buffer, Player player) {
        // skip short blockCount - we do not need it
        buffer.position(buffer.position() + 2);
        byte bitsPerBlock = buffer.get();

        Palette palette = PaletteFactory.getInstance(bitsPerBlock, buffer);

        int dataLength = VarIntUtil.deserializeVarInt(buffer);

        int beforeData = buffer.position();

        boolean didFuscate = false;
        if (palette.containsAny(blockTranslator.getHiddenGlobalPaletteIds())) {
            if (!blockTranslator.getWorldFuscatorGuard().hasAreaRights(player, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(),origin.getBlockX()+15, origin.getBlockY()+15, origin.getBlockZ()+15, origin.getWorld())) {
                didFuscate = translateChunkData(origin, buffer, player, bitsPerBlock, palette, dataLength, beforeData);
            }
        }
        buffer.position(beforeData + (dataLength * 8));

        return didFuscate;
    }

    @Override
    public boolean processChunkletBlockEntities(World world, int chunkX, int chunkZ, List<NbtBase<?>> blockEntities, Player player) {
        if (blockEntities.isEmpty() || blockTranslator.getConfiguration().getHiddenBlockEntityIds().isEmpty()) {
            return false;
        }

        if (blockTranslator.getWorldFuscatorGuard().hasAreaRights(player, chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15, 256, chunkZ * 16 +15, world)) {
            return false;
        }

        Iterator<NbtBase<?>> iterator = blockEntities.iterator();
        while (iterator.hasNext()) {
            NbtBase<?> blockEntity = iterator.next();
            NbtCompound nbtCompound = NbtFactory.asCompound(blockEntity);
            String id = nbtCompound.getString("id");
            if (id == null) {
                break;
            }

            if (blockTranslator.getConfiguration().getHiddenBlockEntityIds().contains(id)) {
                int x = nbtCompound.getInteger("x");
                int y = nbtCompound.getInteger("y");
                int z = nbtCompound.getInteger("z");
                if (blockTranslator.needsTranslation(world, x, y, z, player)) {
                    iterator.remove();
                }
            }
        }

        return true;
    }

    @Override
    public boolean isThreadSafe() {
        // We do not access any unsafe api here, check if our guard is thread safe
        return blockTranslator.getWorldFuscatorGuard().isThreadSafe();
    }

    private boolean translateChunkData(Location origin, ByteBuffer buffer, Player player,
                                    int bitsPerBlock, Palette palette, int dataLength, int beforeData) {
        World world = origin.getWorld();
        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

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
        IntList hiddenPaletteIds = palette.translate(this.blockTranslator.getHiddenGlobalPaletteIds());

        long[] blockIndizes = new long[dataLength];
        buffer.asLongBuffer().get(blockIndizes);

        VariableValueArray flexibleStorage = new VariableValueArray(bitsPerBlock, blockIndizes);

        boolean didFuscate = false;
        for (int posY = 0; posY < 16; posY++) {
            for (int posZ = 0; posZ < 16; posZ++) {
                for (int posX = 0; posX < 16; posX++) {
                    int index = posX + posZ * 16 + posY * 256;

                    int x = originX + posX;
                    int y = originY + posY;
                    int z = originZ + posZ;

                    int paletteIdBefore = flexibleStorage.get(index);

                    if (hiddenPaletteIds.contains(paletteIdBefore) && blockTranslator.needsTranslation(world, x, y, z, player)) {
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
}