package com.pro_crafting.mc.worldfuscator;

import java.nio.ByteBuffer;

// taken from 1.9.1 mc server (PacketSerializer class)
public class VarIntUtil {

    public static int deserializeVarInt(ByteBuffer buf) {
        int i = 0;
        int j = 0;
        for (; ; ) {
            int k = buf.get();

            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((k & 0x80) != 128) {
                break;
            }
        }
        return i;
    }

    public static long deserializeVarLong(ByteBuffer buf) {
        long i = 0L;
        int j = 0;

        byte b0;

        do {
            b0 = buf.get();
            i |= (long) (b0 & 127) << j++ * 7;
            if (j > 10) {
                throw new RuntimeException("VarLong too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    public static ByteBuffer serializeVarInt(ByteBuffer buf, int i) {
        while ((i & -128) != 0) {
            buf.put((byte) (i & 127 | 128));
            i >>>= 7;
        }

        buf.put((byte) i);
        return buf;
    }

    public static ByteBuffer serializeVarLong(ByteBuffer buf, long i) {
        while ((i & -128L) != 0L) {
            buf.put((byte) ((int) (i & 127L) | 128));
            i >>>= 7;
        }

        buf.put((byte) (int) i);
        return buf;
    }
}
