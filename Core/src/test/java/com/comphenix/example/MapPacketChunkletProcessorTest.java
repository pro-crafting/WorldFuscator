package com.comphenix.example;

import de.pro_crafting.worldfuscator.Core.BlockTranslater;

import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class MapPacketChunkletProcessorTest {
    private byte[] data;
    Location origin = new Location(null, 0, 0, 0);
    MapPacketChunkletProcessor mapProcessor = new MapPacketChunkletProcessor(new BlockTranslater(null));

    @Before
    public void setUp() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("chunk.bin");
        this.data = IOUtils.toByteArray(stream);
    }

    @Test
    public void testGetPalette() {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int bitsPerBlock = buffer.get();
        State[] palette = mapProcessor.getPalette(buffer, bitsPerBlock);

        // 0, 16 * 35, 121
        assertEquals(18, palette.length);
    }

    @Test
    public void testGetStateReturnsBlockDamage() {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int bitsPerBlock = buffer.get();
        State[] palette = mapProcessor.getPalette(buffer, bitsPerBlock);

        int dataLength = mapProcessor.deserializeVarInt(buffer);

        long[] blockIndizes = new long[dataLength];
        buffer.asLongBuffer().get(blockIndizes);
        FlexibleStorage fS = new FlexibleStorage(bitsPerBlock, blockIndizes);

        State state = mapProcessor.getState(fS, palette, 0);
        assertEquals(35, state.getId());
        assertEquals(0, state.getData());

        state = mapProcessor.getState(fS, palette, 1);
        assertEquals(35, state.getId());
        assertEquals(1, state.getData());

        state = mapProcessor.getState(fS, palette, 2);
        assertEquals(35, state.getId());
        assertEquals(2, state.getData());

        state = mapProcessor.getState(fS, palette, 16);
        assertEquals(121, state.getId());
        assertEquals(0, state.getData());
    }
}