package com.lbc.nlp_domain;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.RoundingMode;
import java.util.Arrays;

import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;

public class BitArray {
    final long[] data;
    int bitCount;

    public BitArray(long bits) {
      this(new long[Ints.checkedCast(LongMath.divide(bits, 64, RoundingMode.CEILING))]);
    }

    // Used by serialization
    BitArray(long[] data) {
      checkArgument(data.length > 0, "data length is zero!");
      this.data = data;
      int bitCount = 0;
      for (long value : data) {
        bitCount += Long.bitCount(value);
      }
      this.bitCount = bitCount;
    }

    /** Returns true if the bit changed value. */
    public boolean set(int index) {
      if (!get(index)) {
        data[index >> 6] |= (1L << index);
        bitCount++;
        return true;
      }
      return false;
    }

    public boolean get(int index) {
      return (data[index >> 6] & (1L << index)) != 0;
    }

    /** Number of bits */
    public int bitSize() {
      return data.length * Long.SIZE;
    }

    /** Number of set bits (1s) */
    int bitCount() {
      return bitCount;
    }

    public BitArray copy() {
      return new BitArray(data.clone());
    }

    /** Combines the two BitArrays using bitwise OR. */
    public void putAll(BitArray array) {
      checkArgument(data.length == array.data.length,
          "BitArrays must be of equal length (%s != %s)", data.length, array.data.length);
      bitCount = 0;
      for (int i = 0; i < data.length; i++) {
        data[i] |= array.data[i];
        bitCount += Long.bitCount(data[i]);
      }
    }

    @Override public boolean equals(Object o) {
      if (o instanceof BitArray) {
        BitArray bitArray = (BitArray) o;
        return Arrays.equals(data, bitArray.data);
      }
      return false;
    }

    @Override public int hashCode() {
      return Arrays.hashCode(data);
    }
  }
