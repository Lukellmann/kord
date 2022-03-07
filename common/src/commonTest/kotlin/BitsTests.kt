package dev.kord.common

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BitsTests {

    @Test
    fun stripTrailingZeros_works() {
        val onlyZeros = IntArray(10) { 0 }
        assertTrue(onlyZeros.stripTrailingZeros().isEmpty())

        val noStrippingNeeded = intArrayOf(0, 0, 1, 2, 3, -3)
        assertSame(noStrippingNeeded, noStrippingNeeded.stripTrailingZeros())

        val strippingNeeded = intArrayOf(0, 0, -3, 3, 2, 1, 0, 0)
        val expected = intArrayOf(0, 0, -3, 3, 2, 1)
        assertContentEquals(expected, strippingNeeded.stripTrailingZeros())
    }


    @Test
    fun bigEndianBytesToLittleEndianInts_works() {
        val onlyZeros = ByteArray(10) { 0 }
        assertTrue(onlyZeros.bigEndianBytesToLittleEndianInts().isEmpty())

        val noDiscardingNeeded = byteArrayOf(0b1000_0000.toByte(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        var expected = intArrayOf(0, 0, 0b1000_0000_0000_0000_0000_0000_0000_0000.toInt())
        assertContentEquals(expected, noDiscardingNeeded.bigEndianBytesToLittleEndianInts())

        val discardingNeeded = byteArrayOf(0x00, 0x00, 0x00, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80.toByte())
        expected = intArrayOf(0x10_20_40_80, 0x00_02_04_08)
        assertContentEquals(expected, discardingNeeded.bigEndianBytesToLittleEndianInts())

        val fromDocumentation = byteArrayOf(0, 0, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
        expected = intArrayOf(
            (3 shl 24) or (2 shl 16) or (1 shl 8) or (0 shl 0),
            (7 shl 24) or (6 shl 16) or (5 shl 8) or (4 shl 0),
            (0 shl 24) or (10 shl 16) or (9 shl 8) or (8 shl 0),
        )
        assertContentEquals(expected, fromDocumentation.bigEndianBytesToLittleEndianInts())
    }


    @Test
    fun setBits_works() {
        var target = intArrayOf(0b1100, 0b0000_0000, 0b1010_0100, 123, -127)
        var source = intArrayOf(0b0110, 0b1000_0000, 0b0000_0010)
        var expect = intArrayOf(0b1110, 0b1000_0000, 0b1010_0110, 123, -127)

        target.setBits(from = source)
        assertContentEquals(expect, target)

        target = intArrayOf(0b1100, 0b0000_0000, 0b1010_0100)
        source = intArrayOf(0b0110, 0b1000_0000, 0b0000_0010, 123, -127)
        expect = intArrayOf(0b1110, 0b1000_0000, 0b1010_0110)

        target.setBits(from = source)
        assertContentEquals(expect, target)
    }


    @Test
    fun clearBits_works() {
        var target = intArrayOf(0b1100, 0b1000_0000, 0b1010_0110, 123, -127)
        var source = intArrayOf(0b0110, 0b1000_0000, 0b0000_0010)
        var expect = intArrayOf(0b1000, 0b0000_0000, 0b1010_0100, 123, -127)

        target.clearBits(from = source)
        assertContentEquals(expect, target)

        target = intArrayOf(0b1100, 0b1000_0000, 0b1010_0110)
        source = intArrayOf(0b0110, 0b1000_0000, 0b0000_0010, 123, -127)
        expect = intArrayOf(0b1000, 0b0000_0000, 0b1010_0100)

        target.clearBits(from = source)
        assertContentEquals(expect, target)
    }
}
