package dev.kord.common.serialization

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

private val PLATFORM_MIN_INSTANT = Instant.fromEpochSeconds(Long.MIN_VALUE, Long.MIN_VALUE)
private val PLATFORM_MAX_INSTANT = Instant.fromEpochSeconds(Long.MAX_VALUE, Long.MAX_VALUE)

// epoch milliseconds

/** Serializer that encodes and decodes [Instant]s in [epoch milliseconds][Instant.toEpochMilliseconds]. */
public object InstantInEpochMillisecondsSerializer : KSerializer<Instant> {

    // fractional part of milliseconds is rounded down to the whole number of milliseconds
    // -> add (1ms - 1ns) for biggest non-coerced Instant
    private val VALID_INSTANTS = Instant.fromEpochMilliseconds(Long.MIN_VALUE)..
        ((Instant.fromEpochMilliseconds(Long.MAX_VALUE) + (1.milliseconds - 1.nanoseconds))
            // workaround for https://github.com/Kotlin/kotlinx-datetime/issues/263
            .takeIf { it > Instant.fromEpochSeconds(0) } ?: Instant.fromEpochMilliseconds(Long.MAX_VALUE))

    private val VALID_MILLISECONDS =
        PLATFORM_MIN_INSTANT.toEpochMilliseconds()..PLATFORM_MAX_INSTANT.toEpochMilliseconds()

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dev.kord.common.serialization.InstantInEpochMilliseconds", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        if (value !in VALID_INSTANTS) throw SerializationException(
            "The Instant $value expressed as a number of milliseconds from the epoch Instant does not fit in the " +
                "range of Long type and therefore cannot be serialized with InstantInEpochMillisecondsSerializer"
        )
        encoder.encodeLong(value.toEpochMilliseconds())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val epochMilliseconds = decoder.decodeLong()
        if (epochMilliseconds !in VALID_MILLISECONDS) throw SerializationException(
            "The Instant representing $epochMilliseconds milliseconds from the epoch Instant would be out of the " +
                "boundaries for Instant." // todo test
        )
        return Instant.fromEpochMilliseconds(epochMilliseconds)
    }
}

// TODO use this typealias instead of annotating types/properties with
//  @Serializable(with = InstantInEpochMillisecondsSerializer::class) once
//  https://github.com/Kotlin/kotlinx.serialization/issues/1895 is fixed
// /** An [Instant] that is [serializable][Serializable] with [InstantInEpochMillisecondsSerializer]. */
// public typealias InstantInEpochMilliseconds = @Serializable(with = InstantInEpochMillisecondsSerializer::class) Instant


// epoch seconds

/** Serializer that encodes and decodes [Instant]s in [epoch seconds][Instant.epochSeconds]. */
public object InstantInEpochSecondsSerializer : KSerializer<Instant> {

    private val VALID_SECONDS = PLATFORM_MIN_INSTANT.epochSeconds..PLATFORM_MAX_INSTANT.epochSeconds

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dev.kord.common.serialization.InstantInEpochSeconds", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        // epochSeconds always fits in the range of Long type and never coerces -> no need for range check
        encoder.encodeLong(value.epochSeconds)
    }

    override fun deserialize(decoder: Decoder): Instant {
        val epochSeconds = decoder.decodeLong()
        if (epochSeconds !in VALID_SECONDS) throw SerializationException(
            "The Instant representing $epochSeconds seconds from the epoch Instant would be out of the boundaries " +
                "for Instant." // todo test
        )
        return Instant.fromEpochSeconds(epochSeconds)
    }
}

// TODO use this typealias instead of annotating types/properties with
//  @Serializable(with = InstantInEpochSecondsSerializer::class) once
//  https://github.com/Kotlin/kotlinx.serialization/issues/1895 is fixed
// /** An [Instant] that is [serializable][Serializable] with [InstantInEpochSecondsSerializer]. */
// public typealias InstantInEpochSeconds = @Serializable(with = InstantInEpochSecondsSerializer::class) Instant
