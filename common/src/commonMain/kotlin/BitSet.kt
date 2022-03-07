package dev.kord.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.JsName

private val EMPTY_BIT_SET = BitSet(EMPTY_INT_ARRAY)

public fun emptyBitSet(): BitSet = EMPTY_BIT_SET
public fun bitSetOf(): BitSet = EMPTY_BIT_SET
public fun bitSetOf(vararg words: Int): BitSet = BitSet(words)

@ExperimentalUnsignedTypes
public fun bitSetOf(vararg words: UInt): BitSet = BitSet(words.asIntArray())

// toUInt().toULong() to fill with zeros
public fun BitSet(value: Int): BitSet = BitSet(value.toUInt().toULong())
public fun BitSet(value: UInt): BitSet = BitSet(value.toULong())
public fun BitSet(value: Long): BitSet = BitSet(value.toULong())
public fun BitSet(value: ULong): BitSet = BitSet(value.toIntArray())
public fun BitSet(value: String): BitSet = BitSet(value.parseUnsignedIntegerToIntArray())


public fun mutableBitSetOf(): MutableBitSet = MutableBitSet(EMPTY_INT_ARRAY)
public fun mutableBitSetOf(vararg words: Int): MutableBitSet = MutableBitSet(words)

@ExperimentalUnsignedTypes
public fun mutableBitSetOf(vararg words: UInt): MutableBitSet = MutableBitSet(words.asIntArray())

// toUInt().toULong() to fill with zeros
public fun MutableBitSet(value: Int): MutableBitSet = MutableBitSet(value.toUInt().toULong())
public fun MutableBitSet(value: UInt): MutableBitSet = MutableBitSet(value.toULong())
public fun MutableBitSet(value: Long): MutableBitSet = MutableBitSet(value.toULong())
public fun MutableBitSet(value: ULong): MutableBitSet = MutableBitSet(value.toIntArray())
public fun MutableBitSet(value: String): MutableBitSet = MutableBitSet(value.parseUnsignedIntegerToIntArray())

@JsName("emptyMutableBitSet")
public fun MutableBitSet(): MutableBitSet = MutableBitSet(EMPTY_INT_ARRAY)


private const val WORD_SIZE = Int.SIZE_BITS

// index components
private val Int.word get() = this / WORD_SIZE
private val Int.inWord get() = this % WORD_SIZE

@Serializable(with = BitSet.Serializer::class)
// open class but internal constructor to prevent external inheritance but allow for MutableBitSet
public open class BitSet internal constructor(
    // this is `var` to be resizable in MutableBitSet, don't mutate or change in BitSet!!!
    /** **Don't touch outside BitSet or MutableBitSet!!!** */
    internal var words: IntArray,
) {

    internal val bitIndices get() = 0 until words.size * WORD_SIZE

    /** The amount of words that are actually required (excluding trailing zero words). */
    internal val requiredWords get() = words.indexOfLast { it != 0 } + 1

    private fun wordOrZero(index: Int) = words.getOrElse(index) { 0 }

    /** Returns whether no bits are set. */
    public val isEmpty: Boolean get() = words.all { it == 0 }

    /** Returns whether the bit at the specified [index] is set. */
    public operator fun get(index: Int): Boolean {
        require(index >= 0) { "index must be >= 0 but was $index" }

        if (index !in bitIndices) return false

        val word = words[index.word]
        val testBit = 1 shl index.inWord

        return (word and testBit) != 0
    }

    public operator fun contains(other: BitSet): Boolean {
        for ((index, otherWord) in other.words.withIndex()) {
            val wordsMatch = (this.wordOrZero(index) and otherWord) == otherWord
            if (!wordsMatch) return false
        }
        return true
    }

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BitSet) return false

        val maxSize = maxOf(this.words.size, other.words.size)

        for (index in 0 until maxSize) {
            if (this.wordOrZero(index) != other.wordOrZero(index)) return false
        }
        return true
    }

    final override fun hashCode(): Int {
        // implementation based on java.util.Arrays.hashCode but ignoring trailing zero words to meet requirement in
        // hashCode contract:
        // "If two objects are equal according to the equals() method, then calling the hashCode method on each of the
        // two objects must produce the same integer result."

        val lastNonZeroIndex = words.indexOfLast { it != 0 }

        var result = 1

        for (index in 0..lastNonZeroIndex) {
            result = 31 * result + words[index]
        }

        return result
    }


    public operator fun plus(other: BitSet): BitSet {
        // copy this.words, pad end with zeros if necessary
        val combinedWords = this.words.copyOf(newSize = maxOf(this.requiredWords, other.requiredWords))

        combinedWords.setBits(from = other.words)

        return BitSet(words = combinedWords.stripTrailingZeros())
    }

    public operator fun minus(other: BitSet): BitSet {
        // copy this.words, it can't get larger when removing bits
        val combinedWords = this.words.copyOf(newSize = this.requiredWords)

        combinedWords.clearBits(from = other.words)

        return BitSet(words = combinedWords.stripTrailingZeros())
    }

    public val value: String
        get() {
            val buffer = ByteBuffer.allocate(words.size * Int.SIZE_BYTES).order(BIG_ENDIAN)
            buffer.asIntBuffer().put(words.reversedArray())
            return BigInteger(1, buffer.array()).toString()
        }

    public val binary: String
        get() = words
            .map { it.toUInt().toString(radix = 2).padStart(length = WORD_SIZE, padChar = '0') }
            .reversed() // words are in little-endian order, string representation in big-endian order
            .joinToString(separator = "")
            .trimStart('0') // trim leading zeros
            .ifEmpty { "0" }

    override fun toString(): String = "BitSet($binary)"


    internal object Serializer : KSerializer<BitSet> {
        override val descriptor = PrimitiveSerialDescriptor("BitSet", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder) = BitSet(decoder.decodeString())

        override fun serialize(encoder: Encoder, value: BitSet) {
            encoder.encodeString(value.value)
        }
    }
}

public class MutableBitSet internal constructor(words: IntArray) : BitSet(words) {

    /** Sets the bit at the specified [index] to [value]. */
    public operator fun set(index: Int, value: Boolean) {
        require(index >= 0) { "index must be >= 0 but was $index" }

        if (index !in bitIndices) {
            words = words.copyOf(newSize = (index / WORD_SIZE) + 1)
        }

        val wordIndex = index.word
        val setBit = value.toBit() shl index.inWord

        words[wordIndex] = words[wordIndex] or setBit
    }

    public fun add(other: BitSet) {
        val requiredWords = other.requiredWords
        if (this.words.size < requiredWords) {
            this.words = this.words.copyOf(newSize = requiredWords)
        }

        this.words.setBits(from = other.words)
    }

    public fun remove(other: BitSet) {
        this.words.clearBits(from = other.words)
    }

    public fun toBitSet(): BitSet = BitSet(words = words.copyOf(newSize = requiredWords))
    public fun copy(): MutableBitSet = MutableBitSet(words = words.copyOf(newSize = requiredWords))

    override fun toString(): String = "MutableBitSet($binary)"
}
