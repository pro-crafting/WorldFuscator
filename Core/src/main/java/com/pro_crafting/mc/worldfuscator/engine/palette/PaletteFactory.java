package com.pro_crafting.mc.worldfuscator.engine.palette;

import java.nio.ByteBuffer;

public class PaletteFactory {
    private PaletteFactory() {

    }

    /**
     * Initializes the correct Palette type based upon bitsPerblock.
     * The palette may be read and advanced to a position after the palette
     *
     * @param bitsPerBlock No further description provided
     * @param buffer       chunk data packet, positioned after full chunk boolean
     * @return No further description provided
     */
    public static Palette getInstance(byte bitsPerBlock, ByteBuffer buffer) {
        if (bitsPerBlock < 9) {
            return new IndirectPalette(buffer);
        } else {
            return new DirectPalette();
        }
    }
}
