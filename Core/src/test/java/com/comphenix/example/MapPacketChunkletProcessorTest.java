package com.comphenix.example;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import de.pro_crafting.worldfuscator.Core.BlockTranslater;
import de.pro_crafting.worldfuscator.Core.Configuration;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MapPacketChunkletProcessorTest {

  Location origin = new Location(null, 0, 0, 0);
  MapPacketChunkletProcessor mapProcessor = new MapPacketChunkletProcessor(
      new BlockTranslater(null));
  private byte[] data;

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

    State state = mapProcessor.getState(fS, palette, 0, 1);
    assertEquals(35, state.getId());
    assertEquals(0, state.getData());

    state = mapProcessor.getState(fS, palette, 1, 1);
    assertEquals(35, state.getId());
    assertEquals(1, state.getData());

    state = mapProcessor.getState(fS, palette, 2, 1);
    assertEquals(35, state.getId());
    assertEquals(2, state.getData());

    state = mapProcessor.getState(fS, palette, 16, 1);
    assertEquals(121, state.getId());
    assertEquals(0, state.getData());

    //TODO: add tests for global palette
  }

  @Test
  public void testGetHiddenPaletteIndexReturnsHideListIndex() {
    State[] palette = new State[3];
    palette[0] = new State(1, 0);
    palette[1] = new State(2, 0);
    palette[2] = new State(3, 0);

    Configuration configuration = mock(Configuration.class);
    when(configuration.getObfuscationBlock()).thenCallRealMethod();
    when(configuration.getHideIds()).thenReturn(Lists.newArrayList(1, 3));

    MapPacketChunkletProcessor mapProcessor = new MapPacketChunkletProcessor(
        new BlockTranslater(configuration));

    int hiddenPaletteIndex = mapProcessor.getHiddenPaletteIndex(palette);

    assertEquals(1, hiddenPaletteIndex);
  }

  @Test
  public void testGetHiddenPaletteIndexReturnsPaletteIndex() {
    State[] palette = new State[3];
    palette[0] = new State(1, 0);
    palette[1] = new State(2, 0);
    palette[2] = new State(121, 0);

    Configuration configuration = mock(Configuration.class);
    when(configuration.getObfuscationBlock()).thenCallRealMethod();
    when(configuration.getHideIds()).thenReturn(Lists.newArrayList(1));

    MapPacketChunkletProcessor mapProcessor = new MapPacketChunkletProcessor(
        new BlockTranslater(configuration));

    int hiddenPaletteIndex = mapProcessor.getHiddenPaletteIndex(palette);

    assertEquals(2, hiddenPaletteIndex);
  }

  @Test
  @Ignore
  public void testProcessChunklet() throws Exception {
    Location location = mock(Location.class);
    Player player = mock(Player.class);

    Configuration configuration = mock(Configuration.class);
    when(configuration.getObfuscationBlock()).thenCallRealMethod();
    when(configuration.getHideIds()).thenReturn(Lists.newArrayList(0));

    MapPacketChunkletProcessor mapProcessor = new MapPacketChunkletProcessor(
        new BlockTranslater(configuration));

    for (int i = 0; i < 100; i++) {
      byte[] data = Arrays.copyOf(this.data, this.data.length);
      ByteBuffer buffer = ByteBuffer.wrap(data);
      mapProcessor.processChunklet(location, buffer, player);
    }

  }

  @Test
  public void testShouldHide() throws Exception {
    Configuration configuration = mock(Configuration.class);
    when(configuration.getObfuscationBlock()).thenCallRealMethod();
    when(configuration.getHideIds()).thenReturn(Lists.newArrayList(1, 2, 3, 4, 5, 6, 7));

    MapPacketChunkletProcessor mapProcessor = new MapPacketChunkletProcessor(
        new BlockTranslater(configuration));

    assertEquals(true, mapProcessor.shouldHide(null, 9));
    assertEquals(true, mapProcessor.shouldHide(new State[]{new State(1, 0)}, 8));
    assertEquals(false, mapProcessor.shouldHide(new State[]{}, 8));
  }
}