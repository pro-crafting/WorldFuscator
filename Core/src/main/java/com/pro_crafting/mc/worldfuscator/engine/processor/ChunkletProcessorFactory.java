package com.pro_crafting.mc.worldfuscator.engine.processor;

import com.pro_crafting.mc.worldfuscator.WorldFuscator;
import com.pro_crafting.mc.worldfuscator.engine.BlockTranslator;

public class ChunkletProcessorFactory {
    private final BlockTranslator blockTranslator;
    private final WorldFuscator plugin;

    public ChunkletProcessorFactory(WorldFuscator plugin) {
        this.blockTranslator = plugin.getTranslator();
        this.plugin = plugin;
    }

    public ChunkletProcessor getProcessor() {
        switch (plugin.getConfiguration().getFuscationMode()) {
            case CHUNK_PALETTE:
                return new ChunkPaletteChunkletProcessor(blockTranslator);
            case CHUNK_AND_BLOCK:
            default:
                return new ChunkAndBlockChunkletProcessor(blockTranslator);
        }
    }
}
