package com.pro_crafting.mc.worldfuscator.engine.processor;

import com.pro_crafting.mc.worldfuscator.engine.BlockTranslator;

public class ChunkletProcessorFactory {
    private final BlockTranslator blockTranslator;

    public ChunkletProcessorFactory(BlockTranslator blockTranslator) {
        this.blockTranslator = blockTranslator;
    }

    public ChunkletProcessor getProcessor() {
        switch (blockTranslator.getConfiguration().getFuscationMode()) {
            case CHUNK_PALETTE:
                return new ChunkPaletteChunkletProcessor(blockTranslator);
            case CHUNK_AND_BLOCK:
            default:
                return new ChunkAndBlockChunkletProcessor(blockTranslator);
        }
    }
}
