package dev.kord.common

import kotlin.js.JsName
import kotlin.test.*

class BitSetTests {
    @Test
    @JsName("test1")
    fun `a contains b and c`() {
        val a = DiscordBitSet(0b111)
        val b = DiscordBitSet(0b101)
        val c = DiscordBitSet(0b101, 0)
        assertTrue(b in a)
        assertTrue(c in a)
    }

    @Test
    @JsName("test2")
    fun `a and b are equal and have the same hashCode`() {
        val a = DiscordBitSet(0b111, 0)
        val b = DiscordBitSet(0b111)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    @JsName("test3")
    fun `a does not equal b`() {
        val a = DiscordBitSet(0b111, 0)
        val b = DiscordBitSet(0b111, 0b1)
        assertNotEquals(a, b)
    }

    @Test
    @JsName("test4")
    fun `get bits`() {
        val a = DiscordBitSet(0b101, 0)
        assertTrue(a[0])
        assertFalse(a[1])
        assertTrue(a[2])
        for (i in 3..64) assertFalse(a[i])

        val b = DiscordBitSet(1L shl 63)
        for (i in 0..62) assertFalse(b[i])
        assertTrue(b[63])
    }

    @Test
    @JsName("test5")
    fun `set bits`() {
        val a = EmptyBitSet()
        for (i in 0..64) a[i] = true
        assertEquals(DiscordBitSet(ULong.MAX_VALUE.toLong(), 1), a)

        val b = EmptyBitSet()
        b[1] = true
        b[2] = true
        b[5] = true
        assertEquals(DiscordBitSet(0b100110), b)
        b[2] = false
        assertEquals(DiscordBitSet(0b100010), b)
    }

    @Test
    @JsName("test6")
    fun `get a bit out of range`() {
        val a = DiscordBitSet(0b101, 0)
        assertFalse(a[10000])
    }

    @Test
    @JsName("test7")
    fun `add and remove a bit`() {
        val a = DiscordBitSet(0b101, 0)
        a.add(DiscordBitSet(0b111))
        assertEquals(0b111.toString(), a.value)
        a.remove(DiscordBitSet(0b001))
        assertEquals(0b110.toString(), a.value)
    }

    @Test
    @JsName("test8")
    fun `remove a bit`() {
        val a = DiscordBitSet(0b101, 0)
        a.remove(DiscordBitSet(0b111))
        assertEquals("0", a.value)
    }

    @Test
    @JsName("test9")
    fun `binary works`() {
        assertEquals("0", DiscordBitSet().binary)
        assertEquals("0", DiscordBitSet(0).binary)
        assertEquals("10011", DiscordBitSet(0b10011).binary)
        assertEquals(
            "110" +
                "0000000000000000000000000000000000000000000000000000000000111001" +
                "0000000000000000000000000000000000000000000000000000000000001011",
            DiscordBitSet(0b1011, 0b111001, 0b110).binary,
        )
    }

    @Test
    fun equals_works() {
        var a = bitSetOf(0, 0, 1, 4, 80, -13, 32, 0, 0)
        var b = bitSetOf(0, 0, 1, 4, 80, -13, 32)
        assertEquals(a, b)

        a = bitSetOf()
        b = bitSetOf(0, 0, 0)
        assertEquals(a, b)

        a = bitSetOf(1, 2, 3)
        b = bitSetOf(3, 2, 1)
        assertNotEquals(a, b)
    }

    @Test
    fun BitSet_with_trailing_zeros_has_same_hashCode_as_equal_BitSet_without_trailing_zeros() {
        val trailingZeros = bitSetOf(0, 0, 1, 4, 80, -13, 32, 0, 0)
        val noTrailingZeros = bitSetOf(0, 0, 1, 4, 80, -13, 32)

        assertEquals(trailingZeros, noTrailingZeros)
        assertEquals(trailingZeros.hashCode(), noTrailingZeros.hashCode())
    }

    @Test
    fun value_works() {
        val smallBits = bitSetOf(13)
        assertEquals("13", smallBits.value)

        val largeBits = bitSetOf(
            0b00010001010011011001000110011010,
            0b01100010110001011001001000100101,
            0b00100000010111111110100010110011,
        )
        assertEquals("10019467165254902396520075674", largeBits.value)

        val zeros = bitSetOf(0, 0, 0)
        assertEquals("0", zeros.value)
    }

    @Test
    fun binary_works() {
        val smallBits = bitSetOf(13)
        assertEquals("1101", smallBits.binary)

        val largeBits = bitSetOf(
            0b00010001010011011001000110011010,
            0b01100010110001011001001000100101,
            0b00100000010111111110100010110011,
        )
        assertEquals(
            "001000000101111111101000101100110110001011000101100100100010010100010001010011011001000110011010"
                .trimStart('0'),
            largeBits.binary,
        )

        val zeros = bitSetOf(0, 0, 0)
        assertEquals("0", zeros.binary)
    }
}
