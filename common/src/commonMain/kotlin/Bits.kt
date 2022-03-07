package dev.kord.common

import java.math.BigInteger

internal val EMPTY_INT_ARRAY = IntArray(size = 0)

internal fun Boolean.toBit(): Int = if (this) 1 else 0


private fun sharedIndices(a: IntArray, b: IntArray) = 0 until minOf(a.size, b.size)

/** Sets all set bits with a valid index from the specified array in this array. */
internal fun IntArray.setBits(from: IntArray) {
    for (index in sharedIndices(this, from)) {
        // set logic: a |= b
        this[index] = this[index] or from[index]
    }
}

/** Clears all set bits with a valid index from the specified array in this array. */
internal fun IntArray.clearBits(from: IntArray) {
    for (index in sharedIndices(this, from)) {
        // clear logic: a &= ~b
        this[index] = this[index] and from[index].inv()
    }
}

/** Returns an array with trailing zeros removed, it will be the same instance if no stripping is necessary. */
internal fun IntArray.stripTrailingZeros(): IntArray {
    if (isEmpty()) return this

    val lastNonZeroIndex = indexOfLast { it != 0 }

    return when {
        // all zero
        lastNonZeroIndex < 0 -> EMPTY_INT_ARRAY

        // no stripping necessary (no trailing zeros)
        lastNonZeroIndex == lastIndex -> this

        // strip by copying with new and smaller size
        else -> copyOf(newSize = lastNonZeroIndex + 1)
    }
}

internal fun ULong.toIntArray(): IntArray {
    if (this == 0uL) return EMPTY_INT_ARRAY

    val lowerInt = this.toInt()
    val higherInt = (this shr Int.SIZE_BITS).toInt()

    return if (higherInt == 0) intArrayOf(lowerInt) else intArrayOf(lowerInt, higherInt)
}


// ULong.MAX_VALUE: 18446744073709551615
// SAFE_LENGTH = ULong.MAX_VALUE.toString().length - 1
private const val SAFE_LENGTH = 19
internal fun String.parseUnsignedIntegerToIntArray(): IntArray {
    if (length <= SAFE_LENGTH) {
        return toULong().toIntArray()
    }

    val integer = BigInteger(this)
    val sign = integer.signum()

    if (sign == 0) return EMPTY_INT_ARRAY

    require(sign >= 0) { "BitSet can't be constructed from negative value: $this" }

    return integer.toByteArray().bigEndianBytesToLittleEndianInts()
}

private const val ZERO: Byte = 0
internal fun ByteArray.bigEndianBytesToLittleEndianInts(): IntArray {

    val firstNonZeroIndex = indexOfFirst { it != ZERO }

    // -1 if none match predicate -> all zero or empty
    if (firstNonZeroIndex < 0) return EMPTY_INT_ARRAY

    /*
     * ByteArray we get (big-endian):
     * index: 0   1   2    3   4   5   6   7   8   9   10  11  12  13         '00': zero byte
     *       [00, 00, 00, b10, b9, b8, b7, b6, b5, b4, b3, b2, b1, b0]        'bn': byte with `byteSignificance` = n
     *
     * IntArray we want to convert it to (little-endian):
     * index: 0            1            2                          '3_2_1_0': values from above, shifted to the left by
     *       [b3_b2_b1_b0, b7_b6_b5_b4, 00_b10_b9_b8]                         24/16/8/0 respectively and or-ed together
     */

    val numberOfSignificantBytes = size - firstNonZeroIndex

    val fullyNeededInts = numberOfSignificantBytes / Int.SIZE_BYTES
    val leftoverBytes = numberOfSignificantBytes % Int.SIZE_BYTES

    val ints = IntArray(size = if (leftoverBytes == 0) fullyNeededInts else fullyNeededInts + 1)

    // see 'bn' above for meaning of `byteSignificance`
    for (byteSignificance in 0 until numberOfSignificantBytes) {

        // read b0 -> b1 -> b2 -> ...
        val byte = this[lastIndex - byteSignificance].toInt() and 0xFF // treat as unsigned (fill with zeros)

        val intSignificance = byteSignificance / Int.SIZE_BYTES
        val byteShift = (byteSignificance % Int.SIZE_BYTES) * Byte.SIZE_BITS

        ints[intSignificance] = ints[intSignificance] or (byte shl byteShift)
    }

    return ints
}
