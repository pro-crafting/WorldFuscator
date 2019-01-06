package net.myplayplanet.worldfuscator.Core;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.comphenix.example.State;
import com.google.common.collect.Lists;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BlockTranslatorTest {

  @Spy
  BlockTranslator translater = new BlockTranslator();

  @Test
  public void testTranslateBlockID() throws Exception {
    Configuration configuration = mock(Configuration.class);
    translater.setConfiguration(configuration);
    World world = mock(World.class);
    Player player = mock(Player.class);

    when(configuration.getObfuscationBlock()).thenCallRealMethod();
    when(configuration.getHideIds()).thenReturn(Lists.newArrayList(1, 2, 3));

    // We excpect the same block id to be returned, when this id is not in the config
    int translated = translater.translateBlockMaterial(world, 0, 0, 0, player, new State(4, 0));
    assertEquals(4, translated);

    // Should return the obfuscation block, when hasRights returns false
    translated = translater.translateBlockMaterial(world, 0, 0, 0, player, new State(2, 0));
    assertEquals(configuration.getObfuscationBlock(), translated);

    // Should return the normal block, when hasRights returns false
    when(translater.hasRights(any(Player.class), anyInt(), anyInt(), anyInt(), any(World.class)))
        .thenReturn(true);
    translated = translater.translateBlockMaterial(world, 0, 0, 0, player, new State(2, 0));
    assertEquals(2, translated);
  }
}