package net.myplayplanet.worldfuscator.Core;

import java.nio.ByteBuffer;
import junit.framework.Assert;
import org.junit.Test;

public class VarIntUtilTest {

  @Test
  public void testVarintConversion() {
    ByteBuffer buffer = ByteBuffer.wrap(new byte[1024]);

    VarIntUtil.serializeVarInt(buffer, 127);
    buffer.position(0);
    Assert.assertEquals(127, VarIntUtil.deserializeVarInt(buffer));
  }
}