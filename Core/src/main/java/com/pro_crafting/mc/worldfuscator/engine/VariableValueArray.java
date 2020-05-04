package com.pro_crafting.mc.worldfuscator.engine;

import java.util.Objects;

// see: https://github.com/GlowstoneMC/Glowstone/blob/bcfd021cc28e9b64ec87f5b12ddf3ef0fc6380ed/src/main/java/net/glowstone/util/VariableValueArray.java
public final class VariableValueArray implements Cloneable {

    private final long[] backing;
    private final int capacity;
    private final int bitsPerValue;
    private final long valueMask;

    public long[] getBacking() {
        return backing;
    }

    /**
     * Creates an instance.
     *
     * @param bitsPerEntry the number of bits into which each value must fit
     * @param data the backing data
     */
    public VariableValueArray(int bitsPerEntry, long[] data) {
        Objects.requireNonNull(data);

        if (bitsPerEntry < 4) {
            bitsPerEntry = 4;
        }

        this.bitsPerValue = bitsPerEntry;
        this.backing = data;

        this.capacity = this.backing.length * 64 / this.bitsPerValue;
        this.valueMask = (1L << this.bitsPerValue) - 1;
    }

    /**
     * Returns a value.
     *
     * @param index the entry to look up
     * @return the entry value
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    public int get(int index) {
        checkIndex(index);

        index *= bitsPerValue;
        int i0 = index >> 6;
        int i1 = index & 0x3f;

        long value = backing[i0] >>> i1;
        int i2 = i1 + bitsPerValue;
        // The value is divided over two long values
        if (i2 > 64) {
            value |= backing[++i0] << 64 - i1;
        }

        return (int) (value & valueMask);
    }

    /**
     * Sets a value.
     *
     * @param index the entry to set
     * @param value the value to set it to
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     * @throws IllegalArgumentException if {@code value} is out of range
     */
    public void set(int index, int value) {
        checkIndex(index);

        if (value < 0) {
            throw new IllegalArgumentException(String
                    .format("value (%s) must not be negative", value));
        }
        if (value > valueMask) {
            throw new IllegalArgumentException(String
                    .format("value (%s) must not be greater than %s", value, valueMask));
        }

        index *= bitsPerValue;
        int i0 = index >> 6;
        int i1 = index & 0x3f;

        backing[i0] = this.backing[i0] & ~(this.valueMask << i1) | (value & valueMask) << i1;
        int i2 = i1 + bitsPerValue;
        // The value is divided over two long values
        if (i2 > 64) {
            i0++;
            backing[i0] = backing[i0] & ~((1L << i2 - 64) - 1L) | value >> 64 - i1;
        }
    }

    private void checkIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(String
                    .format("index (%s) must not be negative", index));
        }
        if (index >= capacity) {
            throw new IndexOutOfBoundsException(String
                    .format("index (%s) must not be greater than the capacity (%s)", index,
                            capacity));
        }
    }
}