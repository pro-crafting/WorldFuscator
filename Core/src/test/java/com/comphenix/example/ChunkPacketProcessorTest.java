package com.comphenix.example;

import de.pro_crafting.worldfuscator.Core.BlockTranslater;

import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChunkPacketProcessorTest {
    @Mock
    private MapPacketChunkletProcessor mapProcessor = new MapPacketChunkletProcessor(new BlockTranslater(null));

    private byte[] data;

    @Before
    public void setUp() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("chunk.bin");
        this.data = IOUtils.toByteArray(stream);
    }

    @Test
    public void testProcessCallsChunkletProcessor() throws Exception {
        World world = mock(World.class);
        Player player = mock(Player.class);

        // 16 chunk sections here, because of maximum
        // in reality, this would be a 1
        ChunkPacketProcessor processor = ChunkPacketProcessor.from(world, 0, 0, 16, data, false);

        doNothing().when(mapProcessor).processChunklet(any(Location.class), any(ByteBuffer.class), any(Player.class));
        when(world.isChunkLoaded(anyInt(), anyInt())).thenReturn(true);

        processor.process(mapProcessor, player, null);

        // The testdata contains exactly one chunklet
        verify(mapProcessor, times(1)).processChunklet(any(Location.class), any(ByteBuffer.class), any(Player.class));
    }

}